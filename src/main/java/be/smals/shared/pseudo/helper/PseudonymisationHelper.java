package be.smals.shared.pseudo.helper;

import static be.smals.shared.pseudo.helper.exceptions.ThrowableWrapperException.throwWrapped;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.synchronizedSet;

import be.smals.shared.pseudo.helper.exceptions.ThrowableWrapperException;
import be.smals.shared.pseudo.helper.internal.DomainImpl;
import be.smals.shared.pseudo.helper.utils.ThrowingFunction;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEObjectJSON;
import com.nimbusds.jose.crypto.MultiDecrypter;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import java.net.URI;
import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.crypto.SecretKey;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Library provided for the pseudonymisation of data following the requirements outlined in the eHealth cookbook for Pseudonymisation API.
 *
 * @see <a href="https://portal.api.ehealth.fgov.be/api-details?apiId=eb8015c0-693b-4c4f-bab9-f671d35ddc15&managerId=1&Itemid=171">eHealth Pseudonymisation API</a>
 */
public final class PseudonymisationHelper {

  private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
  private static final Logger log = LoggerFactory.getLogger(PseudonymisationHelper.class);

  private final URI jwksUrl;
  private final Supplier<CompletableFuture<String>> jwksSupplier;
  private final PseudonymisationClient pseudonymisationClient;
  private final PrivateKeySupplier privateKeySupplier;
  private final SecureRandom secureRandom;
  private final Object domainsLock;
  private final ConcurrentHashMap<String, DomainImpl> domains;
  private final Set<String> refreshableDomains;
  /**
   * Unmodifiable copy of refreshableDomains.
   * <p>
   * It is created to prevent any change on refreshableDomains causing damage on the navigation in the returned list by the caller.
   */
  private volatile Set<String> unmodifiableCopyOfRefreshableDomains;
  private volatile CompletableFuture<JWKSet> jwkSet;

  /**
   * @param jwksUrl                The JSON Web Key Set URL used by eHealth Pseudonymisation service to encrypt the domaim secret keys.
   *                               It <strong>must</strong> be exactly the URL defined in eHealth Pseudonymisation service.
   * @param jwksSupplier           A {@link Supplier} of the JWKS pointed by {@code jwksUrl}.
   * @param pseudonymisationClient The {@link PseudonymisationClient} to use to make calls to eHealth pseudonymisation service.
   * @param privateKeySupplier     The {@link PrivateKeySupplier} to use to decrypt the secret keys of the domain.
   */
  @SuppressWarnings("RedundantThrows")
  private PseudonymisationHelper(final URI jwksUrl,
                                 final Supplier<CompletableFuture<String>> jwksSupplier,
                                 final PseudonymisationClient pseudonymisationClient,
                                 final PrivateKeySupplier privateKeySupplier) {
    this.jwksUrl = jwksUrl;
    this.jwksSupplier = jwksSupplier;
    this.pseudonymisationClient = pseudonymisationClient;
    this.privateKeySupplier = privateKeySupplier;
    refreshableDomains = synchronizedSet(new HashSet<>(4, 1f));
    unmodifiableCopyOfRefreshableDomains = Set.of();
    secureRandom = CryptoServicesRegistrar.getSecureRandom();
    domainsLock = new Object();
    if (jwksUrl == null) {
      log.info("`jwksUrl` is null: this `PseudonymisationHelper` will not be able to encrypt or decrypt any transit info");
    }
    if (jwksSupplier == null) {
      log.info("`jwksSupplier` is null: this `PseudonymisationHelper` will not be able to encrypt or decrypt any transit info");
      jwkSet = CompletableFuture.failedFuture(new NullPointerException("`jwksSupplier` cannot be null if you need to encrypt/decrypt transit info"));
    } else {
      refreshJwks();
    }
    Stream.of(Map.entry("jwksSupplier", Optional.ofNullable(jwksSupplier)))
          .filter(property -> property.getValue().isEmpty())
          .forEach(property -> log.info("`{}` is null: this PseudonymisationHelper will not be able to encrypt or decrypt any transit info",
                                        property.getKey()));
    domains = new ConcurrentHashMap<>(8, 0.75f, 8);
  }

  public CompletableFuture<? extends Domain> getDomain(final String domainKey) {
    return Optional.ofNullable(domains.get(domainKey))
                   .map(CompletableFuture::completedFuture)
                   .orElseGet(() -> refreshDomain(domainKey).thenApply(unused -> domains.get(domainKey)));
  }

  /**
   * Return the {@link Set} of domains that must be refreshed.
   * <p>
   * This list will be automatically populated by this {@link PseudonymisationHelper}:
   * each time a domain is created,
   * if the JKU is defined as recipient of any key of the domain,
   * the domain will be added to this set.
   *
   * @return an unmodifiable {@link Set} containing the list of domains that must be refreshed.
   */
  @SuppressWarnings("unused")
  public Set<String> refreshableDomains() {
    return unmodifiableCopyOfRefreshableDomains;
  }

  public CompletableFuture<Void> refreshDomain(final String domainKey) {
    synchronized (domainsLock) {
      return pseudonymisationClient
                 .getDomain(domainKey)
                 .thenApply(this::createDomain)
                 .thenAccept(domain -> domains.put(domain.key(), domain));
    }
  }

  private DomainImpl createDomain(final String rawDomain) throws ThrowableWrapperException {
    try {
      String activeKid = null;
      EncryptionMethod activeKeyAlgorithm = null;
      boolean isKnownJku = false;
      final var jku = jwksUrl == null ? null : jwksUrl.toString();
      final var parsedEHealthDomain = GSON.fromJson(rawDomain, Map.class);
      final var domainKey = (String) parsedEHealthDomain.get("domain");
      @SuppressWarnings("unchecked")
      final var secretKeysFromEHealth = (List<Map<String, Object>>) parsedEHealthDomain.get("secretKeys");
      final var secretKeys = new ConcurrentHashMap<String, SecretKey>(secretKeysFromEHealth.size(), 1f, 1);
      //noinspection unchecked
      if (jku != null && jwksSupplier != null && ((List<String>) parsedEHealthDomain.get("jku")).contains(jku)) {
        isKnownJku = true;
        for (final Map<String, Object> secretKey : secretKeysFromEHealth) {
          try {
            final var kid = (String) secretKey.get("kid");
            @SuppressWarnings("unchecked")
            final var parsedJwe = JWEObjectJSON.parse((Map<String, Object>) secretKey.get("encoded"));
            final var isActiveKid = TRUE.equals(secretKey.get("active"));
            final var myKid = parsedJwe.getRecipients().stream()
                                       .filter(recipient -> jku.equals(recipient.getUnprotectedHeader().getParam("jku")))
                                       .findFirst()
                                       .map(recipient -> recipient.getUnprotectedHeader().getKeyID());
            if (myKid.isPresent()) {
              final var privateKid = myKid.get();
              final var jweKey = getJweKey(privateKid, domainKey);
              // If the private key is known
              if (jweKey != null) {
                final var privateKey = privateKeySupplier.getByHash(jweKey.getX509CertSHA256Thumbprint().toString());
                parsedJwe.decrypt(new MultiDecrypter(new RSAKey.Builder(jweKey.toRSAKey()).privateKey(privateKey).build()));
                final var jwk = JWK.parse(parsedJwe.getPayload().toString());
                final var algName = jwk.getAlgorithm().getName();
                secretKeys.put(kid, ((OctetSequenceKey) jwk).toSecretKey(algName));
                if (isActiveKid) {
                  activeKid = kid;
                  activeKeyAlgorithm = EncryptionMethod.parse(algName);
                }
              }
            }
          } catch (final ParseException e) {
            throw new ThrowableWrapperException(e);
          }
        }
      }
      final var crv = (String) parsedEHealthDomain.get("crv");
      final var curve = ECNamedCurveTable.getParameterSpec(crv).getCurve();
      final var bufferSize = ((Number) parsedEHealthDomain.get("bufferSize")).intValue();
      final var domain = new DomainImpl(domainKey,
                                        crv,
                                        curve,
                                        (String) parsedEHealthDomain.get("audience"),
                                        bufferSize,
                                        secretKeys,
                                        activeKid,
                                        activeKeyAlgorithm,
                                        Duration.parse((String) parsedEHealthDomain.get("timeToLiveInTransit")),
                                        pseudonymisationClient,
                                        secureRandom);
      if (isKnownJku) {
        if (refreshableDomains.add(domainKey)) {
          unmodifiableCopyOfRefreshableDomains = Set.copyOf(refreshableDomains);
        }
      }
      return domain;
    } catch (final Exception e) {
      return throwWrapped(e);
    }
  }

  private JWK getJweKey(final String privateKid, final String domainKey) {
    var jweKey = getJwks().getKeyByKeyId(privateKid);
    // If the private key is unknown, we will refresh the JWKS
    if (jweKey == null) {
      refreshJwks();
      jweKey = getJwks().getKeyByKeyId(privateKid);
    }
    // If the private key is still unknown, we log the error
    if (jweKey == null) {
      //noinspection StringConcatenationArgumentToLogCall
      log.error("The kid `" + privateKid +
                "` is not present in the JWKS `" + jwksUrl +
                "`: impossible to encrypt/decrypt any transit info of the domain `" + domainKey + "`");
    }
    return jweKey;
  }

  private JWKSet getJwks() throws ThrowableWrapperException {
    try {
      return jwkSet.get();
    } catch (final InterruptedException e) {
      Thread.currentThread().interrupt();
      return throwWrapped(e);
    } catch (final ExecutionException e) {
      return throwWrapped(e.getCause());
    }
  }

  /**
   * Refresh the JWK set.
   * <p>
   * This method will retrieve the JWK set string by calling the `jwksSupplier` given in the constructor,
   * every time there is an unknown key ID in your recipient of one of the JWE defined in the domain.
   * Despite this, it is recommended to call this method as soon as you know that an update has been made to the JWKS.
   * <p>
   * It is up to you to cache or not the string returned by `jwksSupplier`.
   */
  public void refreshJwks() {
    jwkSet = jwksSupplier.get().thenApply(ThrowingFunction.sneaky(JWKSet::parse));
  }

  /**
   * Creates a {@link Builder} to help create {@link PseudonymisationHelper}.
   *
   * @return a new {@link Builder}
   */
  public static Builder builder() {
    return new Builder();
  }

  @SuppressWarnings("unused")
  public static class Builder {

    private URI jwksUrl;
    private Supplier<CompletableFuture<String>> jwksSupplier;
    private PrivateKeySupplier privateKeySupplier;
    private PseudonymisationClient pseudonymisationClient;

    private Builder() {
    }

    /**
     * Set {@code jwksUrl}.
     *
     * @param jwksUrl The JSON Web Key Set URL used by eHealth Pseudonymisation service to encrypt the domaim secret keys.
     *                It <strong>must</strong> be exactly the URL defined in eHealth Pseudonymisation service.
     * @return {@code this}
     */
    public Builder jwksUrl(final URI jwksUrl) {
      this.jwksUrl = jwksUrl;
      return this;
    }

    /**
     * Set {@code jwksSupplier}.
     *
     * @param jwksSupplier A {@link Supplier} of the JWKS matching the encryption keys returned by the {@link PrivateKeySupplier}.
     * @return {@code this}
     */
    public Builder jwkSupplier(final Supplier<CompletableFuture<String>> jwksSupplier) {
      this.jwksSupplier = jwksSupplier;
      return this;
    }

    /**
     * Set {@code privateKeySupplier}.
     *
     * @param privateKeySupplier The {@link PrivateKeySupplier} to use to decrypt the secret keys of the domain.
     * @return {@code this}
     */
    public Builder privateKeySupplier(final PrivateKeySupplier privateKeySupplier) {
      this.privateKeySupplier = privateKeySupplier;
      return this;
    }

    /**
     * Set {@code privateKeySupplier}.
     *
     * @param pseudonymisationClient The {@link PseudonymisationClient} to use to make calls to eHealth pseudonymisation service.
     * @return {@code this}
     */
    public Builder pseudonymisationClient(final PseudonymisationClient pseudonymisationClient) {
      this.pseudonymisationClient = pseudonymisationClient;
      return this;
    }

    /**
     * Build the {@link PseudonymisationHelper}.
     *
     * @return a new {@link PseudonymisationHelper}.
     */
    public PseudonymisationHelper build() {
      return new PseudonymisationHelper(jwksUrl, jwksSupplier, pseudonymisationClient, privateKeySupplier);
    }
  }
}
