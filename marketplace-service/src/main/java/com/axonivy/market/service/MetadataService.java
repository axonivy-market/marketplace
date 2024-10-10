package com.axonivy.market.service;

import com.axonivy.market.entity.Product;

public interface MetadataService {

  int syncAllProductsMetadata();
  boolean syncProductMetadata(Product product);
}
