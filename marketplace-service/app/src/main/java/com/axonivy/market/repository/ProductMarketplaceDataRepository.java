package com.axonivy.market.repository;

import com.axonivy.market.core.entity.ProductMarketplaceData;
import com.axonivy.market.core.repository.CoreProductMarketplaceDataRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

//@Repository
//public interface ProductMarketplaceDataRepository
//    extends JpaRepository<ProductMarketplaceData, String>, CustomProductMarketplaceDataRepository {
//
//  @Modifying
//  @Transactional
//  @Query("UPDATE ProductMarketplaceData p SET p.customOrder = NULL")
//  void resetCustomOrderForAllProducts();
//}

@Repository
public interface ProductMarketplaceDataRepository
    extends CoreProductMarketplaceDataRepository, CustomProductMarketplaceDataRepository {

  @Modifying
  @Transactional
  @Query("UPDATE ProductMarketplaceData p SET p.customOrder = NULL")
  void resetCustomOrderForAllProducts();
}
