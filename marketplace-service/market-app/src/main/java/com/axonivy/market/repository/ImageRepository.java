package com.axonivy.market.repository;

import com.axonivy.market.core.entity.Image;
import com.axonivy.market.core.repository.CoreImageRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends CoreImageRepository {
  List<Image> findByProductIdAndSha(String productId, String sha);

  List<Image> findByImageUrlEndsWithIgnoreCase(String fileName);

  List<Image> findByProductId(String productId);

  void deleteAllByProductId(String productId);
}
