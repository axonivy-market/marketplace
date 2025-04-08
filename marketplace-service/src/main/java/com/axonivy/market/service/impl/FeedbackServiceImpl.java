package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.enums.FeedbackSortOption;
import com.axonivy.market.enums.FeedbackStatus;
import com.axonivy.market.exceptions.model.NoContentException;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.FeedbackApprovalModel;
import com.axonivy.market.model.FeedbackModelRequest;
import com.axonivy.market.model.FeedbackProjection;
import com.axonivy.market.model.ProductRating;
import com.axonivy.market.repository.FeedbackRepository;
import com.axonivy.market.repository.GithubUserRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FeedbackService;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class FeedbackServiceImpl implements FeedbackService {

  private final FeedbackRepository feedbackRepository;
  private final GithubUserRepository githubUserRepository;
  private final ProductRepository productRepository;
  private static final String ERROR_MESSAGE_FORMAT = "Not found feedback with id %s and version %s";

  public FeedbackServiceImpl(FeedbackRepository feedbackRepository, GithubUserRepository githubUserRepository,
      ProductRepository productRepository) {
    this.feedbackRepository = feedbackRepository;
    this.githubUserRepository = githubUserRepository;
    this.productRepository = productRepository;
  }

  @Override
  public Page<Feedback> findAllFeedbacks(Pageable pageable) {
    Page<FeedbackProjection> results = feedbackRepository.findFeedbackWithProductNames(pageable);
    List<Feedback> feedbacks = results.getContent().stream()
        .map(result -> {
          Feedback feedback = Feedback.builder()
              .userId(result.getUserId())
              .productId(result.getProductId())
              .content(result.getContent())
              .rating(result.getRating())
              .feedbackStatus(result.getFeedbackStatus())
              .moderatorName(result.getModeratorName())
              .reviewDate(result.getReviewDate())
              .version(result.getVersion())
              .productNames(result.getProductNames())  // Convert JSON to Map
              .build();

          feedback.setId(result.getId());
          feedback.setCreatedAt(result.getCreatedAt());
          feedback.setUpdatedAt(result.getUpdatedAt());

          return feedback;
        }).toList();
    return new PageImpl<>(feedbacks, pageable, results.getTotalElements());
  }

  @Override
  public Page<Feedback> findFeedbacks(String productId, Pageable pageable) {
    validateProductExists(productId);
    List<Feedback> feedbacks = feedbackRepository.findLatestApprovedFeedbacks(productId,
        List.of(FeedbackStatus.REJECTED,
            FeedbackStatus.PENDING), refinePagination(pageable));
    return new PageImpl<>(feedbacks, pageable, feedbacks.size());
  }

  @Override
  public Feedback findFeedback(String id) throws NotFoundException {
    return feedbackRepository.findById(id).orElseThrow(
        () -> new NotFoundException(ErrorCode.FEEDBACK_NOT_FOUND, "Not found feedback with id: " + id));
  }

  @Override
  public List<Feedback> findFeedbackByUserIdAndProductId(String userId,
      String productId) throws NotFoundException, NoContentException {
    if (StringUtils.isNotBlank(userId)) {
      validateUserExists(userId);
    }
    validateProductExists(productId);

    List<Feedback> existingUserFeedback = feedbackRepository.findFeedbacksByUser(productId,
        userId, List.of(FeedbackStatus.REJECTED));
    if (existingUserFeedback == null) {
      throw new NoContentException(ErrorCode.NO_FEEDBACK_OF_USER_FOR_PRODUCT,
          String.format("No feedback with user id '%s' and product id '%s'", userId, productId));
    }
    return existingUserFeedback;
  }

  @Override
  public Feedback updateFeedbackWithNewStatus(FeedbackApprovalModel feedbackApproval) {
    return feedbackRepository.findByIdAndVersion(feedbackApproval.getFeedbackId(), feedbackApproval.getVersion()).map(
        existingFeedback -> {
          boolean isApproved = BooleanUtils.isTrue(feedbackApproval.getIsApproved());
          FeedbackStatus newFeedbackStatus = isApproved ? FeedbackStatus.APPROVED : FeedbackStatus.REJECTED;

          existingFeedback.setFeedbackStatus(newFeedbackStatus);
          existingFeedback.setModeratorName(feedbackApproval.getModeratorName());
          existingFeedback.setReviewDate(new Date());
          return feedbackRepository.save(existingFeedback);
        }).orElseThrow(() -> new NotFoundException(ErrorCode.FEEDBACK_NOT_FOUND,
        String.format(ERROR_MESSAGE_FORMAT, feedbackApproval.getFeedbackId(), feedbackApproval.getVersion())));
  }

  @Override
  public Feedback upsertFeedback(FeedbackModelRequest feedback, String userId) throws NotFoundException {
    validateUserExists(userId);
    List<Feedback> feedbacks = feedbackRepository.findFeedbacksByUser(
        feedback.getProductId(), userId, List.of(FeedbackStatus.REJECTED));

    Feedback approvedFeedback = findFeedbackByStatus(feedbacks, FeedbackStatus.APPROVED);
    Feedback pendingFeedback = findFeedbackByStatus(feedbacks, FeedbackStatus.PENDING);

    if (approvedFeedback != null && isMatchingWithExistingFeedback(approvedFeedback, feedback)) {
      if (pendingFeedback != null) {
        feedbackRepository.delete(pendingFeedback);
      }
      return approvedFeedback;
    }

    return saveOrUpdateFeedback(pendingFeedback, feedback, userId);
  }

  private Feedback findFeedbackByStatus(List<Feedback> feedbacks, FeedbackStatus feedbackStatus) {
    return feedbacks.stream()
        .filter(feedback -> {
          if (FeedbackStatus.APPROVED == feedbackStatus) {
            return feedbackStatus == feedback.getFeedbackStatus() || feedback.getFeedbackStatus() == null;
          }
          return feedbackStatus == feedback.getFeedbackStatus();
        })
        .findFirst()
        .orElse(null);
  }

  private boolean isMatchingWithExistingFeedback(Feedback approvedFeedback, FeedbackModelRequest feedback) {
    return approvedFeedback.getContent().trim().equals(feedback.getContent().trim()) &&
        approvedFeedback.getRating().equals(feedback.getRating());
  }

  private Feedback saveOrUpdateFeedback(Feedback pendingFeedback, FeedbackModelRequest feedbackModel, String userId) {
    if (pendingFeedback == null) {
      pendingFeedback = new Feedback();
      pendingFeedback.setUserId(userId);
      pendingFeedback.setProductId(feedbackModel.getProductId());
      pendingFeedback.setFeedbackStatus(FeedbackStatus.PENDING);
    }
    pendingFeedback.setRating(feedbackModel.getRating());
    pendingFeedback.setContent(feedbackModel.getContent());

    return feedbackRepository.save(pendingFeedback);
  }

  @Override
  public List<ProductRating> getProductRatingById(String productId) {
    List<Feedback> feedbacks = feedbackRepository.findLatestApprovedFeedbacks(productId,
        List.of(FeedbackStatus.PENDING, FeedbackStatus.REJECTED), Pageable.unpaged());
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
    if (githubUserRepository.findById(userId).isEmpty()) {
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
