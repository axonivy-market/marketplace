package com.axonivy.market.service;

import com.axonivy.market.core.entity.ProductJsonContent;

public interface ProductJsonContentService {
  ProductJsonContent updateProductJsonContent(String jsonContent, String currentVersion, String replaceVersion,
      String productId, String productName);
}
