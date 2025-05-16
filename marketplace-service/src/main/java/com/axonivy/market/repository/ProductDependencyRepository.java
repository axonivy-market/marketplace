package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductDependency;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDependencyRepository extends JpaRepository<ProductDependency, String> {

  @EntityGraph(attributePaths = "dependencies")
  List<ProductDependency> findByProductIdAndArtifactIdAndVersion(String productId, String artifactId, String version);

  @EntityGraph(attributePaths = "dependencies")
  List<ProductDependency> findByProductId(String productId);
}
