package com.axonivy.market.repository;

import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.key.MavenArtifactKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MavenArtifactVersionRepository extends JpaRepository<MavenArtifactVersion, MavenArtifactKey> {
  @Query("SELECT m FROM MavenArtifactVersion m WHERE m.productId = :productId ORDER BY m.id.isAdditionalVersion")
  List<MavenArtifactVersion> findByProductIdOrderByAdditionalVersion(@Param("productId") String productId);

  List<MavenArtifactVersion> findByProductId(String productId);

  void deleteAllByProductId(String productId);

  @Query("SELECT m FROM MavenArtifactVersion m WHERE m.id.artifactId = :artifactId AND m.id.productVersion = :version" +
      " AND m.productId = :productId")
  List<MavenArtifactVersion> findByArtifactIdAndVersion(@Param("productId") String productId,
      @Param("artifactId") String artifactId,
      @Param("version") String version);
}
