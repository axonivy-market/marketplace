package com.axonivy.market.service;

import com.axonivy.market.entity.Product;

import java.util.List;

public interface ExternalDocumentService {
  void syncDocumentForProduct(String productId, boolean isResetSync);

  List<Product> findAllProductsHaveDocument();

  String findExternalDocumentURI(String productId, String version);
}
