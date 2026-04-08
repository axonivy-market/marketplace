package com.axonivy.market.service;

import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.model.DeprecationRequest;
import com.axonivy.market.model.DeprecationResponse;
import com.axonivy.market.model.ProductCustomSortRequest;
import com.axonivy.market.model.ProductDeprecationProjection;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public interface ProductMarketplaceDataService {
  void addCustomSortProduct(ProductCustomSortRequest customSort);

  ProductCustomSortRequest getCustomSortProducts();

  int updateInstallationCountForProduct(String id, String designerVersion);

  ProductMarketplaceData updateProductInstallationCount(String id);

  ProductMarketplaceData getProductMarketplaceData(String id);

  Integer getInstallationCount(String id);

  ResponseEntity<Resource> getProductArtifactStream(String productId, String artifactId, String version);

  OutputStream buildArtifactStreamFromResource(String productId, Resource resource, OutputStream outputStream);

  DeprecationResponse updateSuccessorForProduct(String productId, DeprecationRequest deprecationRequest) throws IOException;

  List<ProductDeprecationProjection> getProductIdsByDeprecated(Boolean isDeprecated);
}
