package com.axonivy.market.util;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

@Component
public class MultiTaskUtils {

  private final Executor executor;

  public MultiTaskUtils(@Qualifier("sharedVirtualThreadExecutor") Executor executor) {
    this.executor = executor;
  }

  public <T, R> List<R> parallelProcessWithLimit(Collection<T> items, Function<T, R> task, int maxConcurrency) {
    // Use the shared Spring-managed executor; the semaphore is only for per-call throttling.
    Semaphore semaphore = new Semaphore(maxConcurrency);
    List<CompletableFuture<R>> futures = items.stream()
        .map(item -> CompletableFuture.supplyAsync(() -> processWithSemaphore(item, task, semaphore), executor))
        .toList();

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
        .thenApply(v -> futures.stream().map(CompletableFuture::join).toList())
        .join();
  }

  private static <T, R> R processWithSemaphore(T item, Function<T, R> task, Semaphore semaphore) {
    boolean isAcquired = false;
    try {
      semaphore.acquire();
      isAcquired = true;
      return task.apply(item);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    } finally {
      if (isAcquired) {
        semaphore.release();
      }
    }
  }
}
