package com.axonivy.market.core.repository;

import com.axonivy.market.core.entity.ProductJsonContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoreProductJsonContentRepository extends JpaRepository<ProductJsonContent, String> {
  List<ProductJsonContent> findByProductIdAndVersionIgnoreCase(String productId, String version);
}
