package com.axonivy.market.repository;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.repository.CoreProductRepository;
import com.axonivy.market.model.projection.ProductIdMarketDirectoryProjection;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
public interface ProductRepository extends CoreProductRepository, CustomProductRepository {
  List<Product> findByMarketDirectory(String marketDirectory);

  @Query("""
      SELECT p.id AS id, p.marketDirectory AS marketDirectory
      FROM Product p
      """)
  List<ProductIdMarketDirectoryProjection> findAllIdAndMarketDirectory();

  @EntityGraph(attributePaths = {"artifacts.archivedArtifacts", "names"})
  @Query("""
      SELECT p
      FROM Product p
      WHERE p IN (
          SELECT p2
          FROM Product p2
          JOIN p2.names n
          WHERE KEY(n) = 'en'
      )
      """)
  List<Product> findProductsWithEnglishNameAndArtifacts();

  @Query("SELECT p.id FROM Product p ORDER BY p.id")
  List<String> findAllIds();
}
