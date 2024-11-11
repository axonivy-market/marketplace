package com.axonivy.market.repository;

public interface CustomProductMarketplaceDataRepository {

  int updateInitialCount(String productId, int initialCount);

  int increaseInstallationCount(String productId);

  void increaseInstallationCountForProductByDesignerVersion(String productId, String designerVersion);

  void checkAndInitProductMarketplaceDataIfNotExist(String productId);
}
