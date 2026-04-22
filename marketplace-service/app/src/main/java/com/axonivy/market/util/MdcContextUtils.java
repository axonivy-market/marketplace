package com.axonivy.market.util;

import org.slf4j.MDC;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Utility for propagating MDC (Mapped Diagnostic Context) across async threads.
 * This solves the MDC context loss issue when using CompletableFuture.supplyAsync().
 */
public final class MdcContextUtils {

  private MdcContextUtils() {}

  /**
   * Wraps a Supplier to preserve MDC context across async thread boundaries.
   *
   * @param supplier function that runs on the thread pool
   * @param <T>      return type
   * @return wrapped supplier with MDC context
   */
  public static <T> Supplier<T> wrapMdcContext(Supplier<T> supplier) {
    // Copy MDC from the current thread
    Map<String, String> mdcContext = MDC.getCopyOfContextMap();

    return () -> {
      // Restore MDC in the worker thread
      if (mdcContext != null) {
        MDC.setContextMap(mdcContext);
      }
      try {
        return supplier.get();
      } finally {
        // Clear MDC in pooled threads to avoid context leakage
        MDC.clear();
      }
    };
  }
}
