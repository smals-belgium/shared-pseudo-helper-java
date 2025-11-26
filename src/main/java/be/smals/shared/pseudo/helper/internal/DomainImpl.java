package be.smals.shared.pseudo.helper.internal;

import static java.math.BigInteger.ZERO;
import static java.util.UUID.randomUUID;

import be.smals.shared.pseudo.helper.Domain;
import be.smals.shared.pseudo.helper.Pseudonym;
import be.smals.shared.pseudo.helper.PseudonymInTransit;
import be.smals.shared.pseudo.helper.PseudonymisationClient;
import be.smals.shared.pseudo.helper.TransitInfo;
import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonPrimitive;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Map;
import javax.crypto.SecretKey;
import org.bouncycastle.math.ec.ECCurve;

public class DomainImpl implements Domain {

  private final String key;
  private final String crv;
  private final ECCurve curve;
  private final String audience;
  private final int bufferSize;
  private final Map<String, SecretKey> secretKeys;
  private final String activeKid;
  private final EncryptionMethod activeKeyEncryptionMethod;
  private final Duration inTransitTtl;
  private final PseudonymisationClient pseudonymisationClient;
  private final ValueFactoryImpl valueFactory;
  private final PseudonymFactoryImpl pseudonymFactory;
  private final PseudonymInTransitFactoryImpl pseudonymInTransitFactory;
  private final SecureRandom secureRandom;

  public DomainImpl(final String key,
                    final String crv,
                    final ECCurve curve,
                    final String audience,
                    final int bufferSize,
                    final Map<String, SecretKey> secretKeys,
                    final String activeKid,
                    final EncryptionMethod activeKeyEncryptionMethod,
                    final Duration inTransitTtl,
                    final PseudonymisationClient pseudonymisationClient,
                    final SecureRandom secureRandom) {
    this.key = key;
    this.crv = crv;
    this.curve = curve;
    this.audience = audience;
    this.bufferSize = bufferSize;
    this.secretKeys = secretKeys;
    this.activeKid = activeKid;
    this.activeKeyEncryptionMethod = activeKeyEncryptionMethod;
    this.inTransitTtl = inTransitTtl;
    this.pseudonymisationClient = pseudonymisationClient;
    this.valueFactory = new ValueFactoryImpl(this);
    this.pseudonymFactory = new PseudonymFactoryImpl(this);
    this.pseudonymInTransitFactory = new PseudonymInTransitFactoryImpl(this);
    this.secureRandom = secureRandom;
  }

  @Override
  public String key() {
    return key;
  }

  ECCurve curve() {
    return curve;
  }

  String audience() {
    return audience;
  }

  int bufferSize() {
    return bufferSize;
  }

  Map<String, SecretKey> secretKeys() {
    return secretKeys;
  }

  /**
   * Returns the active kid.
   *
   * @return the kid of the secret key to use to encrypt {@link TransitInfo} or {@code null} if there is no active kid
   * (should not happen in regular case).
   */
  String activeKid() {
    return activeKid;
  }

  EncryptionMethod activeKeyEncryptionMethod() {
    return activeKeyEncryptionMethod;
  }

  Duration inTransitTtl() {
    return inTransitTtl;
  }

  PseudonymisationClient pseudonymisationClient() {
    return pseudonymisationClient;
  }

  String createPayloadString(final Pseudonym pseudonym) {
    return createPayload(pseudonym, null).toString();
  }

  String createPayloadString(final Pseudonym pseudonym, final String transitInfo) {
    return createPayload(pseudonym, transitInfo).toString();
  }

  JsonObject createPayload(final Pseudonym pseudonym) {
    return createPayload(pseudonym, pseudonym instanceof PseudonymInTransit ? ((PseudonymInTransit) pseudonym).transitInfo().asString() : null);
  }

  JsonObject createPayload(final Pseudonym pseudonym, final String transitInfo) {
    final var payload = new JsonObject();
    payload.add("id", new JsonPrimitive(randomUUID().toString()));
    payload.add("crv", new JsonPrimitive(crv));
    payload.add("x", new JsonPrimitive(pseudonym.x()));
    payload.add("y", new JsonPrimitive(pseudonym.y()));
    if (transitInfo != null) {
      payload.add("transitInfo", new JsonPrimitive(transitInfo));
    }
    return payload;
  }

  BigInteger createRandom() {
    BigInteger random;
    // 1 is excluded to prevent no-op blinding
    // P521.getOrder() is excluded to prevent `INF` (infinite) result
    // Not sure those checks are necessary because I guess BouncyCastle already does it
    final var curveOrder = curve.getOrder();
    do {
      random = curve.randomFieldElementMult(secureRandom).toBigInteger();
    } while (random.equals(ZERO) || random.equals(curveOrder));
    return random;
  }

  @Override
  public ValueFactoryImpl valueFactory() {
    return valueFactory;
  }

  @Override
  public PseudonymFactoryImpl pseudonymFactory() {
    return pseudonymFactory;
  }

  @Override
  public PseudonymInTransitFactoryImpl pseudonymInTransitFactory() {
    return pseudonymInTransitFactory;
  }
}
