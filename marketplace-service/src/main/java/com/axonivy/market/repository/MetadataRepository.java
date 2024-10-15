package com.axonivy.market.repository;

import com.axonivy.market.entity.Metadata;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MetadataRepository extends MongoRepository<Metadata, String> {
  List<Metadata> findByProductId(String productId);

  List<Metadata> findByProductIdAndArtifactId(String productId, String artifactId);

  void deleteAllByProductId(String productId);
}
