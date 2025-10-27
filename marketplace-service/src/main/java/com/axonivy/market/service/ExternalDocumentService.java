package com.axonivy.market.service;

import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.model.DocumentInfoResponse;

import java.util.List;

public interface ExternalDocumentService {
  void syncDocumentForProduct(String productId, boolean isResetSync, String version);

  List<Product> findAllProductsHaveDocument();

  ExternalDocumentMeta findExternalDocument(String productId, String version);

  DocumentInfoResponse findDocVersionsAndLanguages(String artifact, String version, String language, String host);
  
  /**
   * Resolve the best redirect URL for a given document path
   * @param path The original path from the URL
   * @return The redirect URL or null if cannot be resolved
   */
  String resolveBestMatchRedirectUrl(String path);
}
