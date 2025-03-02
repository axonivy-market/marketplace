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
    return feedbackRepository.findById(feedbackApproval.getFeedbackId())
        .map(existingFeedback -> {
          List<Feedback> existingFeedbacks = customFeedbackRepository
              .findByUserIdAndProductId(existingFeedback.getUserId(), existingFeedback.getProductId());

          boolean isApproved = BooleanUtils.isTrue(feedbackApproval.getIsApproved());
          FeedbackStatus newFeedbackStatus = isApproved ? FeedbackStatus.APPROVED : FeedbackStatus.REJECTED;
          existingFeedbacks.stream()
              .filter(f -> f.getFeedbackStatus() == (isApproved ? FeedbackStatus.APPROVED : FeedbackStatus.PENDING))
              .filter(f -> !f.getId().equals(existingFeedback.getId()))
              .findFirst().ifPresent(feedbackRepository::delete);

          existingFeedback.setFeedbackStatus(newFeedbackStatus);
          existingFeedback.setModeratorName(feedbackApproval.getModeratorName());
          existingFeedback.setReviewDate(new Date());

          return feedbackRepository.save(existingFeedback);
        })
        .orElse(null);
  }

  @Override
  public Feedback upsertFeedback(FeedbackModelRequest feedback, String userId) throws NotFoundException {
    validateUserExists(userId);
    List<Feedback> feedbacks = customFeedbackRepository.findByUserIdAndProductId(userId, feedback.getProductId());

    Feedback approvedFeedback = feedbacks.stream()
        .filter(fb -> fb.getFeedbackStatus() == FeedbackStatus.APPROVED)
        .findFirst()
        .orElse(null);

    Feedback pendingFeedback = feedbacks.stream()
        .filter(fb -> fb.getFeedbackStatus() == FeedbackStatus.PENDING)
        .findFirst()
        .orElse(null);

    Feedback rejectedFeedback = feedbacks.stream()
        .filter(fb -> fb.getFeedbackStatus() == FeedbackStatus.REJECTED)
        .filter(fb -> isMatchingFeedback(fb, feedback)) // Check if content & rating match
        .findFirst()
        .orElse(null);

    if (approvedFeedback != null && isMatchingFeedback(approvedFeedback, feedback)) {
      if (pendingFeedback != null) {
        feedbackRepository.delete(pendingFeedback);
      }
      return approvedFeedback;
    }

    if (rejectedFeedback != null) {
      return rejectedFeedback; // Return existing rejected feedback instead of creating a new pending one
    }

    return saveOrUpdatePendingFeedback(pendingFeedback, feedback, userId);
  }

  private boolean isMatchingFeedback(Feedback approvedFeedback, FeedbackModelRequest feedback) {
    return approvedFeedback.getContent().equals(feedback.getContent()) &&
        approvedFeedback.getRating().equals(feedback.getRating());
  }

  private Feedback saveOrUpdatePendingFeedback(Feedback pendingFeedback, FeedbackModelRequest feedback, String userId) {
    if (pendingFeedback == null) {
      pendingFeedback = new Feedback();
      pendingFeedback.setUserId(userId);
      pendingFeedback.setProductId(feedback.getProductId());
      pendingFeedback.setFeedbackStatus(FeedbackStatus.PENDING);
      pendingFeedback.setRating(feedback.getRating());
      pendingFeedback.setContent(feedback.getContent());
      return feedbackRepository.insert(pendingFeedback);
    }

    pendingFeedback.setRating(feedback.getRating());
    pendingFeedback.setContent(feedback.getContent());
    return feedbackRepository.save(pendingFeedback);
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
