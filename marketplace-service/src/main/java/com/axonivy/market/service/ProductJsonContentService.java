package com.axonivy.market.service;

public interface ProductJsonContentService {
  void updateProductJsonContent(String jsonContent, String currentVersion, String replaceVersion,
      String productId, String productName);
}