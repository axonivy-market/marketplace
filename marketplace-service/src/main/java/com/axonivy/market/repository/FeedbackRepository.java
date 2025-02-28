package com.axonivy.market.repository;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.enums.FeedbackStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends MongoRepository<Feedback, String> {
  List<Feedback> findByProductId(String productId);

  List<Feedback> findByProductIdAndFeedbackStatusNotIn(String productId, List<FeedbackStatus> feedbackStatuses);
}
