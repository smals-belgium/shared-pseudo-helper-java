package be.smals.shared.pseudo.helper.utils;

import static be.smals.shared.pseudo.helper.exceptions.ThrowableWrapperException.throwWrapped;
import static java.util.Objects.requireNonNull;

import java.util.function.Function;

/**
 * Represents a function that accepts one argument and returns some value.
 *
 * <p>Function might throw a checked exception instance.</p>
 *
 * @param <T> the type of arguments supplied by this supplier
 * @param <R> the type of results supplied by this supplier
 * @param <E> the type of the thrown checked exception
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, E extends Exception> {

  R apply(T arg) throws E;

  static <T1, R> Function<T1, R> sneaky(final ThrowingFunction<? super T1, ? extends R, ?> function) {
    requireNonNull(function);
    return t -> {
      try {
        return function.apply(t);
      } catch (final Throwable throwable) {
        return throwWrapped(throwable);
      }
    };
  }
}
