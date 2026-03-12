package com.axonivy.market.core.service;

import com.axonivy.market.core.entity.ProductMarketplaceData;

public interface CoreProductMarketplaceDataService {
  int updateProductInstallationCount(String id);

  ProductMarketplaceData getProductMarketplaceData(String productId);
}
