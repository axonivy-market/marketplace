package com.axonivy.market.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultiTaskUtils {

  public static <T, R> List<R> parallelProcessWithLimit(Collection<T> items, Function<T, R> task, int maxConcurrency) {
    Semaphore semaphore = new Semaphore(maxConcurrency);
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<CompletableFuture<R>> futures = items.stream()
          .map(item -> CompletableFuture.supplyAsync(() -> processWithSemaphore(item, task, semaphore), executor))
          .toList();

      return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
          .thenApply(v -> futures.stream().map(CompletableFuture::join).toList())
          .join();
    }
  }

  private static <T, R> R processWithSemaphore(T item, Function<T, R> task, Semaphore semaphore) {
    try {
      semaphore.acquire();
      return task.apply(item);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return null;
    } finally {
      semaphore.release();
    }
  }

}
