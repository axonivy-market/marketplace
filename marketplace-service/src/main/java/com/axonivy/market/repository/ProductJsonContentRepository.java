package com.axonivy.market.repository;

import com.axonivy.market.entity.ProductJsonContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductJsonContentRepository extends JpaRepository<ProductJsonContent, String> {

  List<ProductJsonContent> findByProductIdAndVersion(String productId, String version);

  List<ProductJsonContent> findByProductIdAndVersionIn(String productId, List<String> versions);

  void deleteAllByProductId(String productId);
}
