package com.axonivy.market.core.repository;

import com.axonivy.market.core.criteria.ProductSearchCriteria;
import com.axonivy.market.core.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CoreCustomProductRepository {

  Page<Product> searchByCriteria(ProductSearchCriteria criteria, Pageable pageable);

  Product findByCriteria(ProductSearchCriteria criteria);
}
