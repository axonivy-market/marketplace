package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Feedback;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    Feedback existingUserFeedback = feedbackRepository.findByUserIdAndProductId(userId, productId);
    if (existingUserFeedback == null) {
      throw new NotFoundException(ErrorCode.FEEDBACK_NOT_FOUND, String.format("Not found feedback with user id '%s' and product id '%s'", userId, productId));
    }
    return existingUserFeedback;
  }

  @Override
  public Feedback upsertFeedback(Feedback feedback) throws NotFoundException {
     userRepository.findById(feedback.getUserId())
        .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND,"Not found user with id: " + feedback.getUserId()));

    Feedback existingUserFeedback = feedbackRepository.findByUserIdAndProductId(feedback.getUserId(), feedback.getProductId());
    if (existingUserFeedback == null) {
      return feedbackRepository.save(feedback);
    } else {
      existingUserFeedback.setRating(feedback.getRating());
      existingUserFeedback.setContent(feedback.getContent());
      return feedbackRepository.save(existingUserFeedback);
    }
  }

  @Override
  public List<ProductRating> getProductRatingById(String productId) {
    List<Feedback> feedbacks = feedbackRepository.findByProductId(productId);
    int totalFeedbacks = feedbacks.size();

    if (totalFeedbacks == 0) {
      return IntStream.rangeClosed(1, 5)
          .mapToObj(star -> new ProductRating(star, 0, 0))
          .collect(Collectors.toList());
    }

    Map<Integer, Long> ratingCountMap = feedbacks.stream()
        .collect(Collectors.groupingBy(Feedback::getRating, Collectors.counting()));

    return IntStream.rangeClosed(1, 5)
        .mapToObj(star -> {
          long count = ratingCountMap.getOrDefault(star, 0L);
          int percent = (int) ((count * 100) / totalFeedbacks);
          return new ProductRating(star, Math.toIntExact(count), percent);
        })
        .collect(Collectors.toList());
  }

  private void validateProductExists(String productId) throws NotFoundException {
    productRepository.findById(productId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Not found product with id: " + productId));
  }
}