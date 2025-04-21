package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductDependency;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDependencyRepository extends JpaRepository<ProductDependency, String> {

  @Query("SELECT p FROM ProductDependency p LEFT JOIN FETCH p.dependencies WHERE p.productId = :id")
  ProductDependency findByIdWithDependencies(@Param("id") String id);

  @Query("SELECT p FROM ProductDependency p LEFT JOIN FETCH p.dependencies")
  List<ProductDependency> findAllWithDependencies();

  List<ProductDependency> findByProductIdAndArtifactIdAndVersion(String productId, String artifactId, String productVersion);

  @Modifying
  @Transactional
  @Query("DELETE FROM ProductDependency p WHERE p.productId = :productId")
  void deleteAllByProductId(@Param("productId") String productId);

  void deleteByProductIdAndArtifactIdAndVersion(String productId, String artifactId, String version);
}
