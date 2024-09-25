package be.smals.shared.pseudo.helper;

import be.smals.shared.pseudo.helper.exceptions.InvalidPseudonymException;
import java.util.Collection;

/**
 * Allows to create {@link PseudonymInTransit} for a {@link Domain}.
 */
public interface PseudonymInTransitFactory {

  // tag::methods[]
  /**
   * Creates a {@link PseudonymInTransit} from the given coordinates, and transit info.
   *
   * @param x           Base64 string representation of the X coordinate.
   * @param y           Base64 string representation of the Y coordinate.
   * @param transitInfo the standard JWE compact representation (Base64 URL encoded String) of the transitInfo
   *                    which contains the scalar that will be used to unblind the given {@link Pseudonym}.
   * @return A {@link PseudonymInTransit} created from the given coordinates and transit info
   */
  PseudonymInTransit fromXYAndTransitInfo(final String x, final String y, final String transitInfo) throws InvalidPseudonymException;

  /**
   * Creates a {@link PseudonymInTransit} from the given SEC 1 representation of the elliptic curve point and transit info.
   *
   * @param sec1AndTransitInfo Base64 URL string representation (without padding) of the SEC 1 encoded point
   *                           (can be SEC 1 compressed or uncompressed format),
   *                           followed by {@code :},
   *                           and by the standard JWE compact representation (Base64 URL encoded String) of the transitInfo
   *                           which contains the scalar that will be used to unblind the given point coordinates (pseudonym).
   * @return A {@link PseudonymInTransit} created from the given {@code sec1AndTransitInfo}
   * @throws InvalidPseudonymException if the format of the given {@code sec1AndTransitInfo} is invalid
   */
  PseudonymInTransit fromSec1AndTransitInfo(final String sec1AndTransitInfo) throws InvalidPseudonymException;

  /**
   * Create an empty {@link MultiplePseudonymInTransit}.
   *
   * @return an empty {@link MultiplePseudonymInTransit}.
   */
  @SuppressWarnings("unused")
  MultiplePseudonymInTransit multiple();

  /**
   * Create a {@link MultiplePseudonymInTransit} containing the items of the given {@link Collection}.
   * <p>
   * The items (references) of the given collection are copied to returned {@link MultiplePseudonymInTransit}.
   * Changes done on the collection will not be reflected on the returned {@link MultiplePseudonymInTransit}.
   *
   * @param pseudonymsInTransit {@link Collection} of items to copy in the returned {@link MultiplePseudonymInTransit}
   * @return a {@link MultiplePseudonymInTransit} containing the items of the given {@link Collection}
   */
  @SuppressWarnings("unused")
  MultiplePseudonymInTransit multiple(Collection<PseudonymInTransit> pseudonymsInTransit);
  // end::methods[]
}
