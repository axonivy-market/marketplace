package com.axonivy.market.repository;

public interface CustomProductMarketplaceDataRepository {
  int increaseInstallationCount(String productId);

  void increaseInstallationCountForProductByDesignerVersion(String productId, String designerVersion);

  int updateInitialCount(String productId, int initialCount);
}
