package com.axonivy.market.repository;

import com.axonivy.market.entity.MavenArtifactVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MavenArtifactVersionRepository extends JpaRepository<MavenArtifactVersion, String> {
  @Query("SELECT m FROM MavenArtifactVersion m WHERE m.id.artifactId = :artifactId ORDER BY m.id.isAdditionalVersion")
  List<MavenArtifactVersion> findByArtifactIdOrderByAdditionalVersion(@Param("artifactId") String artifactId);

  List<MavenArtifactVersion> findByProductId(String productId);

  void deleteAllByProductId(String productId);
}
