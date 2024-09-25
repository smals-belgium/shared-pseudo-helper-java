package be.smals.shared.pseudo.helper.utils;

import be.smals.shared.pseudo.helper.exceptions.ThrowableWrapperException;
import java.util.concurrent.ExecutionException;

public class ExceptionUnwrapper {

  /**
   * Unwrap the given {@link ExecutionException}.
   * <p>
   * This method handle {@link ThrowableWrapperException} and {@link ExecutionException}.
   * While the wrapped {@link Exception} or the cause if on of those {@link Exception},
   * it will continue the unwrap process.
   * <p>
   * If you catch a {@link ExecutionException}, call this method to have the real reason of the problem.
   *
   * @param exception The {@link ExecutionException} to unwrap.
   * @return the unwrapped {@link Exception}
   */
  @SuppressWarnings("unused")
  public static Throwable unwrap(final ExecutionException exception) {
    return unwrap((Exception) exception);
  }

  /**
   * Unwrap the given {@link ThrowableWrapperException}.
   * <p>
   * This method handle {@link ThrowableWrapperException} and {@link ExecutionException}.
   * While the wrapped {@link Exception} or the cause if on of those {@link Exception},
   * it will continue the unwrap process.
   * <p>
   * If you catch a {@link ThrowableWrapperException}, call this method to have the real reason of the problem.
   *
   * @param exception The {@link ThrowableWrapperException} to unwrap.
   * @return the unwrapped {@link Exception}
   */
  public static Throwable unwrap(final ThrowableWrapperException exception) {
    return unwrap((Exception) exception);
  }

  /**
   * Unwrap {@link Exception} recursively.
   * <p>
   * This method handle {@link ThrowableWrapperException} and {@link ExecutionException}.
   * While the wrapped {@link Exception} or the cause if on of those {@link Exception},
   * it will continue the unwrap process.
   * <p>
   * Do the same if you catch an {@link ExecutionException} (for example) and if the cause is a {@link ThrowableWrapperException}.
   *
   * @return the unwrapped {@link Exception}.
   */
  private static Throwable unwrap(final Exception exception) {
    Throwable throwable = exception;
    while (true) {
      if (throwable instanceof ThrowableWrapperException) {
        throwable = ((ThrowableWrapperException) throwable).wrappedThrowable();
      } else if (throwable instanceof ExecutionException) {
        throwable = throwable.getCause();
      } else {
        break;
      }
    }
    return throwable;
  }
}
