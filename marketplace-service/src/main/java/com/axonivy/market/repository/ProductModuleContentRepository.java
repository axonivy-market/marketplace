package com.axonivy.market.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.axonivy.market.entity.ProductModuleContent;

@Repository
public interface ProductModuleContentRepository extends MongoRepository<ProductModuleContent, String> {

}
