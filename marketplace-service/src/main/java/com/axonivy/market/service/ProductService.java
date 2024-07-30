package com.axonivy.market.service;

import com.axonivy.market.entity.Product;
import com.axonivy.market.exceptions.model.InvalidParamException;
import com.axonivy.market.model.ProductCustomSortRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
  Page<Product> findProducts(String type, String keyword, String language, Pageable pageable);

  boolean syncLatestDataFromMarketRepo();

  int updateInstallationCountForProduct(String key);

  Product fetchProductDetail(String id);

  String getCompatibilityFromOldestTag(String oldestTag);

  void clearAllProducts();

  void addCustomSortProduct(ProductCustomSortRequest customSort) throws InvalidParamException;
}
