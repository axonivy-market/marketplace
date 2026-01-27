package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductModuleContent;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductModuleContentRepository extends JpaRepository<ProductModuleContent, String>,
    CustomProductModuleContentRepository {
  ProductModuleContent findByVersionAndProductId(String version, String productId);

  @Modifying
  @Transactional
  @Query("DELETE FROM ProductModuleContent p WHERE p.productId = :productId")
  void deleteAllByProductId(@Param("productId") String productId);
}
