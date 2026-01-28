package com.axonivy.market.repository;

import com.axonivy.market.core.entity.Metadata;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
public interface MetadataRepository extends CoreMetadataRepository {

  @EntityGraph(attributePaths = {"versions"})
  List<Metadata> findByProductIdAndArtifactId(String productId, String artifactId);

  void deleteAllByProductId(String productId);

  List<Metadata> findByGroupIdAndArtifactId(String groupId, String artifactId);
}
