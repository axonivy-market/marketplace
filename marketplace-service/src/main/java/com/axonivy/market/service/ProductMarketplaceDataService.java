package com.axonivy.market.service;

import com.axonivy.market.bo.VersionDownload;
import com.axonivy.market.entity.ProductMarketplaceData;
import com.axonivy.market.model.ProductCustomSortRequest;

import java.io.IOException;

public interface ProductMarketplaceDataService {
  void addCustomSortProduct(ProductCustomSortRequest customSort);

  int updateInstallationCountForProduct(String id, String designerVersion);

  int updateProductInstallationCount(String id);

  ProductMarketplaceData getProductMarketplaceData(String id);

  VersionDownload downloadArtifact(String artifactUrl, String productId) throws IOException;
}
