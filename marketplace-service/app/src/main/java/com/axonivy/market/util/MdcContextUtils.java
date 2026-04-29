package com.axonivy.market.util;

import org.slf4j.MDC;

import java.util.function.Function;

/**
 * Utility for propagating MDC (Mapped Diagnostic Context) across async threads.
 * This solves the MDC context loss issue when using CompletableFuture.supplyAsync().
 */
public final class MdcContextUtils {

  private MdcContextUtils() {}

  /**
   * Wraps a Function to preserve MDC context across async thread boundaries.
   *
   * @param function function that runs on the thread pool
   * @param <T>      input type
   * @param <R>      return type
   * @return wrapped function with MDC context
   */
  public static <T, R> Function<T, R> wrapMdcContext(Function<T, R> function) {
    java.util.Map<String, String> mdcContext = MDC.getCopyOfContextMap();
    return (T t) -> {
      if (mdcContext != null) {
        MDC.setContextMap(mdcContext);
      }
      try {
        return function.apply(t);
      } finally {
        MDC.clear();
      }
    };
  }
}
