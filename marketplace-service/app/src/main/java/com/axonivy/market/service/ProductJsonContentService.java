package com.axonivy.market.service;

import com.axonivy.market.core.entity.ProductJsonContent;

public interface ProductJsonContentService {

  /**
   * <p>
   * Update product json content
   * </p>
   *
   * @param  jsonContent
   *              type {@link String}
   * @param  currentVersion
   *              type {@link String}
   * @param  replaceVersion
   *              type {@link String}
   * @param  productId
   *              type {@link String}
   * @param  productName
   *              type {@link String}
   * @return {@link ProductJsonContent}
   * @author nntthuy
   */
  ProductJsonContent updateProductJsonContent(String jsonContent, String currentVersion, String replaceVersion,
      String productId, String productName);
}
