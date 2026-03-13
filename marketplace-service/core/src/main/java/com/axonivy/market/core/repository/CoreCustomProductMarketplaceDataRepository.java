package com.axonivy.market.core.repository;

public interface CoreCustomProductMarketplaceDataRepository {
  int updateInitialCount(String productId, int initialCount);

  int increaseInstallationCount(String productId);
}
