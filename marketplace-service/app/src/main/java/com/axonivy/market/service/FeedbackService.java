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
   * Find all feedbacks
   * </p>
   *
   * @param pageable
   *              type {@link Pageable}
   * @return {@link Page<Feedback>}
   * @author nntthuy
   */
  Page<Feedback> findAllFeedbacks(Pageable pageable);

  /**
   * <p>
   * Find feedbacks by product id
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @return {@link Page<Feedback>}
   * @author ndkhanh
   */
  Page<Feedback> findFeedbacks(String productId, Pageable pageable) throws NotFoundException;

  /**
   * <p>
   * Find feedback by id
   * </p>
   *
   * @param id 
   *              type {@link String}
   * @return {@link Feedback}
   * @author ndkhanh
   */
  Feedback findFeedback(String id) throws NotFoundException;

  /**
   * <p>
   * Find feedback by user id and product id
   * </p>
   *
   * @param  userId
   *              type {@link String}
   * @param  productId
   *              type {@link String}
   * @return {@link List<Feedback>}
   * @author nntthuy
   */
  List<Feedback> findFeedbackByUserIdAndProductId(String userId, String productId) throws NotFoundException;

  /**
   * <p>
   * Update feedback with new status
   * </p>
   *
   * @param  feedbackApproval
   *              type {@link FeedbackApprovalModel}
   * @return {@link Feedback}
   * @author nntthuy
   */
  Feedback updateFeedbackWithNewStatus(FeedbackApprovalModel feedbackApproval);

  /**
   * <p>
   * Update and insert feedback
   * </p>
   *
   * @param  feedback
   *              type {@link FeedbackModelRequest}
   * @param  userId
   *              type {@link String}
   * @return {@link Feedback}
   * @author ntqdinh
   */
  Feedback upsertFeedback(FeedbackModelRequest feedback, String userId) throws NotFoundException;

  /**
   * <p>
   * Get product rating by id
   * </p>
   *
   * @param  productId
   *              type {@link String}
   * @return {@link List<ProductRating>}
   * @author ndkhanh
   */
  List<ProductRating> getProductRatingById(String productId);
}
