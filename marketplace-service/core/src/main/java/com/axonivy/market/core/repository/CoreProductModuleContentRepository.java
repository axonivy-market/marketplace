package com.axonivy.market.core.repository;

import com.axonivy.market.core.entity.ProductModuleContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoreProductModuleContentRepository extends JpaRepository<ProductModuleContent, String> {
  ProductModuleContent findByVersionAndProductId(String version, String productId);
}
