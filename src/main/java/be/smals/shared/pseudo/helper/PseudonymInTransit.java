package be.smals.shared.pseudo.helper;

import be.smals.shared.pseudo.helper.exceptions.InvalidTransitInfoException;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public interface PseudonymInTransit extends Point {

  /**
   * Base64 URL encoded uncompressed SEC1 Elliptic-Curve-Point-to-Octet-String Conversion of this point.
   *
   * @return Base64 URL encoded the uncompressed SEC1 Elliptic-Curve-Point-to-Octet-String Conversion of this point
   * @deprecated Please call {@link #pseudonym()}.{@link Pseudonym#asString() asString()} instead.
   */
  @Deprecated(forRemoval = true)
  String sec1();

  /**
   * Base64 URL encoded compressed SEC1 Elliptic-Curve-Point-to-Octet-String Conversion of this point.
   *
   * @return Base64 URL encoded the compressed SEC1 Elliptic-Curve-Point-to-Octet-String Conversion of this point
   * @deprecated Please call {@link #pseudonym()}.{@link Pseudonym#asShortString() asString()} instead.
   */
  @Deprecated(forRemoval = true)
  String sec1Compressed();

  // tag::methods[]
  /**
   * Returns the {@link Pseudonym} of this {@link PseudonymInTransit}.
   *
   * @return the {@link Pseudonym} of this {@link PseudonymInTransit}
   */
  Pseudonym pseudonym();

  /**
   * Returns the {@link TransitInfo} of this {@link PseudonymInTransit}.
   *
   * @return the {@link TransitInfo}
   */
  TransitInfo transitInfo();

  /**
   * Returns the standard String representation of this {@link PseudonymInTransit}.
   * <p>
   * It returns the Base64 URL representation of the uncompressed SEC 1 representation of the point
   * followed by `:` and by the String representation of the {@link TransitInfo} (JWE compact).
   * <p>
   * Prefer this method instead of {@link #asShortString()} when the length of the String is not very important,
   * because it avoids the recipient of this {@link PseudonymInTransit} to compute the Y coordinate of the point.
   *
   * @return the standard String representation of this {@link PseudonymInTransit}
   */
  String asString();

  /**
   * Returns the short String representation of this {@link PseudonymInTransit}.
   * <p>
   * It returns the Base64 URL representation of the compressed SEC 1 representation of the point
   * followed by `:` and by the String representation of the {@link TransitInfo} (JWE compact).
   * <p>
   * Only use this method instead of {@link #asString()} when you need to shorten the String (to prevent a too long URL, for example).
   * The drawback is that the recipient of this {@link PseudonymInTransit} will have to compute the Y coordinate of the point.
   *
   * @return the standard String representation of this {@link PseudonymInTransit}
   */
  String asShortString();

  /**
   * Identify (de-pseudonymise) this {@link PseudonymInTransit}.
   *
   * @return the identified {@link Pseudonym} as a {@link Value}.
   */
  CompletableFuture<? extends Value> identify();

  /**
   * Decrypt the pseudonym in transit.
   * <p>
   * {@code iat} and {@code exp} must be valid: it calls {@link #atRest(boolean)} with value {@code true}.
   *
   * @return The pseudonym at rest.
   */
  Pseudonym atRest() throws InvalidTransitInfoException;

  /**
   * Decrypt the pseudonym in transit.
   * <p>
   * In regular case, you should not use this method: you should use {@link #atRest()} instead.
   * Only use this method if you need to recover an expired {@link PseudonymInTransit}, for example.
   *
   * @param validateIatAndExp must {@code iat} and {@code exp} be validated ?
   * @return The pseudonym at rest.
   */
  Pseudonym atRest(boolean validateIatAndExp) throws InvalidTransitInfoException;

  /**
   * Convert this {@link PseudonymInTransit} into a {@link PseudonymInTransit} for the given domain.
   *
   * @param toDomain the target domain for the returned {@link PseudonymInTransit}
   * @return a {@link PseudonymInTransit} for the given domain, matching this {@link PseudonymInTransit}
   */
  CompletableFuture<? extends PseudonymInTransit> convertTo(Domain toDomain);
  // end::methods[]

  /**
   * Returns {@code this} because it is already a {@link PseudonymInTransit}.
   *
   * @return this
   * @deprecated This method returns {@code this}: there is no need to keep it.
   */
  @Deprecated(forRemoval = true)
  PseudonymInTransit inTransit();
}
