package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.repository.FeedbackRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.repository.UserRepository;
import com.axonivy.market.service.FeedbackService;
import com.axonivy.market.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeedbackServiceImpl implements FeedbackService {

  private final FeedbackRepository feedbackRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final UserService userService;

  public FeedbackServiceImpl(FeedbackRepository feedbackRepository, UserRepository userRepository, ProductRepository productRepository, UserService userService) {
    this.feedbackRepository = feedbackRepository;
    this.userRepository = userRepository;
    this.productRepository = productRepository;
    this.userService = userService;
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
      incrementStarCount(product, feedback.getRating());
      productRepository.save(product);
      return feedbackRepository.save(feedback);
    } else {
      Feedback existingFeedback = existingFeedbacks.get(0);
      if (!existingFeedback.getRating().equals(feedback.getRating())) {
        updateStarCount(product, existingFeedback.getRating(), feedback.getRating());
      }
      existingFeedback.setRating(feedback.getRating());
      existingFeedback.setContent(feedback.getContent());
      productRepository.save(product);
      return feedbackRepository.save(existingFeedback);
    }
  }

  private void validateProductExists(String productId) throws NotFoundException {
    productRepository.findById(productId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Not found product with id: " + productId));
  }

  private void incrementStarCount(Product product, int rating) {
    switch (rating) {
      case 1:
        product.setOneStarCount(getSafeCount(product.getOneStarCount()) + 1);
        break;
      case 2:
        product.setTwoStarCount(getSafeCount(product.getTwoStarCount()) + 1);
        break;
      case 3:
        product.setThreeStarCount(getSafeCount(product.getThreeStarCount()) + 1);
        break;
      case 4:
        product.setFourStarCount(getSafeCount(product.getFourStarCount()) + 1);
        break;
      case 5:
        product.setFiveStarCount(getSafeCount(product.getFiveStarCount()) + 1);
        break;
      default:
        throw new IllegalArgumentException("Invalid rating: " + rating);
    }
  }

  private void decrementStarCount(Product product, int rating) {
    switch (rating) {
      case 1:
        if (getSafeCount(product.getOneStarCount()) > 0) {
          product.setOneStarCount(product.getOneStarCount() - 1);
        }
        break;
      case 2:
        if (getSafeCount(product.getTwoStarCount()) > 0) {
          product.setTwoStarCount(product.getTwoStarCount() - 1);
        }
        break;
      case 3:
        if (getSafeCount(product.getThreeStarCount()) > 0) {
          product.setThreeStarCount(product.getThreeStarCount() - 1);
        }
        break;
      case 4:
        if (getSafeCount(product.getFourStarCount()) > 0) {
          product.setFourStarCount(product.getFourStarCount() - 1);
        }
        break;
      case 5:
        if (getSafeCount(product.getFiveStarCount()) > 0) {
          product.setFiveStarCount(product.getFiveStarCount() - 1);
        }
        break;
      default:
        throw new IllegalArgumentException("Invalid rating: " + rating);
    }
  }


  private void updateStarCount(Product product, int oldRating, int newRating) {
    decrementStarCount(product, oldRating);
    incrementStarCount(product, newRating);
  }

  private int getSafeCount(Integer count) {
    return count == null ? 0 : count;
  }
}
