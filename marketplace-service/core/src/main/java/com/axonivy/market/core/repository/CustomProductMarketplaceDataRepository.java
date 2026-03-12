package com.axonivy.market.core.repository;

public interface CustomProductMarketplaceDataRepository {

  int updateInitialCount(String productId, int initialCount);

  int increaseInstallationCount(String productId);

  void checkAndInitProductMarketplaceDataIfNotExist(String productId);
}
