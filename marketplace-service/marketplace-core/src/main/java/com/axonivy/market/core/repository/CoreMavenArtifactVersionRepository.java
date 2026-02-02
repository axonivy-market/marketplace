package com.axonivy.market.core.repository;

import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.entity.key.MavenArtifactKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoreMavenArtifactVersionRepository extends JpaRepository<MavenArtifactVersion, MavenArtifactKey> {
  List<MavenArtifactVersion> findByProductId(String productId);
}
