package be.smals.shared.pseudo.helper;

import static be.smals.shared.pseudo.helper.exceptions.ThrowableWrapperException.throwWrapped;
import static java.lang.Boolean.TRUE;
import static java.util.Collections.synchronizedSet;
import static java.util.concurrent.CompletableFuture.failedFuture;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;

import be.smals.shared.pseudo.helper.exceptions.ThrowableWrapperException;
import be.smals.shared.pseudo.helper.internal.DomainImpl;
import be.smals.shared.pseudo.helper.utils.ThrowingFunction;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEObjectJSON;
import com.nimbusds.jose.UnprotectedHeader;
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
import java.util.Objects;
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
  private static final String[] EMPTY_STRING_ARRAY = new String[0];
  private static final String KID_PROBLEM_W_DOMAIN = "Failed to decrypt the secret key with kid `{}` of the domain `{}`. " +
                                                     "The response from eHealth was\n{}";

  private final URI jwksUrl;
  private final Supplier<CompletableFuture<String>> jwksSupplier;
  private final PseudonymisationClient pseudonymisationClient;
  private final PrivateKeySupplier privateKeySupplier;
  private final SecureRandom secureRandom;
  private final ConcurrentHashMap<String, CompletableFuture<DomainImpl>> domains;
  private final ConcurrentHashMap<String, CompletableFuture<DomainImpl>> previousDomains;
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
    initJwksSilently();
    Stream.of(Map.entry("jwksSupplier", Optional.ofNullable(jwksSupplier)),
              Map.entry("jwksUrl", Optional.ofNullable(jwksUrl)))
          .filter(property -> property.getValue().isEmpty())
          .forEach(property -> {
            log.warn("`{}` is null: this PseudonymisationHelper will not be able to encrypt or decrypt any transit info", property.getKey());
          });
    domains = new ConcurrentHashMap<>(8, 0.75f, 8);
    previousDomains = new ConcurrentHashMap<>(8, 0.75f, 8);
  }

  /**
   * Retrieves a {@link CompletableFuture} wrapping a {@link Domain} object associated with the specified domain key.
   * If the domain is not available or encounters issues like unfinished initialization, completion with exceptions,
   * or cancellation, this method will return the previous version of the domain.
   * <p>
   * Please note that if the {@link CompletableFuture} did not complete successfully, no automatic refresh will be attempted.
   *
   * @param domainKey the unique key identifying the domain to be retrieved
   * @return a {@link CompletableFuture} containing the {@link Domain} object associated with the provided key
   */
  public CompletableFuture<? extends Domain> getDomain(final String domainKey) {
    final var domain = domains.get(domainKey);
    if (domain == null) {
      return refreshDomain(domainKey);
    }
    if (domain.isDone() && !domain.isCompletedExceptionally() && !domain.isCancelled()) {
      return domain;
    }
    return previousDomains.getOrDefault(domainKey, domain);
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

  /**
   * Refreshes the specified domain by retrieving its details from the eHealth pseudonymisation service,
   * creating a new domain object, and updating the domain cache.
   * <p>
   * Please call the {@code get()} method on the returned {@link CompletableFuture} to check that the refresh was successful.
   *
   * @param domainKey the unique key identifying the domain to be refreshed
   * @return a {@link CompletableFuture} containing the refreshed domain object
   */
  public CompletableFuture<? extends Domain> refreshDomain(final String domainKey) {
    var domain = domains.get(domainKey);
    // The first time the domain is asked, we create a new CompletableFuture
    if (domain == null) {
      return domains.computeIfAbsent(domainKey, this::domainCompletableFuture);
    }
    // If the domain is still being initialized, we return the same CompletableFuture
    if (!domain.isDone()) {
      return domain;
    }
    // If the domain completed successfully (not exceptionally or canceled),
    // store it in previousDomains map to keep a usable domain to return
    if (!domain.isCompletedExceptionally() && !domain.isCancelled()) {
      previousDomains.put(domainKey, domain);
    }
    final var newDomain = domainCompletableFuture(domainKey);
    final var replaced = domains.replace(domainKey, domain, newDomain);
    // If the domain was successfully replaced, return the new domain
    // Otherwise return the current domain from the map (which may have been updated by another thread)
    return replaced ? newDomain : domains.get(domainKey);
  }

  /**
   * Retrieves a {@link CompletableFuture} that, when completed, provides a {@link DomainImpl} object
   * corresponding to the specified domain key. The domain information is fetched using the
   * pseudonymisation client and processed to create the {@link DomainImpl} instance.
   *
   * @param domainKey the unique key identifying the domain to be retrieved
   * @return a {@link CompletableFuture} containing the {@link DomainImpl} object for the given domain key
   */
  private CompletableFuture<DomainImpl> domainCompletableFuture(final String domainKey) {
    return pseudonymisationClient
               .getDomain(domainKey)
               .orTimeout(5, SECONDS)
               .thenApply(this::createDomain);
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
      // If we are an owner of the domain, we decrypt all the secret keys
      //noinspection unchecked
      if (jku != null && jwksSupplier != null && ((List<String>) parsedEHealthDomain.get("jku")).contains(jku)) {
        isKnownJku = true;
        for (final Map<String, Object> secretKey : secretKeysFromEHealth) {
          try {
            final var kid = (String) secretKey.get("kid");
            @SuppressWarnings("unchecked")
            final var parsedJwe = JWEObjectJSON.parse((Map<String, Object>) secretKey.get("encoded"));
            final var jweKey = getJweKey(parsedJwe, jku);
            if (jweKey != null) {
              final var privateKey = privateKeySupplier.getByHash(jweKey.getX509CertSHA256Thumbprint().toString());
              parsedJwe.decrypt(new MultiDecrypter(new RSAKey.Builder(jweKey.toRSAKey()).privateKey(privateKey).build()));
              final var jwk = JWK.parse(parsedJwe.getPayload().toString());
              final var algName = jwk.getAlgorithm().getName();
              secretKeys.put(kid, ((OctetSequenceKey) jwk).toSecretKey(algName));
              if (TRUE.equals(secretKey.get("active"))) {
                activeKid = kid;
                activeKeyAlgorithm = EncryptionMethod.parse(algName);
              }
            } else {
              log.error(KID_PROBLEM_W_DOMAIN, kid, domainKey, rawDomain);
            }
          } catch (final ParseException e) {
            log.error("An error occurred when processing the domain `{}`. Response from eHealth was\n{}", domainKey, rawDomain);
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
          // Synchronized block ensures that if 2 domains are refreshed at the same time,
          synchronized (refreshableDomains) {
            unmodifiableCopyOfRefreshableDomains = Set.of(refreshableDomains.toArray(EMPTY_STRING_ARRAY));
          }
        }
      }
      return domain;
    } catch (final Exception e) {
      return throwWrapped(e);
    }
  }

  /**
   * Retrieves a JSON Web Key (JWK) matching the specified JSON Web Encryption (JWE) object and the JSON Web Key Set URL (JKU).
   * The method identifies the appropriate key by examining the "unprotected headers" of the recipients in the parsed JWE object
   * and finding a key in the JWKS that matches the key IDs (kids) associated with the given JKU.
   * If no matching key is found initially, the JWKS is refreshed and the search is performed again.
   *
   * @param parsedJwe The parsed JWE object containing recipients' headers and encryption data.
   * @param jku       The JSON Web Key Set URL used to match recipient key IDs to those in the JWK set.
   * @return The matching JWK if found, or null if no matching key is available.
   */
  private JWK getJweKey(final JWEObjectJSON parsedJwe, final String jku) {
    final var kids = parsedJwe.getRecipients().stream()
                              .map(JWEObjectJSON.Recipient::getUnprotectedHeader)
                              .filter(Objects::nonNull)
                              .filter(unprotectedHeader -> jku.equals(unprotectedHeader.getParam("jku")))
                              .map(UnprotectedHeader::getKeyID)
                              .filter(Objects::nonNull)
                              .collect(toList());
    if (kids.isEmpty()) {
      log.error("No valid recipient found in the domain's secret key for the JKU: {}", jku);
      return null;
    }
    var jwks = getJwks();
    var optionalJweKey = findMatchingJwk(jwks, kids);
    if (optionalJweKey.isEmpty()) {
      flagJwksForRefresh();
      jwks = getJwks();
      optionalJweKey = findMatchingJwk(jwks, kids);
    }
    if (optionalJweKey.isEmpty()) {
      log.error("No JWK with kids in {} found in the JKU `{}`", kids, jku);
      return null;
    }
    return optionalJweKey.get();
  }

  private Optional<JWK> findMatchingJwk(final JWKSet jwks, final List<String> kids) {
    return kids.stream().map(jwks::getKeyByKeyId).filter(Objects::nonNull).findAny();
  }

  /**
   * Retrieves the JSON Web Key Set (JWKS).
   * If the process is interrupted, or an execution error occurs,
   * the JWKS is flagged for refresh, and the cause of the error is rethrown.
   *
   * @return the current {@link JWKSet}
   */
  private JWKSet getJwks() {
    try {
      return jwkSet.get();
    } catch (final InterruptedException e) {
      flagJwksForRefresh();
      Thread.currentThread().interrupt();
      return throwWrapped(e);
    } catch (final ExecutionException e) {
      flagJwksForRefresh();
      return throwWrapped(e.getCause());
    }
  }

  /**
   * Flag the JWK set for refresh.
   * <p>
   * This method will reinitialize the {@code CompletableFuture<JWKSet>} to force the retrieve of the JWK set.
   * This method will be automatically called every time there is an unknown key ID in your recipient of one of the JWE defined in the domain.
   * Despite this, it is recommended to call this method as soon as you know that an update has been made to the JWKS.
   * <p>
   * It is up to you to cache or not the string returned by `jwksSupplier`.
   * <p>
   * If you did not provide any `jwksSupplier`, this method has no effect.
   */
  public void flagJwksForRefresh() {
    if (jwksSupplier != null) {
      jwkSet = jwksSupplier.get().orTimeout(5, SECONDS).thenApply(ThrowingFunction.sneaky(JWKSet::parse));
    }
  }

  private void initJwksSilently() {
    if (jwksSupplier == null) {
      jwkSet = failedFuture(new NullPointerException("`jwksSupplier` cannot be null if you need to encrypt/decrypt transit info"));
      return;
    }
    flagJwksForRefresh();
    try {
      getJwks();
    } catch (Exception e) {
      log.error("Failed to retrieve JWKS", e);
    }
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
