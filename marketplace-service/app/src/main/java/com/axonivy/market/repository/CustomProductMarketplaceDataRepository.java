package com.axonivy.market.repository;

import com.axonivy.market.core.repository.CoreCustomProductMarketplaceDataRepository;

public interface CustomProductMarketplaceDataRepository extends CoreCustomProductMarketplaceDataRepository {

  void checkAndInitProductMarketplaceDataIfNotExist(String productId);
}
