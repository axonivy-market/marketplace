package com.axonivy.market.service;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.exceptions.model.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FeedbackService {
  Page<Feedback> findFeedbacks(String productId, Pageable pageable) throws NotFoundException;
  Feedback findFeedback(String id) throws NotFoundException;
  Feedback findFeedbackByUserIdAndProductId(String userId, String productId) throws NotFoundException;
  Feedback upsertFeedback(Feedback feedback) throws NotFoundException;
}
