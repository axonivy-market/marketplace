package com.axonivy.market.repository;

import com.axonivy.market.criteria.ProductSearchCriteria;
import com.axonivy.market.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductSearchRepository {

  Page<Product> searchByCriteria(ProductSearchCriteria criteria, Pageable pageable);

  Product findByCriteria(ProductSearchCriteria criteria);
}
