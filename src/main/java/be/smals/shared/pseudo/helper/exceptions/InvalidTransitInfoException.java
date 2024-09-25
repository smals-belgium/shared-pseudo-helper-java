package be.smals.shared.pseudo.helper.exceptions;

public class InvalidTransitInfoException extends RuntimeException {

  public InvalidTransitInfoException(final String message) {
    super(message);
  }

  public InvalidTransitInfoException(final String message, final Exception e) {
    super(message, e);
  }
}
