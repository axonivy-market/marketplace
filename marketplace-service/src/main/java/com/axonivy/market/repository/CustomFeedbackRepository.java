package com.axonivy.market.repository;

import com.axonivy.market.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomFeedbackRepository {
  Page<Feedback> searchByProductId(String productId, Pageable pageable);
  Feedback findByUserIdAndProductId(String userId, String productId);
}
