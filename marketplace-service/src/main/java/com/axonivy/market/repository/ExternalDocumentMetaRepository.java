package com.axonivy.market.repository;

import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.enums.DocumentLanguage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExternalDocumentMetaRepository extends JpaRepository<ExternalDocumentMeta, String> {

  List<ExternalDocumentMeta> findByProductId(String productId);

  List<ExternalDocumentMeta> findByProductIdAndLanguage(String productId, DocumentLanguage language);

  @Transactional
  void deleteByProductIdAndVersionIn(String productId, List<String> versions);

  List<ExternalDocumentMeta> findByProductIdAndVersionIn(String productId, List<String> versions);

  List<ExternalDocumentMeta> findByProductIdAndRelativeLinkAndVersionIn(String productId, String relativeLink, List<String> versions);
}
