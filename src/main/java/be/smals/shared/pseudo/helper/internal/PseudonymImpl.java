package be.smals.shared.pseudo.helper.internal;

import be.smals.shared.pseudo.helper.Domain;
import be.smals.shared.pseudo.helper.Pseudonym;
import be.smals.shared.pseudo.helper.TransitInfoCustomizer;
import be.smals.shared.pseudo.helper.Value;
import java.math.BigInteger;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import org.bouncycastle.math.ec.ECPoint;

public class PseudonymImpl extends PointImpl implements Pseudonym {

  private static final Base64.Encoder base64EncoderWithoutPadding = Base64.getUrlEncoder().withoutPadding();
  public static final TransitInfoCustomizer NO_OP_TRANSIT_INFO_CUSTOMIZER = new TransitInfoCustomizer() {};

  public PseudonymImpl(final ECPoint ecPoint, final Domain domain) {
    super(ecPoint, domain);
  }

  @Override
  public String x() {
    return Base64.getEncoder().encodeToString(ecPoint.getXCoord().getEncoded());
  }

  @Override
  public String y() {
    return Base64.getEncoder().encodeToString(ecPoint.getYCoord().getEncoded());
  }

  @Override
  public String asString() {
    return base64EncoderWithoutPadding.encodeToString(ecPoint.getEncoded(false));
  }

  @SuppressWarnings("removal")
  @Override
  public String sec1() {
    return asString();
  }

  @Override
  public String asShortString() {
    return base64EncoderWithoutPadding.encodeToString(ecPoint.getEncoded(true));
  }

  @SuppressWarnings("removal")
  @Override
  public String sec1Compressed() {
    return asShortString();
  }

  @Override
  public CompletableFuture<PseudonymInTransitImpl> convertTo(final Domain toDomain) {
    final var random = domain.createRandom();
    final var payload = domain.createPayloadString(multiply(random));
    return domain.pseudonymisationClient().convertTo(domain.key(), toDomain.key(), payload)
                 .thenApply(s -> ((DomainImpl) toDomain).pseudonymInTransitFactory().fromRawResponse(s, random));
  }

  @Override
  public PseudonymInTransitImpl inTransit() {
    return inTransit(NO_OP_TRANSIT_INFO_CUSTOMIZER);
  }

  @Override
  public PseudonymInTransitImpl inTransit(final TransitInfoCustomizer transitInfoCustomizer) {
    final var random = domain.createRandom();
    final var randomModInverse = random.modInverse(ecPoint.getCurve().getOrder());
    final var blinded = new PseudonymImpl(ecPoint.multiply(randomModInverse).normalize(), domain);
    final var transitInfo = new TransitInfoImpl(domain, random, transitInfoCustomizer);
    return new PseudonymInTransitImpl(blinded, transitInfo, this);
  }

  PseudonymImpl multiply(final BigInteger scalar) {
    return new PseudonymImpl(ecPoint.multiply(scalar).normalize(), domain);
  }

  PseudonymImpl multiplyByModInverse(final BigInteger scalar) {
    return multiply(scalar.modInverse(ecPoint.getCurve().getOrder()));
  }

  /**
   * Returns this {@link Pseudonym} as a {@link Value}.
   * <p>
   * Only use it with an identified {@link Pseudonym}.
   *
   * @return this {@link Pseudonym} as a {@link Value}
   */
  ValueImpl asValue() {
    return new ValueImpl(ecPoint, domain);
  }
}
