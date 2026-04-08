package com.axonivy.market.repository;

import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.model.ProductDeprecationProjection;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductMarketplaceDataRepository
    extends JpaRepository<ProductMarketplaceData, String>, CustomProductMarketplaceDataRepository {

  @Modifying
  @Transactional
  @Query("UPDATE ProductMarketplaceData p SET p.customOrder = NULL")
  void resetCustomOrderForAllProducts();

  List<ProductMarketplaceData> findByCustomOrderIsNotNullOrderByCustomOrderDesc();

  @Query(value = """
      SELECT pmd.id AS id,
             pmd.deprecation_date AS deprecationDate,
             pmd.deprecation_requester AS deprecationRequester
      FROM product_marketplace_data pmd
      JOIN product p ON p.id = pmd.id
      WHERE (
          (:deprecated IS NOT NULL AND p.deprecated = :deprecated)
          OR (:deprecated IS NULL AND p.deprecated IS NULL)
      )
      """, nativeQuery = true)
  List<ProductDeprecationProjection> findProductIdsByDeprecated(@Param("deprecated") Boolean deprecated);
}
