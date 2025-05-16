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
  String SELECT_BY_PRODUCT_ID = "SELECT m FROM MavenArtifactVersion m WHERE m.productId = :productId ";
  @Query(SELECT_BY_PRODUCT_ID + "ORDER BY m.id.isAdditionalVersion")
  List<MavenArtifactVersion> findByProductIdOrderByAdditionalVersion(@Param("productId") String productId);

  List<MavenArtifactVersion> findByProductId(String productId);

  void deleteAllByProductId(String productId);

  @Query(SELECT_BY_PRODUCT_ID + "AND m.id.artifactId = :artifactId AND m.id.productVersion = :version")
  List<MavenArtifactVersion> findByProductIdAndArtifactIdAndVersion(@Param("productId") String productId,
      @Param("artifactId") String artifactId, @Param("version") String version);

  @Query("SELECT m.id.productVersion FROM MavenArtifactVersion m WHERE m.productId = :productId AND m.id.artifactId = :artifactId")
  List<String> findProductVersionsByProductIdAndArtifactId(@Param("productId") String productId,
      @Param("artifactId") String artifactId);
}
