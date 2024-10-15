package com.axonivy.market.repository;

import com.axonivy.market.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String>, CustomProductRepository {
  List<Product> findByMarketDirectory(String marketDirectory);
}
