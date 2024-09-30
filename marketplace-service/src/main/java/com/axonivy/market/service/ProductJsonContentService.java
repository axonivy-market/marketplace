package com.axonivy.market.service;

import com.axonivy.market.entity.Product;

public interface ProductJsonContentService {
  void updateProductJsonContent(String jsonContent, String currentVersion, String replaceVersion, Product product);
}