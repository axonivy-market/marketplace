package com.axonivy.market.service;

import com.axonivy.market.entity.ProductMarketplaceData;
import com.axonivy.market.model.ProductCustomSortRequest;

public interface ProductMarketplaceDataService {
  void addCustomSortProduct(ProductCustomSortRequest customSort);

  int updateInstallationCountForProduct(String key, String designerVersion);

  int updateProductInstallationCount(String id);

  ProductMarketplaceData getProductMarketplaceData(String id);
}
