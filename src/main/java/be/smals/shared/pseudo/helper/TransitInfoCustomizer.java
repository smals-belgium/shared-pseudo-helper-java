package be.smals.shared.pseudo.helper;

import java.util.Map;

public interface TransitInfoCustomizer {

  /**
   * Returns the protected {@link TransitInfo} header parameters to add to the {@link PseudonymInTransit}.
   * <p>
   * Use this to add any information that recipients who are not able to decrypt the {@link TransitInfo} need to know about this {@link TransitInfo}.
   * <p>
   * Please note that managed params (as {@code aud}, {@code iat} and {@code exp}) will be overridden if you provide them.
   * <p>
   * Warning: the returned {@link Map} cannot be {@code null}!
   *
   * @return the protected {@link TransitInfo} header parameters to add to the {@link PseudonymInTransit}
   */
  default Map<String, Object> header() {
    return Map.of();
  }

  /**
   * Returns the private {@link TransitInfo} payload properties to add in the {@link PseudonymInTransit}.
   * <p>
   * Use this to add any that recipients who are able to decrypt the {@link TransitInfo} need to know about this {@link TransitInfo}.
   * <p>
   * Please note that managed properties ({@code iat}, {@code exp} and {@code scalar}) will be overridden if you provide them.
   * <p>
   * Warning: the returned {@link Map} cannot be {@code null}!
   *
   * @return the private {@link TransitInfo} payload properties to add in the {@link PseudonymInTransit}
   */
  default Map<String, Object> payload() {
    return Map.of();
  }
}
