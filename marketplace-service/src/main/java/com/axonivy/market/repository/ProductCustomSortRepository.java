package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductCustomSort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductCustomSortRepository extends JpaRepository<ProductCustomSort, String> {
}
