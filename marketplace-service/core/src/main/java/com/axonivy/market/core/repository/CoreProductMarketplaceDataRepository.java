package com.axonivy.market.core.repository;

import com.axonivy.market.core.entity.ProductMarketplaceData;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CoreProductMarketplaceDataRepository
    extends JpaRepository<ProductMarketplaceData, String>, CoreCustomProductMarketplaceDataRepository{
}
