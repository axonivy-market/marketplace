package com.axonivy.market.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultiTaskUtils {
  public static <T, R> List<R> parallelProcessWithLimit(Collection<T> items, Function<T, R> task, int maxConcurrency) {
    Semaphore semaphore = new Semaphore(maxConcurrency);
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<CompletableFuture<R>> futures = items.stream().map(item -> CompletableFuture.supplyAsync(() -> {
            try {
              semaphore.acquire();
              return task.apply(item);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              return null;
            } finally {
              semaphore.release();
            }
          }, executor)).toList();

      return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
          .thenApply(v -> futures.stream().map(CompletableFuture::join).toList())
          .join();
    }
  }
}
