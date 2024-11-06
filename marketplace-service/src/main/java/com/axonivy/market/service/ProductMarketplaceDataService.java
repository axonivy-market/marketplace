package com.axonivy.market.service;

import com.axonivy.market.model.ProductCustomSortRequest;

public interface ProductMarketplaceDataService {
  void addCustomSortProduct(ProductCustomSortRequest customSort);

  int updateInstallationCountForProduct(String key, String designerVersion);

  int updateProductInstallationCount(String id);
}
