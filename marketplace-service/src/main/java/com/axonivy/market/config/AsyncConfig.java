package com.axonivy.market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

  private static final String THREAD_NAME_PREFIX = "AC-Thread-";
  private static final int CORE_POOL_SIZE = 5;
  private static final int MAX_POOL_SIZE = 10;
  private static final int QUEUE_CAPACITY_25 = 25;
  private static final int QUEUE_CAPACITY_50 = 50;
  private static final int KEEP_ALIVE_SECONDS = 240;

  @Override
  public Executor getAsyncExecutor() {
    var executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(CORE_POOL_SIZE);
    executor.setMaxPoolSize(MAX_POOL_SIZE);
    executor.setQueueCapacity(QUEUE_CAPACITY_25);
    executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
    executor.initialize();
    return executor;
  }

  @Bean(name = "zipExecutor")
  public Executor zipExecutor() {
    var executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(CORE_POOL_SIZE);
    executor.setMaxPoolSize(MAX_POOL_SIZE);
    executor.setQueueCapacity(QUEUE_CAPACITY_50);
    executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
    executor.setThreadNamePrefix("ZipTask-");
    executor.initialize();
    return executor;
  }

}
