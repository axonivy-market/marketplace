package com.axonivy.market.service;

import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.DocumentLanguageResponse;

import java.util.List;

public interface ExternalDocumentService {
  void syncDocumentForProduct(String productId, boolean isResetSync, String version);

  List<Product> findAllProductsHaveDocument();

  ExternalDocumentMeta findExternalDocument(String productId, String version);

  String findBestMatchVersion(String productId, String version);

  DocumentLanguageResponse findDocVersionsAndLanguages(String productId, String version, String language, String host);
}
