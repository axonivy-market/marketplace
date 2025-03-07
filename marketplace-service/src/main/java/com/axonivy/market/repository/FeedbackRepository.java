package com.axonivy.market.repository;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.enums.FeedbackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {
  Optional<Feedback> findByIdAndVersion(String id, Integer version);

  Page<Feedback> searchByProductId(String productId, Pageable pageable);

  List<Feedback> findByProductId(String productId);

  List<Feedback> findByProductIdAndFeedbackStatusNotIn(String productId, List<FeedbackStatus> feedbackStatuses);

  List<Feedback> findByProductIdAndUserIdAndFeedbackStatusNotIn(String productId, String userId,
      List<FeedbackStatus> feedbackStatuses);

  Page<Feedback> findByProductIdAndFeedbackStatusNotIn(String productId, List<FeedbackStatus> feedbackStatuses,
      Pageable pageable);
}
