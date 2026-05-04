package com.axonivy.market.util;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Objects;

/**
 * Utility to enforce a wall-clock timeout across multi-step operations.
 */
@Log4j2
public final class TimeoutGuard {

  private final long timeoutMillis;
  private final long startedAtMillis;
  private final String operationName;

  private TimeoutGuard(Duration timeout, String operationName) {
    Objects.requireNonNull(timeout, "timeout must not be null");
    this.timeoutMillis = timeout.toMillis();
    this.startedAtMillis = System.currentTimeMillis();
    this.operationName = operationName == null || operationName.isBlank() ? "operation" : operationName;
  }

  public static TimeoutGuard of(Duration timeout, String operationName) {
    return new TimeoutGuard(timeout, operationName);
  }

  public void check(int processed, int total) throws IOException {
    if (Thread.currentThread().isInterrupted()) {
      Thread.currentThread().interrupt();
      throw new InterruptedIOException(operationName + " was interrupted");
    }

    long elapsedMillis = System.currentTimeMillis() - startedAtMillis;
    if (elapsedMillis > timeoutMillis) {
      long elapsedSeconds = Math.max(1, elapsedMillis / 1000);
      if (processed >= 0 && total >= 0) {
        log.warn("Processed {} items of {}", processed, total);
        throw new SocketTimeoutException(operationName + " timed out after " + elapsedSeconds
            + " seconds, processed " + processed + " of " + total + " items");
      }
      throw new SocketTimeoutException(operationName + " timed out after " + elapsedSeconds + " seconds");
    }
  }
}


