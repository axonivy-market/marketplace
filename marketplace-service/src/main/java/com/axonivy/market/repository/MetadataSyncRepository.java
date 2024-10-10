package com.axonivy.market.repository;

import com.axonivy.market.entity.MetadataSync;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MetadataSyncRepository extends MongoRepository<MetadataSync, String> {
  void deleteAllByProductId(String productId);
}
