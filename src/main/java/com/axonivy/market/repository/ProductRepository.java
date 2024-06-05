package com.axonivy.market.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.axonivy.market.entity.Product;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

  Page<Product> findByType(String type, Pageable pageable);
//  List<Product> findByTypeOrderBy(String type, String orderBy);
}
