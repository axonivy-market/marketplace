package com.axonivy.market.service;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;

import java.util.List;

public interface MetadataService {

  int syncAllProductsMetadata();

  boolean syncProductMetadata(Product product);

  void updateArtifactAndMetadata(String productId , List<String> versions , List<Artifact> artifacts);
}
