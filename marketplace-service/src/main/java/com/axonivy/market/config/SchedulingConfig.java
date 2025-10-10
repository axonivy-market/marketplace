package com.axonivy.market.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulingConfig {

  private static final String THREAD_NAME_PREFIX = "SC-Thread-";
  private static final int POOL_SIZE = 10;

  @Bean
  public ThreadPoolTaskScheduler taskScheduler() {
    var taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(POOL_SIZE);
    taskScheduler.setThreadNamePrefix(THREAD_NAME_PREFIX);
    taskScheduler.initialize();
    return taskScheduler;
  }
}
