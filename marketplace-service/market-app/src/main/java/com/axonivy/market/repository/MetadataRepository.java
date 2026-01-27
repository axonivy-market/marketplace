package com.axonivy.market.repository;

import com.axonivy.market.entity.Metadata;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetadataRepository extends JpaRepository<Metadata, String> {
  @EntityGraph(attributePaths = {"versions"})
  List<Metadata> findByProductId(String productId);

  @EntityGraph(attributePaths = {"versions"})
  List<Metadata> findByProductIdAndArtifactId(String productId, String artifactId);

  void deleteAllByProductId(String productId);

  List<Metadata> findByGroupIdAndArtifactId(String groupId, String artifactId);
}
