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
   * Pseudonymize this {@link Value}.
   *
   * @return a random {@link PseudonymInTransit} for this {@link Value}.
   */
  CompletableFuture<? extends PseudonymInTransit> pseudonymize();
  // end::methods[]
}
