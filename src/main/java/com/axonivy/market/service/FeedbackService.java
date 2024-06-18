package com.axonivy.market.service;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.exceptions.model.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FeedbackService {
  Page<Feedback> findFeedbacks(String productId, Pageable pageable) throws NotFoundException;
  Feedback findFeedback(String id) throws NotFoundException;
  Feedback createFeedback(Feedback feedback) throws NotFoundException;
}
