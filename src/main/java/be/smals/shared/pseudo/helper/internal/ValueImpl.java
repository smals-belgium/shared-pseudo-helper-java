package be.smals.shared.pseudo.helper.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import be.smals.shared.pseudo.helper.Domain;
import be.smals.shared.pseudo.helper.Value;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import org.bouncycastle.math.ec.ECPoint;

public final class ValueImpl extends PseudonymImpl implements Value {

  public ValueImpl(final ECPoint ecPoint, final Domain domain) {
    super(ecPoint, domain);
  }

  @Override
  public byte[] asBytes() {
    final var x = ecPoint.getXCoord().getEncoded();
    final var valueLengthPos = x.length - domain.bufferSize() - 1;
    final var valueLength = x[valueLengthPos];
    final var startPosition = valueLengthPos - valueLength;
    return Arrays.copyOfRange(x, startPosition, startPosition + valueLength);
  }

  @Override
  public String asString(final Charset charset) {
    final var x = ecPoint.getXCoord().getEncoded();
    final var valueLengthPos = x.length - domain.bufferSize() - 1;
    final var valueLength = x[valueLengthPos];
    final var startPosition = valueLengthPos - valueLength;
    return new String(x, startPosition, valueLength, charset);
  }

  @Override
  public String asString() {
    return asString(UTF_8);
  }

  @Override
  public PseudonymImpl asPseudonym() {
    return this;
  }

  @Override
  public CompletableFuture<PseudonymInTransitImpl> pseudonymize() {
    final var random = domain.createRandom();
    final var blindedValue = new PseudonymImpl(ecPoint.multiply(random).normalize(), domain);
    final var payload = domain.createPayloadString(blindedValue);
    final var pseudonymInTransitFactory = domain.pseudonymInTransitFactory();
    return domain.pseudonymisationClient().pseudonymize(domain.key(), payload)
                 .thenApply(rawResponse -> pseudonymInTransitFactory.fromRawResponse(rawResponse, random));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("Value[");
    String separator = "";
    for (final byte aByte : asBytes()) {
      sb.append(separator).append(aByte);
      separator = ",";
    }
    sb.append("]");
    return sb.toString();
  }
}
