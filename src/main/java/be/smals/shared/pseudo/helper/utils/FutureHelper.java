package be.smals.shared.pseudo.helper.utils;

import static be.smals.shared.pseudo.helper.exceptions.ThrowableWrapperException.throwWrapped;
import static java.lang.Thread.currentThread;

import be.smals.shared.pseudo.helper.exceptions.ThrowableWrapperException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@SuppressWarnings("unused")
public class FutureHelper {

  /**
   * Calls {@link Future#get()} and wrap the exceptions into an {@link ThrowableWrapperException} to prevent to catch it.
   *
   * @param future The {@link Future} to get
   * @param <T>    The type the given {@link Future} will return
   * @return the result of {@link Future#get()}.
   * @throws ThrowableWrapperException wraps {@link ExecutionException} or {@link InterruptedException}
   */
  public static <T> T getWithoutCheckedException(final Future<T> future) throws ThrowableWrapperException {
    try {
      return future.get();
    } catch (final InterruptedException e) {
      currentThread().interrupt();
      return throwWrapped(e);
    } catch (final ExecutionException e) {
      return throwWrapped(e);
    }
  }
}
