package be.smals.shared.pseudo.helper;

public interface Point {

  /**
   * Get the domain that owns this point.
   *
   * @return the domain that owns this point
   */
  Domain domain();

  /**
   * Returns binary representation of the X coordinate (as a byte array converted in a Base64 String).
   *
   * @return binary representation of the X coordinate (as a byte array converted in a Base64 String)
   */
  String x();

  /**
   * Returns binary representation of the Y coordinate (as a byte array converted in a Base64 String).
   *
   * @return binary representation of the Y coordinate (as a byte array converted in a Base64 String)
   */
  String y();
}
