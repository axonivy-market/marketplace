package com.axonivy.market.repository;

import com.axonivy.market.entity.ExternalDocumentMeta;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalDocumentMetaRepository extends JpaRepository<ExternalDocumentMeta, String> {

  List<ExternalDocumentMeta> findByProductId(String productId);

  @Transactional
  void deleteByProductIdAndVersionIn(String productId, List<String> versions);

  List<ExternalDocumentMeta> findByProductIdAndVersionIn(String productId, List<String> versions);
}
