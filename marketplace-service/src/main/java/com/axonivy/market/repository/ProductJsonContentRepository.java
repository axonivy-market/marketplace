package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductJsonContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductJsonContentRepository extends MongoRepository<ProductJsonContent, String> {

  List<ProductJsonContent> findByProductIdAndVersion(String productId, String version);

  void deleteAllByProductId(String productId);
}
