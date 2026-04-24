package com.axonivy.market.service;

import com.axonivy.market.core.entity.ProductJsonContent;

public interface ProductJsonContentService {

  /**
   * <p>
   * Creates product JSON content by replacing replaceVersion by currentVersion
   * in the provided JSON content. Used to manage product configuration and version-specific metadata stored
   * as JSON documents in the database.
   * </p>
   *
   * @param  jsonContent
   *              type {@link String} - the raw JSON content containing product configuration and data
   * @param  currentVersion
   *              type {@link String} - the version string to find and replace in the JSON content
   * @param  replaceVersion
   *              type {@link String} - the new version string to replace the current version with
   * @param  productId
   *              type {@link String} - the unique product identifier associated with this JSON content
   * @param  productName
   *              type {@link String} - the product name for identification and logging purposes
   * @return {@link ProductJsonContent} - the updated ProductJsonContent object persisted in the database;
   *         contains productId, productName, version reference, and JSON content
   * @author nntthuy
   */
  ProductJsonContent updateProductJsonContent(String jsonContent, String currentVersion, String replaceVersion,
      String productId, String productName);
}
