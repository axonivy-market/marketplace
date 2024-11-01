package com.axonivy.market.service;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;

import java.util.List;

public interface MetadataService {

  int syncAllProductsMetadata();

  boolean syncProductMetadata(Product product);

  void updateArtifactAndMetaDataForProductJsonContent(ProductJsonContent productJsonContent, Artifact productArtifact);

  void updateArtifactAndMetadata(String productId, List<Artifact> artifacts);
}
