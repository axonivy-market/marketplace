package com.axonivy.market.core.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.axonivy.market.core.entity.Product;

public interface CoreProductService {
  /**
   * @deprecated This method is deprecated and will be no longer use in future release.
   *             Use {@link #findProducts(String, String, String, Pageable)} instead.
   */
  @Deprecated
  Page<Product> findProducts(String type, String keyword, String language, Boolean isRESTClient, Pageable pageable);

  /**
   * <p>
   * Find all products
   * </p>
   *
   * @param  type
   *              type {@link String}
   * @param  keyword
   *              type {@link String}
   * @param  language
   *              type {@link String}
   * @param  pageable
   *              type {@link Pageable}
   * @return {@link Page<Product>}
   * @author ntqdinh
   */
  Page<Product> findProducts(String type, String keyword, String language, Pageable pageable);
}
