package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.ProductRating;
import com.axonivy.market.repository.FeedbackRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.repository.UserRepository;
import com.axonivy.market.service.FeedbackService;
import com.axonivy.market.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FeedbackServiceImpl implements FeedbackService {

  private final FeedbackRepository feedbackRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;

  public FeedbackServiceImpl(FeedbackRepository feedbackRepository, UserRepository userRepository, ProductRepository productRepository, UserService userService) {
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
    return feedbackRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCode.FEEDBACK_NOT_FOUND, "Not found feedback with id: " + id));
  }

  @Override
  public Feedback findFeedbackByUserIdAndProductId(String userId, String productId) throws NotFoundException {
    userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, "Not found user with id: " + userId));
    validateProductExists(productId);

    List<Feedback> existingFeedbacks = feedbackRepository.searchByProductIdAndUserId(userId, productId);
    if (existingFeedbacks.isEmpty()) {
      throw new NotFoundException(ErrorCode.FEEDBACK_NOT_FOUND, String.format("Not found feedback with user id '%s' and product id '%s'", userId, productId));
    }
    return existingFeedbacks.get(0);
  }

  @Transactional
  @Override
  public Feedback upsertFeedback(Feedback feedback) throws NotFoundException {
     userRepository.findById(feedback.getUserId())
        .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND,"Not found user with id: " + feedback.getUserId()));
    Product product = productRepository.findById(feedback.getProductId())
        .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Not found product with id: " + feedback.getProductId()));

    List<Feedback> existingFeedbacks = feedbackRepository.searchByProductIdAndUserId(feedback.getUserId(), feedback.getProductId());

    if (existingFeedbacks.isEmpty()) {
      productRepository.save(product);
      return feedbackRepository.save(feedback);
    } else {
      Feedback existingFeedback = existingFeedbacks.get(0);
      existingFeedback.setRating(feedback.getRating());
      existingFeedback.setContent(feedback.getContent());
      productRepository.save(product);
      return feedbackRepository.save(existingFeedback);
    }
  }

  @Override
  public List<ProductRating> getProductRatingById(String productId) {
    List<Feedback> feedbacks = this.feedbackRepository.getAllByProductId(productId);
    if (feedbacks.isEmpty()) {
      return Arrays.asList(
          new ProductRating(1, 0, 0),
          new ProductRating(2, 0, 0),
          new ProductRating(3, 0, 0),
          new ProductRating(4, 0, 0),
          new ProductRating(5, 0, 0)
      );
    }

    int totalFeedbacks = feedbacks.size();

    Map<Integer, Long> ratingCountMap = new HashMap<>();
    for (int i = 1; i <= 5; i++) {
      ratingCountMap.put(i, 0L);
    }

    ratingCountMap.putAll(feedbacks.stream()
        .collect(Collectors.groupingBy(Feedback::getRating, Collectors.counting())));

    return ratingCountMap.entrySet().stream()
        .map(entry -> {
          ProductRating productRating = new ProductRating();
          productRating.setStarRating(entry.getKey());
          productRating.setCommentNumber(Math.toIntExact(entry.getValue()));
          productRating.setPercent((int) ((entry.getValue() * 100) / totalFeedbacks));
          return productRating;
        })
        .collect(Collectors.toList());
  }

  private void validateProductExists(String productId) throws NotFoundException {
    productRepository.findById(productId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Not found product with id: " + productId));
  }
}
