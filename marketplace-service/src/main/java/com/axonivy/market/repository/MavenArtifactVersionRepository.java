package com.axonivy.market.repository;

import com.axonivy.market.entity.MavenArtifactVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MavenArtifactVersionRepository extends JpaRepository<MavenArtifactVersion, String> {
  List<MavenArtifactVersion> findByProductId(String productId);

  void deleteAllByProductId(String productId);
}
