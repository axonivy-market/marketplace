package com.axonivy.market.service;

import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.model.FeedbackApprovalModel;
import com.axonivy.market.model.FeedbackModelRequest;
import com.axonivy.market.model.ProductRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FeedbackService {

  /**
   * <p>
   * Retrieves a paginated list of all feedback entries from the database, including associated product names and
   * other related information, ordered by creation date or other specified criteria.
   * </p>
   *
   * @param pageable
   *              type {@link Pageable} - pagination and sorting information
   * @return {@link Page<Feedback>} - a page of feedback entries
   * @author nntthuy
   */
  Page<Feedback> findAllFeedbacks(Pageable pageable);

  /**
   * <p>
   * Retrieves a paginated list of feedback entries for a specific product, filtering only the latest approved and not
   * in rejected or pending status feedbacks, and validates that the product exists before fetching.
   * </p>
   *
   * @param productId
   *              type {@link String} - the unique identifier of the product
   * @param pageable
   *              type {@link Pageable} - pagination and sorting information
   * @return {@link Page<Feedback>} - a page of feedback entries for the product
   * @throws NotFoundException if the product is not found
   * @author ndkhanh
   */
  Page<Feedback> findFeedbacks(String productId, Pageable pageable) throws NotFoundException;

  /**
   * <p>
   * Retrieves a single feedback entry by its unique identifier from the database.
   * </p>
   *
   * @param id
   *              type {@link String} - the unique identifier of the feedback
   * @return {@link Feedback} - the feedback entry
   * @throws NotFoundException if the feedback is not found
   * @author ndkhanh
   */
  Feedback findFeedback(String id) throws NotFoundException;

  /**
   * <p>
   * Retrieves a list of feedback entries for a specific user and product combination, filtering only the latest
   * feedbacks that are not rejected, and validates that the user and product exist.
   * </p>
   *
   * @param userId
   *              type {@link String} - the unique identifier of the user
   * @param productId
   *              type {@link String} - the unique identifier of the product
   * @return {@link List<Feedback>} - list of feedback entries for the user and product
   * @throws NotFoundException if the user or product is not found
   * @author nntthuy
   */
  List<Feedback> findFeedbackByUserIdAndProductId(String userId, String productId) throws NotFoundException;

  /**
   * <p>
   * Updates the status of an existing feedback entry based on approval decision, setting it to approved or rejected,
   * and handles deactivating other latest feedbacks if approved.
   * </p>
   *
   * @param feedbackApproval
   *              type {@link FeedbackApprovalModel} - the approval model containing feedback ID, version, and
   *              approval decision
   * @return {@link Feedback} - the updated feedback entry
   * @throws NotFoundException if the feedback is not found
   * @author nntthuy
   */
  Feedback updateFeedbackWithNewStatus(FeedbackApprovalModel feedbackApproval);

  /**
   * <p>
   * Creates a new feedback entry or updates an existing one for the given user and product, handling pending and
   * approved feedbacks, and validates user existence. Deletes pending if duplicate approved exists or reuses
   * approved feedback if content, rating match
   * </p>
   *
   * @param feedback
   *              type {@link FeedbackModelRequest} - the feedback data to create or update
   * @param userId
   *              type {@link String} - the unique identifier of the user submitting the feedback
   * @return {@link Feedback} - the created or updated feedback entry
   * @throws NotFoundException if the user or product is not found
   * @author ntqdinh
   */
  Feedback upsertFeedback(FeedbackModelRequest feedback, String userId) throws NotFoundException;

  /**
   * <p>
   * Retrieves aggregate rating statistics for a specific product. Returns overall rating score,
   * rating distribution (count by star), and average rating calculated from all user feedbacks. Only counts latest,
   * non-pending, non-rejected
   * </p>
   *
   * @param  productId
   *              type {@link String} - the unique product identifier to retrieve ratings for
   * @return {@link List<ProductRating>} - list of rating objects containing rating score, count, and statistics;
   *         returns empty list if product has no ratings/feedbacks and returns 0-filled stars if no data found.
   * @author ndkhanh
   */
  List<ProductRating> getProductRatingById(String productId);
}
