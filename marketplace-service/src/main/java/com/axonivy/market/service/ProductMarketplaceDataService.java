package com.axonivy.market.service;

import com.axonivy.market.entity.ProductMarketplaceData;
import com.axonivy.market.model.ProductCustomSortRequest;
import org.springframework.core.io.ByteArrayResource;

import java.io.IOException;

public interface ProductMarketplaceDataService {
  void addCustomSortProduct(ProductCustomSortRequest customSort);

  int updateInstallationCountForProduct(String key, String designerVersion);

  int updateProductInstallationCount(String id);

  ProductMarketplaceData getProductMarketplaceData(String id);

  ByteArrayResource downloadArtifact(String artifactUrl, String productId) throws IOException;
}
