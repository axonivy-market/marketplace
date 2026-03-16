package com.axonivy.market.repository;

import com.axonivy.market.core.repository.CoreProductMarketplaceDataRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
@Primary
public interface ProductMarketplaceDataRepository
    extends CoreProductMarketplaceDataRepository, CustomProductMarketplaceDataRepository {

  @Modifying
  @Transactional
  @Query("UPDATE ProductMarketplaceData p SET p.customOrder = NULL")
  void resetCustomOrderForAllProducts();
}
