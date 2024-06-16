package com.axonivy.market.service;

import com.axonivy.market.model.ProductDetailModel;
import com.axonivy.market.model.ReadmeModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.axonivy.market.entity.Product;

public interface ProductService {
  Page<Product> findProductsByType(String type, Pageable pageable);

  Page<Product> searchProducts(String keyword, Pageable pageable);

  Product fetchProductDetail(String key);
  ReadmeModel getReadmeContent(String key, String tag);
}
