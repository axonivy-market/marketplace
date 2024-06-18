package com.axonivy.market.service;

import com.axonivy.market.model.ReadmeModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.axonivy.market.entity.Product;

public interface ProductService {
    Page<Product> findProducts(String type, String keyword, Pageable pageable);

    Product fetchProductDetail(String id, String type);

    ReadmeModel getReadmeAndProductContentsFromTag(String id, String tag);
}
