package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductModuleContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductModuleContentRepository extends MongoRepository<ProductModuleContent, String> {
    ProductModuleContent findByTagAndProductId(String tag, String productId);

    boolean existsByProductIdAndTag(String productId, String tag);
}
