package be.smals.shared.pseudo.helper;

import be.smals.shared.pseudo.helper.exceptions.InvalidPseudonymException;
import java.util.Collection;

/**
 * Allows to create {@link Pseudonym} for a {@link Domain}.
 */
@SuppressWarnings("unused")
public interface PseudonymFactory {

  // tag::methods[]
  /**
   * Create a {@link Pseudonym} from the given X coordinate.
   * <p>
   * The Y coordinate will be computed and one of the two possible values will be randomly chosen.
   * The Y coordinate can be chosen randomly because only the X is important in the context of eHealth pseudonymisation.
   *
   * @param xAsBase64String Base64 string representation of the X coordinate.
   * @return a {@link Pseudonym} having the given X coordinate.
   * @throws InvalidPseudonymException If the coordinates or the format are invalid.
   */
  Pseudonym fromX(String xAsBase64String) throws InvalidPseudonymException;

  /**
   * Create a {@link Pseudonym} from the given coordinates.
   *
   * @param xAsBase64String Base64 string representation of the X coordinate.
   * @param yAsBase64String Base64 string representation of the Y coordinate.
   * @return Pseudonym
   * @throws InvalidPseudonymException If the coordinates or the format are invalid.
   */
  Pseudonym fromXY(String xAsBase64String, String yAsBase64String) throws InvalidPseudonymException;

  /**
   * Create an empty {@link MultiplePseudonym}.
   *
   * @return an empty {@link MultiplePseudonym}.
   */
  MultiplePseudonym multiple();

  /**
   * Create a {@link MultiplePseudonym} containing the items of the given {@link Collection}.
   * <p>
   * The items (references) of the given collection are copied to returned {@link MultiplePseudonym}.
   * Changes done on the collection will not be reflected on the returned {@link MultiplePseudonym}.
   *
   * @param pseudonyms {@link Collection} of items to copy in the returned {@link MultiplePseudonym}
   * @return a {@link MultiplePseudonym} containing the items of the given {@link Collection}
   */
  MultiplePseudonym multiple(Collection<Pseudonym> pseudonyms);
  // end::methods[]
}
