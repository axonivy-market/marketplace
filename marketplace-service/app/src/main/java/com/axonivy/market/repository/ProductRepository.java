package com.axonivy.market.repository;

import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.repository.CoreProductRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
public interface ProductRepository extends CoreProductRepository, CustomProductRepository {
  List<Product> findByMarketDirectory(String marketDirectory);

  @Query("SELECT p FROM Product p LEFT JOIN FETCH p.names LEFT JOIN FETCH p.shortDescriptions LEFT JOIN FETCH p" +
      ".artifacts a LEFT JOIN FETCH a.archivedArtifacts")
  List<Product> findAllProductsWithNamesAndShortDescriptions();
}
