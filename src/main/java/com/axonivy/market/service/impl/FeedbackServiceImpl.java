package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.exceptions.model.DuplicatedEntityException;
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
  public Feedback createFeedback(Feedback feedback) throws NotFoundException {
    userRepository.findById(feedback.getUserId())
        .orElseThrow(() -> new NotFoundException("Not found user with id: " + feedback.getUserId()));
    validateProductExists(feedback.getProductId());
    validateUniqueFeedbackForOneUser(feedback.getProductId(), feedback.getUserId());
    return feedbackRepository.save(feedback);
  }

  private void validateProductExists(String productId) throws NotFoundException {
    productRepository.findById(productId)
        .orElseThrow(() -> new NotFoundException("Not found product with id: " + productId));
  }

  private void validateUniqueFeedbackForOneUser(String productId, String userId) {
    List<Feedback> existingFeedbacks  = feedbackRepository.searchByProductIdAndUserId(productId, userId);
    if (!existingFeedbacks.isEmpty()) {
      throw new DuplicatedEntityException(String.format("Feedback already exists for product with id '%s' and user with id '%s'", productId, userId));
    }
  }
}
