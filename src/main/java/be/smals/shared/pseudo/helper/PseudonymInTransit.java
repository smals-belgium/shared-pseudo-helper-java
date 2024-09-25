package be.smals.shared.pseudo.helper;

import be.smals.shared.pseudo.helper.exceptions.InvalidTransitInfoException;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public interface PseudonymInTransit extends Pseudonym {

  // tag::methods[]
  /**
   * Returns the {@link TransitInfo}.
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
   * Returns {@code this} because it is already a {@link PseudonymInTransit}.
   *
   * @return this
   */
  @Override
  PseudonymInTransit inTransit();
  // end::methods[]
}
