package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.enums.FeedbackSortOption;
import com.axonivy.market.enums.FeedbackStatus;
import com.axonivy.market.exceptions.model.NoContentException;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.FeedbackApprovalModel;
import com.axonivy.market.model.FeedbackModelRequest;
import com.axonivy.market.model.ProductRating;
import com.axonivy.market.repository.CustomFeedbackRepository;
import com.axonivy.market.repository.FeedbackRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.repository.UserRepository;
import com.axonivy.market.service.FeedbackService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class FeedbackServiceImpl implements FeedbackService {

  private final FeedbackRepository feedbackRepository;
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final CustomFeedbackRepository customFeedbackRepository;

  public FeedbackServiceImpl(FeedbackRepository feedbackRepository, UserRepository userRepository,
      ProductRepository productRepository, CustomFeedbackRepository customFeedbackRepository) {
    this.feedbackRepository = feedbackRepository;
    this.userRepository = userRepository;
    this.productRepository = productRepository;
    this.customFeedbackRepository = customFeedbackRepository;
  }

  @Override
  public Page<Feedback> findAllFeedbacks(Pageable pageable) {
    return feedbackRepository.findAll(pageable);
  }

  @Override
  public Page<Feedback> findFeedbacks(String productId, Pageable pageable) throws NotFoundException {
    validateProductExists(productId);
    return customFeedbackRepository.searchByProductId(productId, refinePagination(pageable));
  }

  @Override
  public Feedback findFeedback(String id) throws NotFoundException {
    return feedbackRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.FEEDBACK_NOT_FOUND, "Not found feedback with id: " + id));
  }

  @Override
  public List<Feedback> findFeedbackByUserIdAndProductId(String userId,
      String productId) throws NotFoundException, NoContentException {
    if (StringUtils.isNotBlank(userId)) {
      validateUserExists(userId);
    }
    validateProductExists(productId);

    List<Feedback> existingUserFeedback = customFeedbackRepository.findByUserIdAndProductId(userId, productId);
    if (existingUserFeedback == null) {
      throw new NoContentException(ErrorCode.NO_FEEDBACK_OF_USER_FOR_PRODUCT,
          String.format("No feedback with user id '%s' and product id '%s'", userId, productId));
    }
    return existingUserFeedback;
  }

  @Override
  public Feedback updateFeedbackWithNewStatus(FeedbackApprovalModel feedbackApproval) {
//    Feedback existingUserFeedback = feedbackRepository.findById(feedbackApproval.getFeedbackId()).orElse(null);
//    if (existingUserFeedback != null) {
//      existingUserFeedback.setFeedbackStatus(
//          BooleanUtils.isTrue(feedbackApproval.getIsApproved()) ? FeedbackStatus.APPROVED : FeedbackStatus.REJECTED);
//      existingUserFeedback.setModeratorName(feedbackApproval.getModeratorName());
//      existingUserFeedback.setReviewDate(new Date());
//      feedbackRepository.save(existingUserFeedback);
//    }
//
//    if (currentApproved != null && feedback.getFeedbackStatus() == FeedbackStatus.APPROVED) {
//      feedbackRepository.delete(currentApproved);
//      Feedback newApproved = new Feedback();
//      newApproved.setUserId(userId);
//      newApproved.setProductId(feedback.getProductId());
//      newApproved.setRating(feedback.getRating());
//      newApproved.setContent(feedback.getContent());
//      newApproved.setFeedbackStatus(FeedbackStatus.APPROVED);
//      return feedbackRepository.insert(newApproved);
//    }
    Feedback existingUserFeedback = feedbackRepository.findById(feedbackApproval.getFeedbackId()).orElse(null);
    if (existingUserFeedback != null && feedbackApproval.getIsApproved() != null) {
      // If approving and there's an existing approved feedback for the same user/product
      List<Feedback> existingFeedbacks = customFeedbackRepository
          .findByUserIdAndProductId(existingUserFeedback.getUserId(),
              existingUserFeedback.getProductId());

      Feedback existingApproved = existingFeedbacks.stream()
          .filter(f -> f.getFeedbackStatus() == FeedbackStatus.APPROVED)
          .findFirst()
          .orElse(null);

      if (existingApproved != null && !existingApproved.getId().equals(existingUserFeedback.getId())) {
        feedbackRepository.delete(existingApproved);
      }

      existingUserFeedback.setFeedbackStatus(
          BooleanUtils.isTrue(feedbackApproval.getIsApproved()) ?
              FeedbackStatus.APPROVED : FeedbackStatus.REJECTED);
      existingUserFeedback.setModeratorName(feedbackApproval.getModeratorName());
      existingUserFeedback.setReviewDate(new Date());
      return feedbackRepository.save(existingUserFeedback);
    }

    return existingUserFeedback;
  }

  @Override
  public Feedback upsertFeedback(FeedbackModelRequest feedback, String userId) throws NotFoundException {
    validateUserExists(userId);
    List<Feedback> feedbacks = customFeedbackRepository.findByUserIdAndProductId(userId,
        feedback.getProductId());

    if (feedbacks.isEmpty()) {
      Feedback newFeedback = new Feedback();
      newFeedback.setUserId(userId);
      newFeedback.setProductId(feedback.getProductId());
      newFeedback.setRating(feedback.getRating());
      newFeedback.setContent(feedback.getContent());
      newFeedback.setFeedbackStatus(FeedbackStatus.PENDING);
      return feedbackRepository.insert(newFeedback);
    }

    boolean haveApprovedFeedback =
        feedbacks.stream().anyMatch(fb -> fb.getFeedbackStatus() == FeedbackStatus.APPROVED);
    Feedback currentPendingFeedback =
        feedbacks.stream().filter(f -> f.getFeedbackStatus() == FeedbackStatus.PENDING).findFirst().orElse(null);

    if (haveApprovedFeedback && currentPendingFeedback == null) {
      Feedback newFeedback = new Feedback();
      newFeedback.setUserId(userId);
      newFeedback.setProductId(feedback.getProductId());
      newFeedback.setRating(feedback.getRating());
      newFeedback.setContent(feedback.getContent());
      newFeedback.setFeedbackStatus(FeedbackStatus.PENDING);
      return feedbackRepository.insert(newFeedback);
    }

    Objects.requireNonNull(currentPendingFeedback).setRating(feedback.getRating());
    currentPendingFeedback.setContent(feedback.getContent());
    currentPendingFeedback.setFeedbackStatus(FeedbackStatus.PENDING);
    return feedbackRepository.save(currentPendingFeedback);
  }

  @Override
  public List<ProductRating> getProductRatingById(String productId) {
    List<Feedback> feedbacks = feedbackRepository.findByProductIdAndFeedbackStatusNotIn(productId,
        Arrays.asList(FeedbackStatus.PENDING, FeedbackStatus.REJECTED));
    int totalFeedbacks = feedbacks.size();

    if (totalFeedbacks == 0) {
      return IntStream.rangeClosed(1, 5).mapToObj(star -> new ProductRating(star, 0, 0)).toList();
    }

    Map<Integer, Long> ratingCountMap = feedbacks.stream()
        .collect(Collectors.groupingBy(Feedback::getRating, Collectors.counting()));

    return IntStream.rangeClosed(1, 5).mapToObj(star -> {
      long count = ratingCountMap.getOrDefault(star, 0L);
      int percent = (int) ((count * 100) / totalFeedbacks);
      return new ProductRating(star, Math.toIntExact(count), percent);
    }).toList();
  }

  public void validateProductExists(String productId) throws NotFoundException {
    if (productRepository.findById(productId).isEmpty()) {
      throw new NotFoundException(ErrorCode.PRODUCT_NOT_FOUND, "Not found product with id: " + productId);
    }
  }

  public void validateUserExists(String userId) {
    if (userRepository.findById(userId).isEmpty()) {
      throw new NotFoundException(ErrorCode.USER_NOT_FOUND, "Not found user with id: " + userId);
    }
  }

  private Pageable refinePagination(Pageable pageable) {
    PageRequest pageRequest = (PageRequest) pageable;
    if (pageable != null) {
      List<Sort.Order> orders = new ArrayList<>();
      for (var sort : pageable.getSort()) {
        FeedbackSortOption feedbackSortOption = FeedbackSortOption.of(sort.getProperty());
        List<Sort.Order> order = createOrder(feedbackSortOption);
        orders.addAll(order);
      }
      pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }
    return pageRequest;
  }

  public List<Sort.Order> createOrder(FeedbackSortOption feedbackSortOption) {
    String[] fields = feedbackSortOption.getCode().split(StringUtils.SPACE);
    List<Sort.Direction> directions = feedbackSortOption.getDirections();

    if (fields.length != directions.size()) {
      throw new IllegalArgumentException("The number of fields and directions must match.");
    }

    return IntStream.range(0, fields.length)
        .mapToObj(i -> new Sort.Order(directions.get(i), fields[i]))
        .toList();
  }
}
