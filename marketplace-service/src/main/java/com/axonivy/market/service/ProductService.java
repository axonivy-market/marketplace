package com.axonivy.market.service;

import com.axonivy.market.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
  Page<Product> findProducts(String type, String keyword, String language, Pageable pageable);

  boolean syncLatestDataFromMarketRepo();
}
