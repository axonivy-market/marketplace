package com.axonivy.market.repository;

import com.axonivy.market.entity.Image;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends MongoRepository<Image, String> {
  List<Image> findByProductIdAndSha(String productId, String sha);

  List<Image> findByImageUrlEndsWithIgnoreCase(String fileName);

  List<Image> findByProductId(String productId);

  void deleteAllByProductId(String productId);
}
