package com.axonivy.market.core.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.axonivy.market.core.entity.Product;

public interface CoreProductService {
  /**
   * @deprecated This method is deprecated and will be no longer use in future release.
   *             Use {@link #findProducts(String, String, String, Pageable)} instead.
   */
  Page<Product> findProducts(String type, String keyword, String language, Boolean isRESTClient, Pageable pageable);

  Page<Product> findProducts(String type, String keyword, String language, Pageable pageable);
}
