package com.axonivy.market.core.repository;

import com.axonivy.market.core.entity.ProductMarketplaceData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoreProductMarketplaceDataRepository
    extends JpaRepository<ProductMarketplaceData, String>, CoreCustomProductMarketplaceDataRepository{
}
