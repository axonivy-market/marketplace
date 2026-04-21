package com.axonivy.market.util;

import org.slf4j.MDC;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Utility để propagate MDC (Mapped Diagnostic Context) qua async threads.
 * Giải pháp cho vấn đề MDC mất mát khi chạy CompletableFuture.supplyAsync().
 */
public class MdcContextUtils {

  private MdcContextUtils() {
  }

  /**
   * Wrap Supplier để preserve MDC context qua async thread boundaries.
   * 
   * @param supplier hàm sẽ chạy trên thread pool
   * @param <T> kiểu return value
   * @return wrapped supplier với MDC context
   */
  public static <T> Supplier<T> wrapMdcContext(Supplier<T> supplier) {
    // Copy MDC từ thread hiện tại
    Map<String, String> mdcContext = MDC.getCopyOfContextMap();

    return () -> {
      // Restore MDC ở thread mới
      if (mdcContext != null) {
        MDC.setContextMap(mdcContext);
      }
      try {
        return supplier.get();
      } finally {
        // Clean up MDC ở thread pool để tránh memory leak
        MDC.clear();
      }
    };
  }

  /**
   * Wrap Runnable để preserve MDC context qua async thread boundaries.
   */
  public static Runnable wrapMdcContext(Runnable runnable) {
    Map<String, String> mdcContext = MDC.getCopyOfContextMap();
    
    return () -> {
      if (mdcContext != null) {
        MDC.setContextMap(mdcContext);
      }
      try {
        runnable.run();
      } finally {
        MDC.clear();
      }
    };
  }
}

