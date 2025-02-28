package com.axonivy.market.repository;

import com.axonivy.market.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomFeedbackRepository {
  Page<Feedback> searchByProductId(String productId, Pageable pageable);

  List<Feedback> findByUserIdAndProductId(String userId, String productId);
}
