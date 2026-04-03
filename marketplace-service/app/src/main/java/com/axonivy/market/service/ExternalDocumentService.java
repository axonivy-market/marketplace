package com.axonivy.market.service;

import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.model.DocumentInfoResponse;

import java.util.List;

public interface ExternalDocumentService {

  /**
   * <p>
   * Synchronize document for product
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  isResetSync
   *              type {@link boolean}
   * @param  version
   *              type {@link String}
   * @return {@link }
   * @author nqhoan
   */
  void syncDocumentForProduct(String productId, boolean isResetSync, String version);

  /**
   * <p>
   * Find all product have document
   * </p>
   *
   * @param
   *              type {@link }
   * @return {@link List<Product>}
   * @author nqhoan
   */
  List<Product> findAllProductsHaveDocument();

  /**
   * <p>
   * Find external document
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @param  version
   *              type {@link String}
   * @return {@link ExternalDocumentMeta}
   * @author nqhoan
   */
  ExternalDocumentMeta findExternalDocument(String productId, String version);

  /**
   * <p>
   * Get best match redirect url
   * </p>
   *
   * @param  path
   *              type {@link String}
   * @return {@link String}
   * @author ttan
   */
  String resolveBestMatchRedirectUrl(String path);

  /**
   * <p>
   * Find document versions and languages
   * </p>
   *
   * @param artifact
   *              type {@link String}
   * @param version
   *              type {@link String}
   * @param host
   *              type {@link String}
   * @return {@link DocumentInfoResponse}
   * @author pvquan
   */
  DocumentInfoResponse findDocVersionsAndLanguages(String artifact, String version, String language, String host);

  /**
   * <p>
   * Determine product ids for synchronization
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @return {@link  List<String>}
   * @author ttan
   */
  List<String> determineProductIdsForSync(String productId);
}
