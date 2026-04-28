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
   * Retrieves a paginated list of products with multi-criteria filtering. Filters products by type, keyword
   * search, and language configuration. Returns paginated results sorted according to the provided Pageable
   * configuration for efficient large dataset handling.
   * </p>
   *
   * @param  type
   *              type {@link String} - product type filter (e.g., "Connector"); if null for will get all
   * @param  keyword
   *              type {@link String} - search keyword to filter products by name or description; can be null for no keyword filtering
   * @param  language
   *              type {@link String} - language code filter for product documentation (e.g., "en", "de"); can be null
   * @param  pageable
   *              type {@link Pageable} - pagination and sorting configuration (page number, page size, sort order)
   * @return {@link Page<Product>} - paginated list of products matching all criteria with total count and page information
   * @author ntqdinh
   */
  Page<Product> findProducts(String type, String keyword, String language, Pageable pageable);
}
