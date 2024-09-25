package be.smals.shared.pseudo.helper.exceptions;

import be.smals.shared.pseudo.helper.utils.ExceptionUnwrapper;

/**
 * Wrap any {@link Throwable}, except {@link RuntimeException}.
 */
public class ThrowableWrapperException extends RuntimeException {

  private final Throwable wrappedThrowable;

  /**
   * Create a new {@link ThrowableWrapperException}.
   *
   * @param throwable The throwable to wrap. Cannot be a {@link RuntimeException}.
   * @throws IllegalArgumentException if the given throwable is a {@link RuntimeException}.
   */
  public ThrowableWrapperException(final Throwable throwable) throws IllegalArgumentException {
    super(throwable.getMessage(), throwable);
    if (throwable instanceof RuntimeException) {
      throw new IllegalArgumentException("The given throwable cannot be a RuntimeException");
    }
    wrappedThrowable = throwable;
  }

  /**
   * Returns the wrapped {@link Throwable}.
   *
   * @return the wrapped {@link Throwable}.
   */
  public Throwable wrappedThrowable() {
    return wrappedThrowable;
  }

  /**
   * Calls {@link ExceptionUnwrapper#unwrap(ThrowableWrapperException)} and returns the first unwrappable {@link Exception} from the {@link Exception} hierarchy.
   *
   * @return the unwrapped {@link Exception}.
   * @see ExceptionUnwrapper#unwrap(ThrowableWrapperException)
   */
  @SuppressWarnings("unused")
  public Throwable unwrap() {
    return ExceptionUnwrapper.unwrap(this);
  }

  /**
   * Convenient method to throw the given throwable as a {@link ThrowableWrapperException}.
   * <p>
   * Please note that if the given throwable is a {@link RuntimeException}, it will be rethrown as-is (not wrapped).
   * <p>
   * This method will always throw an exception: the return type is only there for convenience.
   *
   * @param throwable The throwable to wrap.
   * @param <T>       the expected type. Defined only to allow {@code return throwWrapped(e);}.
   * @return the expected type. Defined only to allow {@code return throwWrapped(e);}.
   */
  public static <T> T throwWrapped(final Throwable throwable) throws ThrowableWrapperException {
    if (throwable instanceof RuntimeException) {
      throw (RuntimeException) throwable;
    }
    throw new ThrowableWrapperException(throwable);
  }
}
