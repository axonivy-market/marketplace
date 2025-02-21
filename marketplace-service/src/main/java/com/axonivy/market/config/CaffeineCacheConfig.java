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

  private static final int MAXIMUM_SIZE = 100;

  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
    caffeineCacheManager.setCaffeine(Caffeine.newBuilder()
        .expireAfterWrite(EXPIRED_HOURS, TimeUnit.HOURS)  // Entries expire after 10 minutes
        .maximumSize(MAXIMUM_SIZE)  // Maximum 100 entries
    );
    return caffeineCacheManager;
  }
}
