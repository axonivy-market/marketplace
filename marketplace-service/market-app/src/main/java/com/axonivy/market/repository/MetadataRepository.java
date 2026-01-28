package com.axonivy.market.repository;

import com.axonivy.market.core.entity.Metadata;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MetadataRepository extends CoreMetadataRepository {

  void deleteAllByProductId(String productId);

  List<Metadata> findByGroupIdAndArtifactId(String groupId, String artifactId);
}
