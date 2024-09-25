package com.axonivy.market.service;

import com.axonivy.market.entity.Product;

import java.util.List;

public interface ProductDocumentService {
  void syncDocumentForProduct(String productId, boolean isResetSync);

  List<Product> findAllProductsHaveDocument();

  String findViewDocURI(String productId, String version);
}
