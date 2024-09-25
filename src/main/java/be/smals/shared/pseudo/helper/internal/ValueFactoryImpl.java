package be.smals.shared.pseudo.helper.internal;

import static be.smals.shared.pseudo.helper.internal.PointImpl.computeY;
import static java.math.BigInteger.ONE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import be.smals.shared.pseudo.helper.Domain;
import be.smals.shared.pseudo.helper.MultipleValue;
import be.smals.shared.pseudo.helper.Value;
import be.smals.shared.pseudo.helper.ValueFactory;
import be.smals.shared.pseudo.helper.exceptions.InvalidValueException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Collection;
import org.bouncycastle.math.ec.ECPoint;

public class ValueFactoryImpl extends PointFactory implements ValueFactory {

  private final int maxValueSize;

  public ValueFactoryImpl(final Domain domain) {
    super(domain);
    final var curve = this.domain.curve();
    // maxValueSize = max size for curve - buffer size - 1 byte to store the value length
    maxValueSize = (curve.getFieldSize() / 8) - this.domain.bufferSize() - 1;
  }

  @Override
  public int getMaxValueSize() {
    return maxValueSize;
  }

  @Override
  public ValueImpl from(byte[] value) throws InvalidValueException {

    // Process null value as an empty value
    if (value == null) {
      value = new byte[0];
    } else {
      if (value.length > maxValueSize) {
        throw new InvalidValueException("The value is too long: should be max " + maxValueSize + " bytes");
      }
    }

    final var i = domain.bufferSize();

    // Create a new Byte Array with a length 1 + value.length + 1 + bufferSize
    // The first Byte is set to 0
    final var xBytes = new byte[1 + value.length + 1 + i];

    // Copy the value into xBytes starting at position one
    int position = 1;
    System.arraycopy(value, 0, xBytes, position, value.length);
    position += value.length;
    xBytes[position] = (byte) value.length;

    // Compute the X coordinates by converting the xBytes to a BigInteger
    // Then put the X Coordinate on the elliptic curve
    var xCoordinates = new BigInteger(xBytes);

    // Compute y on the elliptic curve
    var y = computeY(domain.curve(), xCoordinates);
    while (y == null) {
      xCoordinates = xCoordinates.add(ONE);
      y = computeY(domain.curve(), xCoordinates);
    }

    return new ValueImpl(createEcPoint(xCoordinates, y), domain);
  }

  @Override
  public ValueImpl from(final String value, final Charset charset) throws InvalidValueException {
    return from(value == null ? null : value.getBytes(charset));
  }

  @Override
  public ValueImpl from(final String value) throws InvalidValueException {
    return from(value, UTF_8);
  }

  @Override
  public MultipleValue multiple() {
    return new MultipleValueImpl(domain);
  }

  @Override
  public MultipleValue multiple(final Collection<Value> values) {
    requireNonNull(values, "`values` cannot be null");
    return new MultipleValueImpl(domain, values);
  }

  private ECPoint createEcPoint(final BigInteger x, final BigInteger y) throws InvalidValueException {
    try {
      return domain.curve().createPoint(x, y);
    } catch (final Exception e) {
      throw new InvalidValueException("Invalid coordinates", e);
    }
  }
}
