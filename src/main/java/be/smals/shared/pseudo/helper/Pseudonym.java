package be.smals.shared.pseudo.helper;

import java.util.concurrent.CompletableFuture;

/**
 * Wrapper around an elliptic curve point that provides useful methods to manipulate eHealth pseudonyms.
 */
@SuppressWarnings("unused")
public interface Pseudonym extends Point {

  // tag::methods[]
  /**
   * Base64 URL encoded uncompressed SEC1 Elliptic-Curve-Point-to-Octet-String Conversion of this point.
   *
   * @return Base64 URL encoded the uncompressed SEC1 Elliptic-Curve-Point-to-Octet-String Conversion of this point
   */
  String asString();

  /**
   * Calls {@link #asString()}.
   *
   * @return String returned by {@link #asString()}
   * @deprecated Please use {@link #asString()} instead.
   */
  @Deprecated(forRemoval = true)
  String sec1();

  /**
   * Compressed SEC 1 representation of this point.
   *
   * @return compressed SEC 1 representation of this point
   */
  String asShortString();

  /**
   * Calls {@link #asShortString()}.
   *
   * @return String returned by {@link #asShortString()}
   * @deprecated Please use {@link #asShortString()} instead.
   */
  @Deprecated(forRemoval = true)
  String sec1Compressed();

  /**
   * Convert this {@link Pseudonym} into a {@link PseudonymInTransit} for the given domain.
   *
   * @param toDomain the target domain for the returned {@link PseudonymInTransit}
   * @return a {@link PseudonymInTransit} for the given domain, matching this {@link Pseudonym}
   */
  CompletableFuture<? extends PseudonymInTransit> convertTo(Domain toDomain);

  /**
   * Create a {@link PseudonymInTransit} from this {@link Pseudonym}.
   * <p>
   * Use this method to convert a pseudonym at rest into a {@link PseudonymInTransit} that you can send externally.
   * <p>
   * The scalar in transitInfo is encoded in Base64.
   *
   * @return a {@link PseudonymInTransit} with X and Y blinded by a scalar (which is encrypted and put in transitInfo)
   */
  PseudonymInTransit inTransit();

  /**
   * Create a {@link PseudonymInTransit} from this {@link Pseudonym}.
   * <p>
   * Use this method to convert a pseudonym at rest into a {@link PseudonymInTransit} that you can send externally.
   * <p>
   * The scalar in transitInfo is encoded in Base64.
   * <p>
   * The given {@link TransitInfoCustomizer} allows you to add header parameters and payload properties
   * in the {@link TransitInfo} of the returned {@link PseudonymInTransit}.
   *
   * @return a {@link PseudonymInTransit} with X and Y blinded by a scalar (which is encrypted and put in transitInfo)
   */
  PseudonymInTransit inTransit(final TransitInfoCustomizer transitInfoCustomizer);
  // end::methods[]
}
