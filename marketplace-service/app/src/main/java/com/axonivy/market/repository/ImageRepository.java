package com.axonivy.market.repository;

import com.axonivy.market.core.entity.Image;
import com.axonivy.market.core.repository.CoreImageRepository;

import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Primary
public interface ImageRepository extends CoreImageRepository {
  List<Image> findByProductIdAndSha(String productId, String sha);

  List<Image> findByImageUrlEndsWithIgnoreCase(String fileName);

  List<Image> findByProductId(String productId);

  @Transactional
  @Modifying
  void deleteAllByProductId(String productId);
}
