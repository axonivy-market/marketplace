package com.axonivy.market.repository;

import com.axonivy.market.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String>, CustomProductRepository {
  List<Product> findByMarketDirectory(String marketDirectory);

  @Query("SELECT p FROM Product p LEFT JOIN FETCH p.names LEFT JOIN FETCH p.shortDescriptions LEFT JOIN FETCH p" +
      ".artifacts a LEFT JOIN FETCH a.archivedArtifacts")
  List<Product> findAllProductsWithNamesAndShortDescriptions();
}
