package com.axonivy.market.service;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.FeedbackApprovalModel;
import com.axonivy.market.model.FeedbackModelRequest;
import com.axonivy.market.model.ProductRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FeedbackService {
  Page<Feedback> findAllFeedbacks(Pageable pageable);

  Page<Feedback> findFeedbacks(String productId, Pageable pageable) throws NotFoundException;

  Feedback findFeedback(String id) throws NotFoundException;

  Feedback findFeedbackByUserIdAndProductId(String userId, String productId) throws NotFoundException;

  Feedback updateFeedbackWithNewStatus(FeedbackApprovalModel feedbackApproval);

  Feedback upsertFeedback(FeedbackModelRequest feedback, String userId) throws NotFoundException;

  List<ProductRating> getProductRatingById(String productId);
}
