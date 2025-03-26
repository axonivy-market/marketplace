package com.axonivy.market.repository;

import com.axonivy.market.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, String> {
  List<Image> findByProductIdAndSha(String productId, String sha);

  List<Image> findByImageUrlEndsWithIgnoreCase(String fileName);

  List<Image> findByProductId(String productId);

  void deleteAllByProductId(String productId);
}
