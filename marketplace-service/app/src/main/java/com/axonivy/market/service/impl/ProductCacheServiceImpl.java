package com.axonivy.market.service.impl;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import com.axonivy.market.core.enums.AppSettingKey;
import com.axonivy.market.core.service.AppSettingService;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ProductCacheService;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProductCacheServiceImpl implements ProductCacheService {

  private final ProductRepository productRepo;
  private final AppSettingService appSettingService;
  private final ReentrantLock refreshLock = new ReentrantLock();

  private volatile Set<String> validProductIds = Set.of();
  private volatile Map<String, Set<String>> validVersionsByProductId = Map.of();
  private volatile long lastLoadedAt = 0L;

  @Override
  public boolean isValidProductId(String productId) {
    if (productId == null) {
      return false;
    }
    refreshIfNeeded();
    return validProductIds.contains(productId);
  }

  @Override
  public boolean isValidProductIdAndVersion(String productId, String version) {
    if (productId == null || version == null) {
      return false;
    }
    refreshIfNeeded();
    var versions = validVersionsByProductId.get(productId);
    return versions != null && versions.contains(version);
  }

  private void refreshIfNeeded() {
    if (!isExpired()) {
      return;
    }
    refreshLock.lock();
    try {
      // Re-check after acquiring the lock, another thread may have already refreshed the cache.
      if (isExpired()) {
        validVersionsByProductId = loadVersionsByProductId();
        validProductIds = new HashSet<>(validVersionsByProductId.keySet());
        lastLoadedAt = System.currentTimeMillis();
        log.info("Refreshed product cache with {} ids and {} products with versions", validProductIds.size(),
            validVersionsByProductId.size());
      }
    } finally {
      refreshLock.unlock();
    }
  }

  private Map<String, Set<String>> loadVersionsByProductId() {
    Map<String, Set<String>> versionsByProductId = new HashMap<>();
    for (var product : productRepo.findAll()) {
      if (product.getReleasedVersions() != null && !product.getReleasedVersions().isEmpty()) {
        versionsByProductId.put(product.getId(), new HashSet<>(product.getReleasedVersions()));
      }
    }
    return versionsByProductId;
  }


  private boolean isExpired() {
    long expirationMillis = Duration.ofMinutes(
        appSettingService.getLongValueByKey(AppSettingKey.PRODUCT_CACHE_EXPIRATION_MINUTES)).toMillis();
    return validProductIds.isEmpty() || System.currentTimeMillis() - lastLoadedAt > expirationMillis;
  }
}
