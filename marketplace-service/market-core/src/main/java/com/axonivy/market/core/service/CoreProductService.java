package com.axonivy.market.core.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.axonivy.market.core.entity.Product;

public interface CoreProductService {
  Page<Product> findProducts(String type, String keyword, String language, Boolean isRESTClient, Pageable pageable);
}
