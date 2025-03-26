package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductDependencyRepository extends JpaRepository<ProductDependency, String> {

  @Query("SELECT p FROM ProductDependency p LEFT JOIN FETCH p.dependenciesOfArtifact WHERE p.productId = :id")
  ProductDependency findByIdWithDependencies(@Param("id") String id);

  @Query("SELECT p FROM ProductDependency p LEFT JOIN FETCH p.dependenciesOfArtifact")
  List<ProductDependency> findAllWithDependencies();

}
