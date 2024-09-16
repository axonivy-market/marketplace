package com.axonivy.market.repository;

import com.axonivy.market.entity.Image;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends MongoRepository<Image, String> {
  Image findByImageUrlAndSha(String logoUrl, String sha);
}
