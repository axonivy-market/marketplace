package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductMarketplaceData;
import org.springframework.data.repository.CrudRepository;

public interface ProductMarketplaceDataRepository extends CrudRepository<ProductMarketplaceData, String>,
    CustomProductMarketplaceDataRepository {
}
