package com.axonivy.market.repository;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.enums.FeedbackStatus;
import com.axonivy.market.model.FeedbackProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import static com.axonivy.market.constants.PostgresDBConstants.PRODUCT_ID;
import static com.axonivy.market.constants.PostgresDBConstants.USER_ID;
import static com.axonivy.market.constants.PostgresDBConstants.EXCLUDED_STATUSES;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {
  Optional<Feedback> findByIdAndVersion(String id, Integer version);

  List<Feedback> findByProductId(String productId);

  @Query("""
      SELECT f FROM Feedback f
      WHERE f.productId = :productId
        AND f.feedbackStatus NOT IN :excludedStatuses
        AND f.userId = :userId
        AND NOT EXISTS (
          SELECT 1 FROM Feedback f2
          WHERE f2.productId = f.productId
            AND f2.userId = f.userId
            AND f2.feedbackStatus NOT IN :excludedStatuses
            AND f2.reviewDate > f.reviewDate)
      """)
  List<Feedback> findFeedbacksByUser(@Param(PRODUCT_ID) String productId, @Param(USER_ID) String userId,
      @Param(EXCLUDED_STATUSES) List<FeedbackStatus> excludedStatuses);

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
  List<Feedback> findLatestApprovedFeedbacks(@Param(PRODUCT_ID) String productId,
      @Param(EXCLUDED_STATUSES) List<FeedbackStatus> excludedStatuses, Pageable pageable);

  @Query(value = """
        SELECT f.id AS id,
               f.user_id AS userId,
               f.product_id AS productId,
               f.content AS content,
               f.rating AS rating,
               f.feedback_status AS feedbackStatus,
               f.moderator_name AS moderatorName,
               f.review_date AS reviewDate,
               f.created_at AS createdAt,
               f.updated_at AS updatedAt,
               f.version AS version,
             CAST(json_object_agg(pn.language, pn.name) AS TEXT) AS productNamesJson
        FROM FEEDBACK f
        JOIN PRODUCT p ON f.product_id = p.id
        JOIN PRODUCT_NAME pn ON p.id = pn.product_id
        GROUP BY f.id
      """, nativeQuery = true)
  Page<FeedbackProjection> findFeedbackWithProductNames(Pageable pageable);
}
