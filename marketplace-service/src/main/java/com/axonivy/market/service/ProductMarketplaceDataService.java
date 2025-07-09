package com.axonivy.market.service;

import com.axonivy.market.entity.ProductMarketplaceData;
import com.axonivy.market.model.ProductCustomSortRequest;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.OutputStream;

public interface ProductMarketplaceDataService {
  void addCustomSortProduct(ProductCustomSortRequest customSort);

  int updateInstallationCountForProduct(String id, String designerVersion);

  int updateProductInstallationCount(String id);

  ProductMarketplaceData getProductMarketplaceData(String id);

  Integer getInstallationCount(String id);

  OutputStream buildArtifactStreamFromResource(String productId, Resource resource, OutputStream outputStream);
}
