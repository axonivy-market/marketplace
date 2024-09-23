package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductDocumentMeta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDocumentMetaRepository extends MongoRepository<ProductDocumentMeta, String> {

  List<ProductDocumentMeta> findByProductIdAndGroupIdAndArtifactIdAndVersion(String productId,
      String groupId, String artifactId, String version);
}
