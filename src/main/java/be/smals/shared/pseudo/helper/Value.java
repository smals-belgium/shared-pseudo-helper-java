package be.smals.shared.pseudo.helper;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * Wrapper around an elliptic curve point representing a value, that provides useful methods to manipulate it.
 */
@SuppressWarnings("unused")
public interface Value extends Point {

  // tag::methods[]

  /**
   * Returns the value as a bytes array.
   * <p>
   * Use it for non-text values.
   *
   * @return the value as a bytes array
   */
  byte[] asBytes();

  /**
   * Returns the value as a String.
   * <p>
   * Convenient method that converts the bytes array to a String.
   * <p>
   * Use it only for text values for which you called {@link ValueFactory#from(String, Charset)}.
   *
   * @param charset The charset to use to convert the internal bytes array to a String.
   * @return the value as a String
   */
  String asString(final Charset charset);

  /**
   * Returns the value as a String.
   * <p>
   * Convenient method that converts the bytes array (representing UTF-8 characters) to a String.
   * <p>
   * Use it for text values.
   *
   * @return the value as a String
   */
  String asString();

  /**
   * Returns this {@link Value} as a {@link Pseudonym}.
   * <p>
   * Should not be used in regular usage, but it can be convenient for testing/logging purpose.
   *
   * @return this {@link Value} as a {@link Pseudonym}.
   */
  Pseudonym asPseudonym();

  /**
   * Creates a {@link PseudonymInTransit} from this {@link Value} (without pseudonymisation).
   * <p>
   * Use this only for 'pseudo-unaware domains'
   * (i.e., domains where `pseudonymize` and `identify` operations are never used, as the domain owners are authorized to access real values, such as SSIN).
   *
   * @return a {@link PseudonymInTransit} from this {@link Value} (without pseudonymisation).
   */
  PseudonymInTransit asPseudonymInTransit();

  /**
   * Creates a {@link PseudonymInTransit} from this {@link Value} (without pseudonymisation).
   * <p>
   * Use this only for 'pseudo-unaware domains'
   * (i.e., domains where `pseudonymize` and `identify` operations are never used, as the domain owners are authorized to access real values, such as SSIN).
   * <p>
   * The given {@link TransitInfoCustomizer} allows you to add header parameters and payload properties
   * in the {@link TransitInfo} of the returned {@link PseudonymInTransit}.
   *
   * @param transitInfoCustomizer {@link TransitInfoCustomizer} to use
   * @return a {@link PseudonymInTransit} from this {@link Value} (without pseudonymisation).
   */
  PseudonymInTransit asPseudonymInTransit(final TransitInfoCustomizer transitInfoCustomizer);

  /**
   * Pseudonymize this {@link Value}.
   *
   * @return a random {@link PseudonymInTransit} for this {@link Value}.
   */
  CompletableFuture<? extends PseudonymInTransit> pseudonymize();
  // end::methods[]
}
