package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductJsonContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductJsonContentRepository extends MongoRepository<ProductJsonContent, String> {

  ProductJsonContent findByProductIdAndTag(String productId, String tag);
}
