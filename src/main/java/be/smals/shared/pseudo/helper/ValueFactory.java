package be.smals.shared.pseudo.helper;

import be.smals.shared.pseudo.helper.exceptions.InvalidValueException;
import java.nio.charset.Charset;
import java.util.Collection;

/**
 * Allows to create {@link Value} for a {@link Domain}.
 */
@SuppressWarnings("unused")
public interface ValueFactory {

  /**
   * Returns the maximum size of the value (as bytes) that can be converted in a Point.
   * <p>
   * Please note that this is the maximum theoretical size. eHealth asks us not to pseudonymise data with a size exceeding 32 bytes.
   *
   * @return the maximum size of the value.
   */
  int getMaxValueSize();

  // tag::methods[]
  /**
   * Creates a {@link Value} from the given array of bytes.
   *
   * @param value Raw value to convert to {@link Value}.
   * @return the {@link Value} for the given array of bytes
   * @throws InvalidValueException If the value cannot be converted to a {@link Value} (if the value is too long).
   */
  Value from(byte[] value) throws InvalidValueException;

  /**
   * Creates a {@link Value} from the given String.
   * <p>
   * The string will be internally converted to an array of bytes using the given {@link Charset}.
   * <p>
   * Use it for strings on which you want to control which {@link Charset} must be used to convert this string into bytes.
   * The main advantage is to limit the number of bytes used if you use a single-byte character set (like ISO 8859-1 (Latin-1), Windows-1252 or ASCII).
   * Keep in mind that eHealth asks not to pseudonymise data with a length exceeding 32 bytes.
   *
   * @param value String to convert to {@link Value}.
   * @return the {@link Value} for the given array of bytes
   * @throws InvalidValueException If the value cannot be converted to a Value (if the value is too long).
   */
  Value from(final String value, final Charset charset) throws InvalidValueException;

  /**
   * Creates a {@link Value} from the given String.
   * <p>
   * The string will be internally converted to an array of bytes using UTF-8 {@link Charset}.
   *
   * @param value String to convert to {@link Value}.
   * @return the {@link Value} for the given array of bytes
   * @throws InvalidValueException If the value cannot be converted to a Value (if the value is too long).
   */
  Value from(final String value) throws InvalidValueException;

  /**
   * Create an empty {@link MultipleValue}.
   *
   * @return an empty {@link MultipleValue}.
   */
  MultipleValue multiple();

  /**
   * Create a {@link MultipleValue} containing the items of the given {@link Collection}.
   * <p>
   * The items (references) of the given collection are copied to returned {@link MultipleValue}.
   * Changes done on the collection will not be reflected on the returned {@link MultipleValue}.
   *
   * @param values {@link Collection} of items to copy in the returned {@link MultipleValue}
   * @return a {@link MultipleValue} containing the items of the given {@link Collection}
   */
  MultipleValue multiple(Collection<Value> values);
  // end::methods[]
}
