package com.axonivy.market.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.axonivy.market.entity.Product;

import java.util.List;
public interface ProductService {
  Product findByKey(String key);

  Page<Product> fetchAll(String type, Pageable pageable);
}
