package com.axonivy.market.service;

import com.axonivy.market.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    List<Product> fetchAll(String type, String sort, int page, int pageSize);

    Product findByKey(String key);

  Page<Product> findProductsByType(String type, Pageable pageable);
}
