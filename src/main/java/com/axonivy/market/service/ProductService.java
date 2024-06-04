package com.axonivy.market.service;

import java.util.List;

import com.axonivy.market.entity.Product;

public interface ProductService {
  List<Product> fetchAll(String type, String sort, int page, int pageSize);

  Product findByKey(String key);
}
