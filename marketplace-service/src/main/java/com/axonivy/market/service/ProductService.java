package com.axonivy.market.service;

import com.axonivy.market.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
  Page<Product> findAllProducts(String type, String keyword, String language, Boolean isRestDesigner,Pageable pageable);

  boolean syncLatestDataFromMarketRepo();

  int updateInstallationCountForProduct(String key);
  Product fetchProductDetail(String id);

  String getCompatibilityFromOldestTag(String oldestTag);

  void clearAllProducts();
}
