package com.axonivy.market.core.repository;

import com.axonivy.market.core.entity.ProductMarketplaceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoreProductMarketplaceDataRepository
    extends JpaRepository<ProductMarketplaceData, String>, CoreCustomProductMarketplaceDataRepository{
}
