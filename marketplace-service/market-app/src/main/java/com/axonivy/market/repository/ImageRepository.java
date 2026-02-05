package com.axonivy.market.repository;

import com.axonivy.market.core.entity.Image;
import com.axonivy.market.core.repository.CoreImageRepository;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Primary
public interface ImageRepository extends CoreImageRepository {
  List<Image> findByProductIdAndSha(String productId, String sha);

  List<Image> findByImageUrlEndsWithIgnoreCase(String fileName);

  List<Image> findByProductId(String productId);

  void deleteAllByProductId(String productId);

  List<Image> findByCustomId(String customID);
}
