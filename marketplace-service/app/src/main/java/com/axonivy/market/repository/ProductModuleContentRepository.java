package com.axonivy.market.repository;

import com.axonivy.market.core.repository.CoreProductModuleContentRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductModuleContentRepository extends CoreProductModuleContentRepository,
    CustomProductModuleContentRepository {

  @Modifying
  @Transactional
  @Query("DELETE FROM ProductModuleContent p WHERE p.productId = :productId")
  void deleteAllByProductId(@Param("productId") String productId);
}
