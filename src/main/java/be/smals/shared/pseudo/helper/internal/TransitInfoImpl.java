package be.smals.shared.pseudo.helper.internal;

import static be.smals.shared.pseudo.helper.exceptions.ThrowableWrapperException.throwWrapped;
import static com.nimbusds.jose.JWEAlgorithm.DIR;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;

import be.smals.shared.pseudo.helper.Domain;
import be.smals.shared.pseudo.helper.PseudonymInTransit;
import be.smals.shared.pseudo.helper.TransitInfo;
import be.smals.shared.pseudo.helper.TransitInfoCustomizer;
import be.smals.shared.pseudo.helper.exceptions.InvalidTransitInfoException;
import be.smals.shared.pseudo.helper.exceptions.UnknownKidException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.factories.DefaultJWEDecrypterFactory;
import com.nimbusds.jose.shaded.gson.Gson;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class TransitInfoImpl implements TransitInfo {

  private static final Duration CLOCK_SKEW = Duration.of(1, MINUTES);  // as per ehealth spec
  private static final DefaultJWEDecrypterFactory jweDecrypterFactory = new DefaultJWEDecrypterFactory();
  private static final Gson GSON = new Gson();

  private final DomainImpl domain;
  private String raw;
  private JWEObject parsed;

  TransitInfoImpl(final DomainImpl domain, final String raw) {
    this.domain = domain;
    this.raw = raw;
  }

  /**
   * Creates a {@link TransitInfoImpl} and encrypts it immediately.
   * <p>
   * The JWE Algo is DIR and encryption method A256GCM.
   *
   * @param domain     The {@link Domain} to which the {@link PseudonymInTransit} belongs
   * @param scalar     The scalar to use to decrypt the {@link PseudonymInTransit}
   * @param customizer The {@link TransitInfoCustomizer} to use to add custom header parameters or payload properties.
   */
  TransitInfoImpl(final DomainImpl domain, final BigInteger scalar, final TransitInfoCustomizer customizer) {
    final var activeKid = Optional.ofNullable(domain.activeKid())
                                  .orElseThrow(() -> new IllegalStateException(
                                      "Not able to decrypt the active kid of the domain `" + domain.key() + "`. " +
                                      "The domain probably needs to be refreshed."));
    final var secretKey = domain.secretKeys().get(activeKid);
    final var payloadAsMap = createPayload(domain, scalar, customizer.payload());
    final var jweHeader = new JWEHeader.Builder(DIR, domain.activeKeyEncryptionMethod())
                              .keyID(activeKid)
                              .customParams(createHeaderParams(domain, payloadAsMap, customizer.header()))
                              .build();
    final var jweCompact = new JWEObject(jweHeader, new Payload(payloadAsMap));
    // It should never happen if the domain is refreshed often enough
    if (secretKey == null) {
      throw new IllegalArgumentException("SecretKey with kid '" + activeKid + "' not found: " +
                                         "is your user allowed to get secret keys for the domain `" + domain.key() + "`?");
    }
    try {
      jweCompact.encrypt(new DirectEncrypter(secretKey));
    } catch (final JOSEException e) {
      throwWrapped(e);
    }
    this.domain = domain;
    this.parsed = jweCompact;
  }

  @Override
  public String asString() {
    if (raw == null) {
      raw = parsed.serialize();
    }
    return raw;
  }

  @Override
  public String audience() throws InvalidTransitInfoException {
    return (String) parse().getHeader().getCustomParam("aud");
  }

  @Override
  public void validateHeader() throws InvalidTransitInfoException {
    validateTransitInfoHeader(parse().getHeader());
  }

  @Override
  public Map<String, Object> header() {
    return parse().getHeader().toJSONObject();
  }

  /**
   * Returns the decrypted the payload.
   *
   * @return the decrypted the payload
   */
  @SuppressWarnings("DuplicateThrows")
  @Override
  public Map<String, Object> payload() throws InvalidTransitInfoException, UnknownKidException {
    final var parsedTransitInfo = parse();
    var payload = parsedTransitInfo.getPayload();
    if (payload == null) {
      validateHeader();
      final var transitInfoHeader = parsedTransitInfo.getHeader();
      final var secretKey = domain.secretKeys().get(transitInfoHeader.getKeyID());
      if (secretKey == null) {
        throw new UnknownKidException(transitInfoHeader.getKeyID());
      }
      try {
        parsedTransitInfo.decrypt(jweDecrypterFactory.createJWEDecrypter(transitInfoHeader, secretKey));
      } catch (final JOSEException e) {
        throw new InvalidTransitInfoException("Error when decrypting transitInfo", e);
      }
      payload = parsedTransitInfo.getPayload();
    }
    return payload.toJSONObject();
  }

  JWEObject parse() throws InvalidTransitInfoException {
    if (parsed != null) {
      return parsed;
    }
    final JWEObject parsed;
    try {
      parsed = JWEObject.parse(raw);
    } catch (final ParseException e) {
      throw new InvalidTransitInfoException("Error when parsing transitInfo", e);
    }
    final var header = parsed.getHeader();
    if (header.getAlgorithm() != DIR) {
      throw new InvalidTransitInfoException("`alg` with value `dir` expected in header");
    }
    if (header.getEncryptionMethod() == null) {
      throw new InvalidTransitInfoException("Missing `enc` in header");
    }
    if (Objects.toString(header.getCustomParam("aud"), "").isBlank()) {
      throw new InvalidTransitInfoException("Missing `aud` in header");
    }
    this.parsed = parsed;
    return parsed;
  }

  void validateTransitInfoHeader(final JWEHeader transitInfoHeader) throws InvalidTransitInfoException {
    if (!transitInfoHeader.getCustomParam("aud").equals(domain.audience())) {
      throw new InvalidTransitInfoException("Invalid `aud`");
    }
  }

  void validatePayload() throws InvalidTransitInfoException {
    final var payload = payload();
    final long iat = (long) payload.get("iat");
    final long exp = (long) payload.get("exp");
    final var currentTime = now();
    if (Instant.ofEpochSecond(iat).isAfter(currentTime.plus(CLOCK_SKEW))) {
      throw new InvalidTransitInfoException("transitInfo not yet ready for use (iat > now)");
    }
    if (Instant.ofEpochSecond(exp).isBefore(currentTime.minus(CLOCK_SKEW))) {
      throw new InvalidTransitInfoException("expired transitInfo (exp < now)");
    }
  }

  private static Map<String, Object> createPayload(final DomainImpl domain, final BigInteger scalar, final Map<String, Object> customPayload) {
    final var currentTime = now();
    final var computedPayload = new HashMap<String, Object>(customPayload.size() + 3);
    computedPayload.putAll(customPayload);
    computedPayload.put("iat", currentTime.getEpochSecond());
    computedPayload.put("exp", currentTime.plus(domain.inTransitTtl()).getEpochSecond());
    computedPayload.put("scalar", Base64.getEncoder().encodeToString(scalar.toByteArray()));
    return computedPayload;
  }

  private static HashMap<String, Object> createHeaderParams(final DomainImpl domain,
                                                            final Map<String, Object> transitInfo,
                                                            final Map<String, Object> customHeaderParams) {
    final var computedCustomHeaderParams = new HashMap<String, Object>(customHeaderParams.size() + 3, 1F);
    computedCustomHeaderParams.putAll(customHeaderParams);
    computedCustomHeaderParams.put("aud", domain.audience());
    computedCustomHeaderParams.put("iat", transitInfo.get("iat"));
    computedCustomHeaderParams.put("exp", transitInfo.get("exp"));
    return computedCustomHeaderParams;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {return true;}
    if (!(obj instanceof TransitInfoImpl)) {return false;}
    return Objects.equals(this.raw, ((TransitInfoImpl) obj).raw);
  }

  @Override
  public int hashCode() {
    return Objects.hash(raw, parsed);
  }

  /**
   * See {@link Object#toString()}.
   *
   * @throws InvalidTransitInfoException
   */
  @SuppressWarnings("JavadocDeclaration")
  @Override
  public String toString() {
    final var payload = parse().getPayload();
    return "{" +
           "\"header\": " + GSON.toJson(header()) + ", " +
           "\"payload\": " + (payload == null ? "\"not decrypted\"" : GSON.toJson(payload.toJSONObject())) +
           "}";
  }
}
