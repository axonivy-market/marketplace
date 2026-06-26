package com.axonivy.market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;

import java.time.Duration;
import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

  private static final String THREAD_NAME_AC_THREAD_PREFIX = "AC-Thread-";
  private static final int MAX_POOL_SIZE = 10;
  private static final int KEEP_ALIVE_SECONDS = 240;
  private static final String THREAD_NAME_ZIP_TASK_PREFIX = "ZipTask-";

  @Override
  public Executor getAsyncExecutor() {
    // Shared executor for @Async work and utility fan-out; virtual threads are fine for blocking I/O.
    return sharedVirtualThreadExecutor();
  }

  @Bean(name = "sharedVirtualThreadExecutor")
  public SimpleAsyncTaskExecutor sharedVirtualThreadExecutor() {
    return createVirtualThreadExecutor(THREAD_NAME_AC_THREAD_PREFIX, MAX_POOL_SIZE);
  }

  @Bean(name = "zipExecutor")
  public Executor zipExecutor() {
    // Keep zip work isolated from the general async lane because it can hold disk and CPU longer.
    var executor = createVirtualThreadExecutor(THREAD_NAME_ZIP_TASK_PREFIX, MAX_POOL_SIZE);
    executor.setTaskTerminationTimeout(Duration.ofSeconds(KEEP_ALIVE_SECONDS).toMillis());
    return executor;
  }

  private SimpleAsyncTaskExecutor createVirtualThreadExecutor(String threadNamePrefix, int concurrencyLimit) {
    var executor = new SimpleAsyncTaskExecutor(threadNamePrefix);
    executor.setVirtualThreads(true);
    executor.setConcurrencyLimit(concurrencyLimit);
    return executor;
  }

}
