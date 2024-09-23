package com.axonivy.market.repository;

import com.axonivy.market.entity.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends MongoRepository<Product, String>,
        CustomProductRepository {

  Product findByLogoId(String logoId);

}
