package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductModuleContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductModuleContentRepository extends JpaRepository<ProductModuleContent, String>,
    CustomProductModuleContentRepository {
  ProductModuleContent findByVersionAndProductId(String version, String productId);

  void deleteAllByProductId(String productId);
}
