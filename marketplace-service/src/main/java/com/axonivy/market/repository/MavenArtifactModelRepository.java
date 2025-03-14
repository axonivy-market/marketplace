package com.axonivy.market.repository;

import com.axonivy.market.entity.MavenArtifactModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MavenArtifactModelRepository extends JpaRepository<MavenArtifactModel, String> {
  List<MavenArtifactModel> findByProductId(String productId);

}
