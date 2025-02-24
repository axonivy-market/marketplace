package com.axonivy.market.repository;

import com.axonivy.market.entity.ExternalDocumentMeta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalDocumentMetaRepository extends JpaRepository<ExternalDocumentMeta, String> {

  List<ExternalDocumentMeta> findByProductIdAndVersion(String productId, String version);

  List<ExternalDocumentMeta> findByProductId(String productId);

  void deleteByProductIdAndVersion(String productId, String version);
}
