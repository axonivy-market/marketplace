package com.axonivy.market.service;

import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;

import java.util.List;

public interface ExternalDocumentService {
  void syncDocumentForProduct(String productId, boolean isResetSync, String version);

  List<Product> findAllProductsHaveDocument();

  ExternalDocumentMeta findExternalDocument(String productId, String version);
}
