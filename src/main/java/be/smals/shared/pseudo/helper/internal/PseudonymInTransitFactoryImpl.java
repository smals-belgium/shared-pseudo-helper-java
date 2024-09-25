package be.smals.shared.pseudo.helper.internal;

import be.smals.shared.pseudo.helper.Domain;
import be.smals.shared.pseudo.helper.PseudonymInTransit;
import be.smals.shared.pseudo.helper.PseudonymInTransitFactory;
import be.smals.shared.pseudo.helper.exceptions.EHealthProblemException;
import be.smals.shared.pseudo.helper.exceptions.InvalidPseudonymException;
import com.nimbusds.jose.shaded.gson.JsonObject;
import java.math.BigInteger;
import java.util.Collection;

public class PseudonymInTransitFactoryImpl extends PointFactory implements PseudonymInTransitFactory {

  PseudonymInTransitFactoryImpl(final Domain domain) {
    super(domain);
  }

  @Override
  public PseudonymInTransitImpl fromXYAndTransitInfo(final String x, final String y, final String transitInfo) throws InvalidPseudonymException {
    return new PseudonymInTransitImpl(domain.pseudonymFactory().fromXY(x, y), new TransitInfoImpl(domain, transitInfo));
  }

  @Override
  public PseudonymInTransitImpl fromSec1AndTransitInfo(final String sec1AndTransitInfo) throws InvalidPseudonymException {
    assertNotEmpty(sec1AndTransitInfo);
    final var colonPos = sec1AndTransitInfo.indexOf(':');
    if (colonPos == -1) {
      throw new InvalidPseudonymException("Missing `:` in the pseudonym in transit string. " +
                                          "Format must be {sec1InBase64Url}:{transitInfoInBase64Url}");
    }
    final var pseudonym = domain.pseudonymFactory().fromSec1(sec1AndTransitInfo.substring(0, colonPos));
    final var transitInfo = new TransitInfoImpl(domain, sec1AndTransitInfo.substring(colonPos + 1));
    return new PseudonymInTransitImpl(pseudonym, transitInfo);
  }

  @Override
  public MultiplePseudonymInTransitImpl multiple() {
    return new MultiplePseudonymInTransitImpl(domain);
  }

  @Override
  public MultiplePseudonymInTransitImpl multiple(final Collection<PseudonymInTransit> pseudonymsInTransit) {
    return new MultiplePseudonymInTransitImpl(domain, pseudonymsInTransit);
  }

  PseudonymInTransitImpl fromRawResponse(final String rawResponse, final BigInteger scalar) throws EHealthProblemException {
    return fromResponse(JSonHelper.parse(rawResponse), scalar);
  }

  PseudonymInTransitImpl fromResponse(final JsonObject response, final BigInteger scalar) throws EHealthProblemException {
    if (!isAcceptableResponse(response)) {
      throw new EHealthProblemException(EHealthProblemImpl.fromResponse(response));
    }
    return new PseudonymInTransitImpl(domain.pseudonymFactory().fromResponse(response, scalar),
                                      new TransitInfoImpl(domain, response.get("transitInfo").getAsString()),
                                      null);
  }

  boolean isAcceptableResponse(final JsonObject response) {
    return domain.pseudonymFactory().isAcceptableResponse(response) &&
           response.has("transitInfo");
  }

  private static void assertNotEmpty(final String string) throws InvalidPseudonymException {
    if (string == null || string.isBlank()) {
      throw new InvalidPseudonymException("The pseudonym in transit string is empty or null");
    }
  }
}
