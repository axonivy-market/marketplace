package com.axonivy.market.repository;

import com.axonivy.market.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {

  Page<Feedback> searchByProductId(String productId, Pageable pageable);

  List<Feedback> findByProductId(String productId);

  Feedback findByUserIdAndProductId(String userId, String productId);

}
