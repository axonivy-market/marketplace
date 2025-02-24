package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductMarketplaceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductMarketplaceDataRepository extends JpaRepository<ProductMarketplaceData, String>, CustomProductMarketplaceDataRepository {
}
