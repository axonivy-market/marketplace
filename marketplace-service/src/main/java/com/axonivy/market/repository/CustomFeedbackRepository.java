package com.axonivy.market.repository;

import com.axonivy.market.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomFeedbackRepository {
  Page<Feedback> searchByProductId(String productId, Pageable pageable);

  Feedback findByUserIdAndProductId(String userId, String productId);

  List<Feedback> findFeedbackByUser(String userId, String productId);
}
