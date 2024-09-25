package be.smals.shared.pseudo.helper.exceptions;

public class InvalidValueException extends RuntimeException {

  public InvalidValueException(final String message) {
    super(message);
  }

  public InvalidValueException(final String message, final Exception e) {
    super(message, e);
  }
}
