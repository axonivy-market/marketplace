package com.axonivy.market.core.repository;

import com.axonivy.market.core.entity.ProductJsonContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoreProductJsonContentRepository extends JpaRepository<ProductJsonContent, String> {
  List<ProductJsonContent> findByProductIdAndVersion(String productId, String version);

}
