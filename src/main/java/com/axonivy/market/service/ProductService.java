package com.axonivy.market.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
  Page<Product> findProductsByType(String type, Pageable pageable);

  Page<Product> searchProducts(String keyword, Pageable pageable);
}
