package com.axonivy.market.repository;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.repository.CoreProductRepository;
import com.axonivy.market.model.projection.ProductIdMarketDirectoryProjection;
import org.springframework.context.annotation.Primary;
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

  @Query("""
      SELECT p
      FROM Product p
      LEFT JOIN FETCH p.artifacts a
      LEFT JOIN FETCH a.archivedArtifacts
      JOIN p.names n
      WHERE KEY(n) = 'en'
      """)
  List<Product> findProductsWithEnglishNameAndArtifacts();

  @Query("SELECT p.id FROM Product p ORDER BY p.id")
  List<String> findAllIds();
}
