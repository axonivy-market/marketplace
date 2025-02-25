package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductDependency;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductDependencyRepository extends MongoRepository<ProductDependency, String>, CustomProductDependencyRepository {

}
