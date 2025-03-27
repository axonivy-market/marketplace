package com.axonivy.market.repository;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.enums.FeedbackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {
  Optional<Feedback> findByIdAndVersion(String id, Integer version);

  List<Feedback> findByProductId(String productId);

  List<Feedback> findByProductIdAndFeedbackStatusNotIn(String productId, List<FeedbackStatus> feedbackStatuses);

  List<Feedback> findByProductIdAndUserIdAndFeedbackStatusNotIn(String productId, String userId,
      List<FeedbackStatus> feedbackStatuses);

  @Query("""
      SELECT f FROM Feedback f
      WHERE f.productId = :productId
        AND f.feedbackStatus NOT IN :excludedStatuses
        AND NOT EXISTS (
          SELECT 1 FROM Feedback f2
          WHERE f2.productId = f.productId
            AND f2.userId = f.userId
            AND f2.feedbackStatus NOT IN :excludedStatuses
            AND f2.reviewDate > f.reviewDate)
      """)
  Page<Feedback> findLatestApprovedFeedbacks(@Param("productId") String productId,
      @Param("excludedStatuses") List<FeedbackStatus> excludedStatuses, Pageable pageable);
}
