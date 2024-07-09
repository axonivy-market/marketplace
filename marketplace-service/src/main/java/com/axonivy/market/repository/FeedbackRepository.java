package com.axonivy.market.repository;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends MongoRepository<Feedback, String> {

  @Query("{ 'productId': ?0 }")
  Page<Feedback> searchByProductId(String productId, Pageable pageable);

  @Query("{ 'productId': ?0 }")
  List<Feedback> getAllByProductId(String productId);

  @Query("{ $and: [ { 'userId': ?0 },  { 'productId': ?1 } ] }")
  List<Feedback> searchByProductIdAndUserId(String userId, String productId);
}
