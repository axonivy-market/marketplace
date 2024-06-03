package com.axonivy.market.service;

import java.util.List;

import com.axonivy.market.model.Product;

public interface ProductService {
  List<Product> fetchAll(String type, String sort);

  Product findByKey(String key);

  List<String> getVersions(String artifactID);
}
