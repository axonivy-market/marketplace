package com.axonivy.market.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.axonivy.market.entity.Product;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String>, ProductSearchRepository, CustomProductRepository {
  List<Product> findByMarketDirectory(String marketDirectory);

}
