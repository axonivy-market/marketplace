package com.axonivy.market.service;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;

public interface MetadataService {

  int syncAllProductsMetadata();

  boolean syncProductMetadata(Product product);

  void updateArtifactAndMetaDataForProduct(ProductJsonContent productJsonContent , Artifact productArtifact);
}
