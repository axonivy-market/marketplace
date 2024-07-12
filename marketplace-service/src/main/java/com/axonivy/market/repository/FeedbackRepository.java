package com.axonivy.market.repository;

import com.axonivy.market.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends MongoRepository<Feedback, String> {

  @Query("{ 'productId': ?0 }")
  Page<Feedback> searchByProductId(String productId, Pageable pageable);

  List<Feedback> findByProductId(String productId);

  Feedback findByUserIdAndProductId(String userId, String productId);
}
