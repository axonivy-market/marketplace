package com.axonivy.market.service;

import com.axonivy.market.model.ProductRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.axonivy.market.entity.Product;

import java.util.List;

public interface ProductService {
  Page<Product> findProducts(String type, String keyword, String language, Pageable pageable);

  boolean syncLatestDataFromMarketRepo();
}
