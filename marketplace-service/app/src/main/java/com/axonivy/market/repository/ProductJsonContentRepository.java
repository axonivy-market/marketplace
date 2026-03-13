package com.axonivy.market.repository;

import com.axonivy.market.core.entity.ProductJsonContent;
import com.axonivy.market.core.repository.CoreProductJsonContentRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Primary
public interface ProductJsonContentRepository extends CoreProductJsonContentRepository {

  List<ProductJsonContent> findByProductIdAndVersionIn(String productId, List<String> versions);

  @Modifying
  @Transactional
  @Query("DELETE FROM ProductJsonContent p WHERE p.productId = :productId")
  void deleteAllByProductId(@Param("productId") String productId);
}
