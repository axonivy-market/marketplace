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

  /**
   * Executes tasks in parallel with concurrency limit, does not return results.
   * Each task is executed with a semaphore to limit concurrency.
   * Any exception in a task is caught and logged, and does not affect other tasks.
   */
  public static <T> void parallelRunWithLimit(Collection<T> items, java.util.function.Consumer<T> task, int maxConcurrency) {
    Semaphore semaphore = new Semaphore(maxConcurrency);
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      List<CompletableFuture<Void>> futures = items.stream()
          .map(item -> CompletableFuture.runAsync(() -> processWithSemaphoreVoid(item, task, semaphore), executor))
          .toList();
      CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
  }

  private static <T> void processWithSemaphoreVoid(T item, java.util.function.Consumer<T> task, Semaphore semaphore) {
    try {
      semaphore.acquire();
      if (Thread.currentThread().isInterrupted()) {
        return;
      }
      task.accept(item);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Exception ex) {
      ex.printStackTrace(); // Or use a logger
    } finally {
      semaphore.release();
    }
  }
}
