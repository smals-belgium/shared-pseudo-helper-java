package be.smals.shared.pseudo.helper.internal;

import static be.smals.shared.pseudo.helper.internal.PointImpl.computeY;
import static java.util.Objects.requireNonNull;

import be.smals.shared.pseudo.helper.Domain;
import be.smals.shared.pseudo.helper.Pseudonym;
import be.smals.shared.pseudo.helper.PseudonymFactory;
import be.smals.shared.pseudo.helper.exceptions.EHealthProblemException;
import be.smals.shared.pseudo.helper.exceptions.InvalidPseudonymException;
import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonObject;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;
import org.bouncycastle.math.ec.ECPoint;

public class PseudonymFactoryImpl extends PointFactory implements PseudonymFactory {

  PseudonymFactoryImpl(final Domain domain) {
    super(domain);
  }

  @Override
  public PseudonymImpl fromX(final String xAsBase64String) throws InvalidPseudonymException {
    final byte[] xAsBytes;
    try {
      xAsBytes = Base64.getDecoder().decode(xAsBase64String);
    } catch (final Exception e) {
      throw new InvalidPseudonymException("The X coordinate is not a valid Base64 string", e);
    }
    final var x = toBigInteger(xAsBytes, "The X coordinate cannot be converted in BigInteger");
    final var y = computeY(domain.curve(), x);
    if (y == null) {
      throw new InvalidPseudonymException("Invalid X coordinate: no Y coordinate can be computed for this X coordinate");
    }
    return new PseudonymImpl(createEcPoint(x, y), domain);
  }

  /**
   * @param xAsBase64String Base64 string representation of the X coordinate.
   * @param yAsBase64String Base64 string representation of the Y coordinate.
   * @return Pseudonym
   * @throws InvalidPseudonymException If the coordinates or the format are invalid.
   */
  @Override
  public PseudonymImpl fromXY(final String xAsBase64String, final String yAsBase64String) throws InvalidPseudonymException {
    return fromXY(xAsBase64String, yAsBase64String, Base64.getDecoder());
  }

  @Override
  public MultiplePseudonymImpl multiple() {
    return new MultiplePseudonymImpl(domain);
  }

  @Override
  public MultiplePseudonymImpl multiple(final Collection<Pseudonym> pseudonyms) {
    requireNonNull(pseudonyms, "`pseudonyms` cannot be null");
    return new MultiplePseudonymImpl(domain, pseudonyms);
  }

  /**
   * @param sec1 Base64 string representation of the SEC 1 encoded point (can be SEC 1 compressed format).
   * @return Pseudonym
   * @throws InvalidPseudonymException If the coordinates or the format are invalid.
   */
  PseudonymImpl fromSec1(final String sec1) throws InvalidPseudonymException {
    assertNotNull(sec1, "The Base64 encoded SEC 1 representation of the point is null");
    final var sec1AsBytes = decodeBase64(sec1, Base64.getUrlDecoder(),
                                         "The Base64 encoded SEC 1 representation of the point is not a valid Base64 URL String");
    final var ecPoint = decodeSec1(sec1AsBytes);
    return new PseudonymImpl(ecPoint, domain);
  }

  private PseudonymImpl fromXY(final String xAsBase64String, final String yAsBase64String, final Base64.Decoder base64Decoder) throws
                                                                                                                               InvalidPseudonymException {
    assertNotEmpty(xAsBase64String, "The Base64 encoded X coordinate is empty or null");
    assertNotEmpty(yAsBase64String, "The Base64 encoded Y coordinate is empty or null");
    final var xAsBytes = decodeBase64(xAsBase64String, base64Decoder, "The Base64 encoded X coordinate is not a valid Base64 String");
    final var yAsBytes = decodeBase64(yAsBase64String, base64Decoder, "The Base64 encoded Y coordinate is not a valid Base64 String");
    final var x = toBigInteger(xAsBytes, "The Base64 encoded X coordinate is not a valid point coordinate");
    final var y = toBigInteger(yAsBytes, "The Base64 encoded Y coordinate is not a valid point coordinate");
    return new PseudonymImpl(createEcPoint(x, y), domain);
  }

  private ECPoint createEcPoint(final BigInteger x, final BigInteger y) throws InvalidPseudonymException {
    try {
      return domain.curve().createPoint(x, y);
    } catch (final Exception e) {
      throw new InvalidPseudonymException("Invalid coordinates", e);
    }
  }

  PseudonymImpl fromRawResponse(final String rawResponse, final BigInteger scalar) throws EHealthProblemException {
    return fromResponse(JSonHelper.parse(rawResponse), scalar);
  }

  PseudonymImpl fromResponse(final JsonObject response, final BigInteger scalar) throws EHealthProblemException {
    if (!isAcceptableResponse(response)) {
      throw new EHealthProblemException(EHealthProblemImpl.fromResponse(response));
    }
    final var domainFromResponse = Optional.ofNullable(response.get("domain"))
                                           .map(JsonElement::getAsString)
                                           .orElseThrow(() -> new RuntimeException("Pseudonym sent by eHealth is invalid: `domain` is missing"));
    if (!domainFromResponse.equals(domain.key())) {
      throw new RuntimeException("Pseudonym sent by eHealth is invalid: `" +
                                 domainFromResponse + "` does not match the expected domain `" + domain.key() + "`");
    }
    try {
      final var blindedPseudonym = fromXY(response.get("x").getAsString(), response.get("y").getAsString());
      return blindedPseudonym.multiplyByModInverse(scalar);
    } catch (final InvalidPseudonymException e) {
      throw new RuntimeException("Pseudonym sent by eHealth is invalid", e);
    }
  }

  boolean isAcceptableResponse(final JsonObject response) {
    return response.has("x") && response.has("y");
  }

  private static void assertNotNull(final Object o, final String exceptionMessage) throws InvalidPseudonymException {
    if (o == null) {
      throw new InvalidPseudonymException(exceptionMessage);
    }
  }

  private static void assertNotEmpty(final String string, final String exceptionMessage) throws InvalidPseudonymException {
    if (string == null || string.isBlank()) {
      throw new InvalidPseudonymException(exceptionMessage);
    }
  }

  private static BigInteger toBigInteger(final byte[] bytes, final String exceptionMessage) throws InvalidPseudonymException {
    try {
      return new BigInteger(bytes);
    } catch (final Exception e) {
      throw new InvalidPseudonymException(exceptionMessage, e);
    }
  }

  private static byte[] decodeBase64(final String string, final Base64.Decoder base64Decoder, final String exceptionMessage)
      throws InvalidPseudonymException {
    try {
      return base64Decoder.decode(string);
    } catch (final Exception e) {
      throw new InvalidPseudonymException(exceptionMessage, e);
    }
  }

  private ECPoint decodeSec1(final byte[] sec1) throws InvalidPseudonymException {
    try {
      return domain.curve().decodePoint(sec1);
    } catch (final Exception e) {
      throw new InvalidPseudonymException("Invalid SEC 1 representation of the point", e);
    }
  }
}
