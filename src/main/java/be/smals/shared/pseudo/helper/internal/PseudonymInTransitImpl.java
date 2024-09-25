package be.smals.shared.pseudo.helper.internal;

import be.smals.shared.pseudo.helper.Domain;
import be.smals.shared.pseudo.helper.Pseudonym;
import be.smals.shared.pseudo.helper.PseudonymInTransit;
import be.smals.shared.pseudo.helper.TransitInfo;
import be.smals.shared.pseudo.helper.exceptions.InvalidTransitInfoException;
import be.smals.shared.pseudo.helper.exceptions.UnknownKidException;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class PseudonymInTransitImpl extends PseudonymImpl implements PseudonymInTransit {

  private final TransitInfoImpl transitInfo;
  private Pseudonym decryptedPseudonym;

  public PseudonymInTransitImpl(final Pseudonym pseudonym, final TransitInfo transitInfo, final Pseudonym decryptedPseudonym) {
    super(((PseudonymImpl) pseudonym).ecPoint, ((PseudonymImpl) pseudonym).domain);
    this.transitInfo = (TransitInfoImpl) transitInfo;
    this.decryptedPseudonym = decryptedPseudonym;
  }

  public PseudonymInTransitImpl(final Pseudonym pseudonym, final TransitInfo transitInfo) {
    this(pseudonym, transitInfo, null);
  }

  @Override
  public TransitInfoImpl transitInfo() {
    return transitInfo;
  }

  @Override
  public String asString() {
    return sec1() + ":" + transitInfo.asString();
  }

  @Override
  public String asShortString() {
    return sec1Compressed() + ":" + transitInfo.asString();
  }

  @Override
  public CompletableFuture<ValueImpl> identify() {
    final var random = domain.createRandom();
    final var blindedPseudonym = multiply(random);
    final var payload = domain.createPayloadString(blindedPseudonym, transitInfo().asString());
    return domain.pseudonymisationClient().identify(domain.key(), payload)
                 .thenApply(rawResponse -> {
                   final var valueAsPseudonym = domain.pseudonymFactory().fromRawResponse(rawResponse, random);
                   return new ValueImpl(valueAsPseudonym.ecPoint, domain);
                 });
  }

  @SuppressWarnings("DuplicateThrows")
  @Override
  public Pseudonym atRest() throws InvalidTransitInfoException, UnknownKidException {
    if (decryptedPseudonym != null) {
      return decryptedPseudonym;
    }
    decryptedPseudonym = atRest(true);
    return decryptedPseudonym;
  }

  @SuppressWarnings("DuplicateThrows")
  @Override
  public Pseudonym atRest(final boolean validateIatAndExp) throws InvalidTransitInfoException, UnknownKidException {
    final var payload = transitInfo.payload();
    if (validateIatAndExp) {
      transitInfo.validatePayload();
    }
    final var scalar = new BigInteger(Base64.getDecoder().decode((String) payload.get("scalar")));
    return new PseudonymImpl(ecPoint.multiply(scalar).normalize(), domain);
  }

  @Override
  public CompletableFuture<PseudonymInTransitImpl> convertTo(final Domain toDomain) {
    final var random = domain.createRandom();
    final var blindedPseudonym = multiply(random);
    final var payload = domain.createPayloadString(blindedPseudonym, transitInfo.asString());
    return domain.pseudonymisationClient().convertTo(domain.key(), toDomain.key(), payload)
                 .thenApply(s -> ((PseudonymInTransitFactoryImpl) toDomain.pseudonymInTransitFactory()).fromRawResponse(s, random));
  }

  @Override
  public PseudonymInTransitImpl inTransit() {
    return this;
  }

  @Override
  PseudonymInTransitImpl multiply(final BigInteger scalar) {
    return new PseudonymInTransitImpl(super.multiply(scalar), transitInfo);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {return true;}
    if (!(obj instanceof PseudonymInTransitImpl)) {return false;}
    final var that = (PseudonymInTransitImpl) obj;
    return Objects.equals(ecPoint.getXCoord(), that.ecPoint.getXCoord()) &&
           Objects.equals(domain.key(), that.domain.key()) &&
           Objects.equals(this.transitInfo, that.transitInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ecPoint.getXCoord(), domain.key(), transitInfo);
  }

  @Override
  public String toString() {
    return "{" +
           "\"x\": \"" + x() + "\", " +
           "\"y\": \"" + y() + "\"," +
           "\"domain\": \"" + domain.key() + "\"," +
           "\"transitInfo\": " + transitInfo +
           '}';
  }
}
