package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductCustomSort;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductCustomSortRepository extends MongoRepository<ProductCustomSort, String> {
}
