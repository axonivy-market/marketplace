package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductDependency;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ProductDependencyRepository extends JpaRepository<ProductDependency, String> {

  @EntityGraph(attributePaths = "dependencies")
  List<ProductDependency> findByProductIdAndArtifactIdAndVersion(String productId, String artifactId, String version);

  @EntityGraph(attributePaths = "dependencies")
  List<ProductDependency> findByProductId(String productId);

  @Modifying
  @Transactional
  @Query(value = "DELETE FROM product_dependency_dependencies WHERE dependencies_id = :dependencyId",
      nativeQuery = true)
  void deleteFromJoinTableByDependencyId(@Param("dependencyId") String dependencyId);
}
