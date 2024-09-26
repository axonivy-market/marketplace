package com.axonivy.market.repository;

import com.axonivy.market.entity.ExternalDocumentMeta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalDocumentMetaRepository extends MongoRepository<ExternalDocumentMeta, String> {

  List<ExternalDocumentMeta> findByProductIdAndVersion(String productId, String version);
}
