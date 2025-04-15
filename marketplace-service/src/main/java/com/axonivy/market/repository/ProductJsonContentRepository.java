package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductJsonContent;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductJsonContentRepository extends JpaRepository<ProductJsonContent, String> {

  List<ProductJsonContent> findByProductIdAndVersion(String productId, String version);

  List<ProductJsonContent> findByProductIdAndVersionIn(String productId, List<String> versions);

  @Modifying
  @Transactional
  @Query("DELETE FROM ProductJsonContent p WHERE p.productId = :productId")
  void deleteAllByProductId(@Param("productId") String productId);
}
