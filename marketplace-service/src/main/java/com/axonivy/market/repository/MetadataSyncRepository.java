package com.axonivy.market.repository;

import com.axonivy.market.entity.MetadataSync;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MetadataSyncRepository extends JpaRepository<MetadataSync, String> {
  void deleteAllByProductId(String productId);
}
