package com.axonivy.market.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.axonivy.market.entity.Product;

public interface ProductService {
  Page<Product> findProducts(String type, String keyword, Pageable pageable);

  boolean syncLatestDataFromMarketRepo();

  int updateInstallationCountForProduct(String key);
}
