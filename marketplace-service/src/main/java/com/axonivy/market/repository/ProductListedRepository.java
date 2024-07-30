package com.axonivy.market.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.criteria.ProductSearchCriteria;

public interface ProductListedRepository {

  Page<Product> findAllListed(Pageable pageable);

  Page<Product> searchListedByCriteria(ProductSearchCriteria criteria, Pageable pageable);

  Product findListedByCriteria(ProductSearchCriteria criteria);

  Product findByCriteria(ProductSearchCriteria criteria);

}
