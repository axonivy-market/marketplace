package com.axonivy.market.service.impl;

import com.axonivy.market.core.enums.AppSettingKey;
import com.axonivy.market.core.service.AppSettingService;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.ProductCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
@Service
@RequiredArgsConstructor
public class ProductCacheServiceImpl implements ProductCacheService {

  private final ProductRepository productRepo;
  private final AppSettingService appSettingService;
  private final ReentrantLock refreshLock = new ReentrantLock();

  private volatile List<String> validProductIds = new ArrayList<>();
  private volatile long lastLoadedAt = 0L;

  @Override
  public boolean isValidProductId(String productId) {
    if (productId == null) {
      return false;
    }
    refreshIfNeeded();
    return validProductIds.contains(productId);
  }

  private void refreshIfNeeded() {
    if (!isExpired()) {
      return;
    }
    refreshLock.lock();
    try {
      if (isExpired()) {
        validProductIds = productRepo.findAllIds();
        lastLoadedAt = System.currentTimeMillis();
        log.info("Refreshed product cache with {} ids products with versions", validProductIds.size());
      }
    } finally {
      refreshLock.unlock();
    }
  }

  private boolean isExpired() {
    return validProductIds.isEmpty() || System.currentTimeMillis() - lastLoadedAt > getProductCacheExpirationMillis();
  }

  private long getProductCacheExpirationMillis() {
    return Duration.ofMinutes(appSettingService.getLongValueByKey(AppSettingKey.PRODUCT_ID_CACHE_EXPIRATION_MINUTES))
        .toMillis();
  }
}
