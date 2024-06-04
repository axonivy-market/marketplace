package com.axonivy.market.service;

import com.axonivy.market.entity.Product;

import java.util.List;

public interface ProductService {
    List<Product> fetchAll(String type, String sort, int page, int pageSize);

    Product findByKey(String key);

    List<Product> findProductsFromGithubRepo();
}
