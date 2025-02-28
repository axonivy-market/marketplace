package com.axonivy.market.repository;

import com.axonivy.market.entity.Metadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetadataRepository extends JpaRepository<Metadata, String> {
  List<Metadata> findByProductId(String productId);

  List<Metadata> findByProductIdAndArtifactId(String productId, String artifactId);

  void deleteAllByProductId(String productId);
}
