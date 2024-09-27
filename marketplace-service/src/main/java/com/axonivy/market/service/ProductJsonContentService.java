package com.axonivy.market.service;

import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;

public interface ProductJsonContentService {
  void updateProductJsonContent(ProductModuleContent productModuleContent, String jsonContent, String currentVersion,
      Product product);
}