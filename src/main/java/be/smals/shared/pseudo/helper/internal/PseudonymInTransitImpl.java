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

public final class PseudonymInTransitImpl implements PseudonymInTransit {

  private final PseudonymImpl pseudonym;
  private final TransitInfoImpl transitInfo;
  private Pseudonym decryptedPseudonym;

  public PseudonymInTransitImpl(final Pseudonym pseudonym, final TransitInfo transitInfo, final Pseudonym decryptedPseudonym) {
    this.pseudonym = (PseudonymImpl) pseudonym;
    this.transitInfo = (TransitInfoImpl) transitInfo;
    this.decryptedPseudonym = decryptedPseudonym;
  }

  public PseudonymInTransitImpl(final Pseudonym pseudonym, final TransitInfo transitInfo) {
    this(pseudonym, transitInfo, null);
  }

  @Override
  public Domain domain() {
    return pseudonym.domain;
  }

  @Override
  public String x() {
    return pseudonym.x();
  }

  @Override
  public String y() {
    return pseudonym.y();
  }

  @SuppressWarnings("removal")
  @Override
  public String sec1() {
    return pseudonym.asString();
  }

  @SuppressWarnings("removal")
  @Override
  public String sec1Compressed() {
    return pseudonym.asShortString();
  }

  @Override
  public PseudonymImpl pseudonym() {
    return pseudonym;
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
    final var domain = pseudonym.domain;
    final var random = domain.createRandom();
    final var blindedPseudonym = pseudonym.multiply(random);
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
    return pseudonym.multiply(scalar);
  }

  @Override
  public CompletableFuture<PseudonymInTransitImpl> convertTo(final Domain toDomain) {
    final var domain = pseudonym.domain;
    final var random = domain.createRandom();
    final var blindedPseudonym = pseudonym.multiply(random);
    final var payload = domain.createPayloadString(blindedPseudonym, transitInfo.asString());
    return domain.pseudonymisationClient().convertTo(domain.key(), toDomain.key(), payload)
                 .thenApply(s -> ((PseudonymInTransitFactoryImpl) toDomain.pseudonymInTransitFactory()).fromRawResponse(s, random));
  }

  @SuppressWarnings("removal")
  @Override
  public PseudonymInTransit inTransit() {
    return this;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {return true;}
    if (!(obj instanceof PseudonymInTransitImpl)) {return false;}
    final var that = (PseudonymInTransitImpl) obj;
    return Objects.equals(pseudonym.ecPoint.getXCoord(), that.pseudonym.ecPoint.getXCoord()) &&
           Objects.equals(pseudonym.domain.key(), that.pseudonym.domain.key()) &&
           Objects.equals(this.transitInfo, that.transitInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pseudonym.ecPoint.getXCoord(), pseudonym.domain.key(), transitInfo);
  }

  @Override
  public String toString() {
    return "{" +
           "\"x\": \"" + pseudonym.x() + "\", " +
           "\"y\": \"" + pseudonym.y() + "\"," +
           "\"domain\": \"" + pseudonym.domain.key() + "\"," +
           "\"transitInfo\": " + transitInfo +
           '}';
  }
}
