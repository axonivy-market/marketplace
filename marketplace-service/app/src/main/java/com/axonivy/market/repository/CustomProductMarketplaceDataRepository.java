package com.axonivy.market.repository;

import com.axonivy.market.core.repository.CoreCustomProductMarketplaceDataRepository;

public interface CustomProductMarketplaceDataRepository extends CoreCustomProductMarketplaceDataRepository {

//  int updateInitialCount(String productId, int initialCount);

  int increaseInstallationCount(String productId);

  void checkAndInitProductMarketplaceDataIfNotExist(String productId);
}
