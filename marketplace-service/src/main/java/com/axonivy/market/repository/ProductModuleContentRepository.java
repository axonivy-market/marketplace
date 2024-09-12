package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductModuleContent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductModuleContentRepository extends MongoRepository<ProductModuleContent, String> {
    ProductModuleContent findByTagAndProductId(String tag, String productId);
    
    List<ProductModuleContent> findByProductId(String productId);
}
