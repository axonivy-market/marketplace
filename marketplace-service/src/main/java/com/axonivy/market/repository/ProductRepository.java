package com.axonivy.market.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.axonivy.market.entity.Product;

@Repository
public interface ProductRepository extends MongoRepository<Product, String>, ProductListedRepository {

  Product findByLogoUrl(String logoUrl);

}
