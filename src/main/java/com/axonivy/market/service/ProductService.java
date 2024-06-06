package com.axonivy.market.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.axonivy.market.entity.Product;

public interface ProductService {
  List<Product> fetchAll(String type, String sort, int page, int pageSize);

  Product findByKey(String key);

  Page<Product> findProductsByType(String type, Pageable pageable);
}
