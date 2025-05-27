package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductDependency;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDependencyRepository extends JpaRepository<ProductDependency, String> {

  List<ProductDependency> findByProductIdAndArtifactIdAndVersion(String productId, String artifactId, String version);

  @Modifying
  @Transactional
  void deleteAllByProductId(String productId);

  @Modifying
  @Transactional
  void deleteByProductIdAndArtifactIdAndVersion(String productId, String artifactId, String version);
}
