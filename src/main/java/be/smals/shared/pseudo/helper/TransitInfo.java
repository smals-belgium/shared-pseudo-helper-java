package be.smals.shared.pseudo.helper;

import be.smals.shared.pseudo.helper.exceptions.InvalidTransitInfoException;
import be.smals.shared.pseudo.helper.exceptions.UnknownKidException;
import java.util.Map;

/**
 * Transit info containing encrypted information about the {@link PseudonymInTransit}.
 * <p>
 * It contains the encrypted headers {@code iat}, {@code exp} and {@code scalar} which is the scalar to use to "decrypt" the {@link PseudonymInTransit}.
 * <p>
 * It also contains public headers like {@code iat} and {@code exp}.
 */
@SuppressWarnings("unused")
public interface TransitInfo {

  // tag::methods[]
  /**
   * Returns the JWE compact representation of this {@link TransitInfo}.
   *
   * @return the JWE compact representation of this {@link TransitInfo}.
   */
  String asString();

  /**
   * Returns the audience of this {@link TransitInfo}.
   * <p>
   * Basically, it is the URL of the {@link Domain}.
   *
   * @return the audience of this {@link TransitInfo}
   * @throws InvalidTransitInfoException if the transit info String cannot be parsed or is invalid
   */
  String audience() throws InvalidTransitInfoException;

  /**
   * Validate the header of this {@link TransitInfo}.
   *
   * @throws InvalidTransitInfoException if the transit info String cannot be parsed or is invalid
   */
  void validateHeader() throws InvalidTransitInfoException;

  /**
   * Returns a {@link Map} containing the parameters of the header of this {@link TransitInfo}.
   * <p>
   * Changes done on the returned {@link Map} are not reflected to this {@link TransitInfo}.
   *
   * @return a {@link Map} containing the parameters of the header of this {@link TransitInfo}
   */
  Map<String, Object> header();

  /**
   * Returns a {@link Map} containing the payload of this {@link TransitInfo}.
   * <p>
   * Changes done on the returned {@link Map} are not reflected to this {@link TransitInfo}.
   *
   * @return a {@link Map} containing the payload of this {@link TransitInfo}
   * @throws InvalidTransitInfoException if the transit info String cannot be parsed or is invalid
   * @throws UnknownKidException         if the kid used to encrypt this {@link TransitInfo} is unknown
   */
  Map<String, Object> payload() throws InvalidTransitInfoException, UnknownKidException;
  // end::methods[]
}
