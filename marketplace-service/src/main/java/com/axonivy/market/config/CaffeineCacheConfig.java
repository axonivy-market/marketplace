package com.axonivy.market.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CaffeineCacheConfig {
  private static final int EXPIRED_HOURS = 24;
  private static final int MAXIMUM_SIZE = 1000;

  @Bean
  public CacheManager cacheManager() {
    var caffeineCacheManager = new CaffeineCacheManager();
    caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
        .expireAfterWrite(EXPIRED_HOURS, TimeUnit.HOURS)
        .maximumSize(MAXIMUM_SIZE)
    );
    return caffeineCacheManager;
  }
}
