package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.repository.FeedbackRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.repository.UserRepository;
import com.axonivy.market.service.FeedbackService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

  private final FeedbackRepository feedbackRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;

  public FeedbackServiceImpl(FeedbackRepository feedbackRepository, UserRepository userRepository, ProductRepository productRepository) {
    this.feedbackRepository = feedbackRepository;
    this.userRepository = userRepository;
    this.productRepository = productRepository;
  }

  @Override
  public Page<Feedback> findFeedbacks(String productId, Pageable pageable) throws NotFoundException {
    validateProductExists(productId);
    return feedbackRepository.searchByProductId(productId, pageable);
  }

  @Override
  public Feedback findFeedback(String id) throws NotFoundException {
    return feedbackRepository.findById(id).orElseThrow(() -> new NotFoundException("Not found feedback with id: " + id));
  }

  @Override
  public Feedback findFeedbackByUserIdAndProductId(String userId, String productId) throws NotFoundException {
    userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("Not found user with id: " + userId));
    validateProductExists(productId);

    List<Feedback> existingFeedbacks = feedbackRepository.searchByProductIdAndUserId(userId, productId);
    if (existingFeedbacks.isEmpty()) {
      throw new NotFoundException(String.format("Not found feedback with user id '%s' and product id '%s'", userId, productId));
    }
    return existingFeedbacks.get(0);
  }

  @Override
  public Feedback upsertFeedback(Feedback feedback) throws NotFoundException {
    userRepository.findById(feedback.getUserId())
        .orElseThrow(() -> new NotFoundException("Not found user with id: " + feedback.getUserId()));
    validateProductExists(feedback.getProductId());

    List<Feedback> existingFeedbacks = feedbackRepository.searchByProductIdAndUserId(feedback.getUserId(), feedback.getProductId());

    if (existingFeedbacks.isEmpty()) {
      return feedbackRepository.save(feedback);
    } else {
      Feedback existingFeedback = existingFeedbacks.get(0);
      existingFeedback.setRating(feedback.getRating());
      existingFeedback.setContent(feedback.getContent());
      return feedbackRepository.save(existingFeedback);
    }
  }

  private void validateProductExists(String productId) throws NotFoundException {
    productRepository.findById(productId)
        .orElseThrow(() -> new NotFoundException("Not found product with id: " + productId));
  }
}
