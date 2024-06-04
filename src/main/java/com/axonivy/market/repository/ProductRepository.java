package com.axonivy.market.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.axonivy.market.entity.Product;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

  List<Product> findByType(String type);
//  List<Product> findByTypeOrderBy(String type, String orderBy);
}
