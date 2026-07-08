package com.axonivy.market.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AsyncConfigTest {

  private final AsyncConfig asyncConfig = new AsyncConfig();

  @Test
  void getAsyncExecutorUsesVirtualThreads() throws Exception {
    assertVirtualThreadExecutor(asyncConfig.getAsyncExecutor(), "AC-Thread-");
  }

  @Test
  void zipExecutorUsesVirtualThreads() throws Exception {
    assertVirtualThreadExecutor(asyncConfig.zipExecutor(), "ZipTask-");
  }

  private void assertVirtualThreadExecutor(Object executor, String expectedPrefix) throws Exception {
    assertInstanceOf(SimpleAsyncTaskExecutor.class, executor);
    var simpleExecutor = (SimpleAsyncTaskExecutor) executor;

    AtomicReference<Thread> threadRef = new AtomicReference<>();
    CountDownLatch latch = new CountDownLatch(1);

    simpleExecutor.execute(() -> {
      threadRef.set(Thread.currentThread());
      latch.countDown();
    });

    assertTrue(latch.await(5, TimeUnit.SECONDS), "Expected task to complete");
    assertNotNull(threadRef.get(), "Expected task thread to be captured");
    assertTrue(threadRef.get().isVirtual(), "Expected executor to run tasks on virtual threads");
    assertTrue(threadRef.get().getName().startsWith(expectedPrefix), "Expected thread name prefix");
  }
}
