package be.smals.shared.pseudo.helper.internal;

import be.smals.shared.pseudo.helper.Domain;
import be.smals.shared.pseudo.helper.Point;
import be.smals.shared.pseudo.helper.PseudonymInTransit;
import be.smals.shared.pseudo.helper.exceptions.InvalidTransitInfoException;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Objects;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECFieldElement;
import org.bouncycastle.math.ec.ECPoint;

public abstract class PointImpl implements Point {

  protected final ECPoint ecPoint;
  protected final DomainImpl domain;

  protected PointImpl(final ECPoint ecPoint, final Domain domain) {
    this.ecPoint = ecPoint;
    this.domain = (DomainImpl) domain;
  }

  @Override
  public DomainImpl domain() {
    return domain;
  }

  /**
   * Indicates whether some other object is "equal to" this one.
   * <p>
   * This method returns {@code true} if the X coordinate and the domain key of the given {@link Point}
   * are identical to the X coordinate and the domain key of this {@link Point}.
   *
   * @param obj the reference {@link Point} with which to compare.
   * @return {@code true} if this {@link Point} is the same as the obj argument; {@code false} otherwise.
   * @throws InvalidTransitInfoException
   * @see Object#equals
   */
  @SuppressWarnings("JavadocDeclaration")
  public boolean equals(final Object obj) {
    if (obj == this) {return true;}
    if (!(obj instanceof PointImpl)) {return false;}
    if (!Objects.equals(domain.key(), ((PointImpl) obj).domain.key())) {
      return false;
    }
    final var atRest = (obj instanceof PseudonymInTransit)
                       ? (PseudonymImpl) ((PseudonymInTransit) obj).atRest()
                       : this;
    return Objects.equals(ecPoint.getXCoord(), atRest.ecPoint.getXCoord());
  }

  @Override
  public int hashCode() {
    return Objects.hash(ecPoint.getXCoord(), domain.key());
  }

  @Override
  public String toString() {
    final var encoder = Base64.getEncoder();
    return "{" +
           "\"x\": \"" + encoder.encodeToString(ecPoint.getXCoord().getEncoded()) + "\", " +
           "\"y\": \"" + encoder.encodeToString(ecPoint.getYCoord().getEncoded()) + "\"," +
           "\"domain\": \"" + domain.key() + "\"}";
  }

  /**
   * Compute the Y coordinate on the basis of X coordinate.
   * One of the 2 possible Y will be returned: you have no control on which one will be chosen.
   * <p>
   * The code has been copied because the Bouncy Castle method is not public.
   *
   * @param x the X coordinate as BigInteger
   * @return the Y coordinate if a valid Y exists, or {@code null} if it does not exist
   * @see ECCurve.AbstractFp#decompressPoint(int, BigInteger)
   */
  @SuppressWarnings("JavadocReference")
  static BigInteger computeY(final ECCurve curve, final BigInteger x) {
    final ECFieldElement xFieldElement = curve.fromBigInteger(x);
    final ECFieldElement rhs = xFieldElement.square().add(curve.getA()).multiply(xFieldElement).add(curve.getB());
    final ECFieldElement y = rhs.sqrt();
    return y == null ? null : y.toBigInteger();
  }
}
