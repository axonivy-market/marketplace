package com.axonivy.market.core.config;

import com.axonivy.market.core.constants.CacheNameConstants;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Shared Caffeine cache configuration for both {@code app} and {@code stable} runtimes. The default
 * Caffeine spec is applied to caches that don't need a custom policy, such as {@code RepoReleases} in
 * {@code app}. Caches with a different lifetime, such as {@link CacheNameConstants#FIND_PRODUCTS}, are
 * registered individually with their own Caffeine spec. Expiry/size values are configurable via
 * environment variables, each with a sane default.
 * </p>
 */
@Configuration
@EnableCaching
public class CaffeineCacheConfig {

  @Value("${cache.default.expired-minutes:60}")
  private int defaultExpiredMinutes;

  @Value("${cache.default.maximum-size:1000}")
  private int defaultMaximumSize;

  @Value("${cache.products.expired-minutes:10}")
  private int productsExpiredMinutes;

  @Value("${cache.products.details.expired-minutes:5}")
  private int productsDetailsExpiredMinutes;

  @Value("${cache.products.details.github-releases.expired-minutes:10}")
  private int productsDetailsGitHubReleaseExpiredMinutes;

  @Bean
  public CacheManager cacheManager() {
    var cacheManager = new CaffeineCacheManager();
    cacheManager.setCaffeine(Caffeine.newBuilder()
        .expireAfterWrite(defaultExpiredMinutes, TimeUnit.MINUTES)
        .maximumSize(defaultMaximumSize));
    // For find all products
    cacheManager.registerCustomCache(CacheNameConstants.FIND_PRODUCTS, Caffeine.newBuilder()
        .expireAfterWrite(productsExpiredMinutes, TimeUnit.MINUTES)
        .maximumSize(defaultMaximumSize)
        .build());

    // For repo release (pinned 24 hours)
    cacheManager.registerCustomCache(CacheNameConstants.REPO_RELEASES, Caffeine.newBuilder()
        .expireAfterWrite(24, TimeUnit.HOURS)
        .maximumSize(defaultMaximumSize)
        .build());

    // For find product by id and version
    cacheManager.registerCustomCache(CacheNameConstants.FIND_PRODUCT_BY_ID_VERSION, Caffeine.newBuilder()
        .expireAfterWrite(productsDetailsExpiredMinutes, TimeUnit.MINUTES)
        .maximumSize(defaultMaximumSize)
        .build());

    // For GitHub releases
    cacheManager.registerCustomCache(CacheNameConstants.GET_GITHUB_RELEASES, Caffeine.newBuilder()
        .expireAfterWrite(productsDetailsGitHubReleaseExpiredMinutes, TimeUnit.MINUTES)
        .maximumSize(defaultMaximumSize)
        .build());
    cacheManager.registerCustomCache(CacheNameConstants.GET_GITHUB_RELEASES_PRODUCT_ID, Caffeine.newBuilder()
        .expireAfterWrite(productsDetailsGitHubReleaseExpiredMinutes, TimeUnit.MINUTES)
        .maximumSize(defaultMaximumSize)
        .build());
    return cacheManager;
  }
}
