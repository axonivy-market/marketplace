package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
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
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
  private static final String ERROR_MESSAGE_FORMAT = "Not found feedback with id %s and version %s";
  private static final int MIN_STAR = 1;
  private static final int MAX_STAR = 5;
  private static final int MAX_PERCENT = 100;
  private final FeedbackRepository feedbackRepository;
  private final GithubUserRepository githubUserRepository;
  private final ProductRepository productRepository;

  @Override
  public Page<Feedback> findAllFeedbacks(Pageable pageable) {
    Page<FeedbackProjection> results = feedbackRepository.findFeedbackWithProductNames(pageable);
    List<Feedback> feedbacks = results.getContent().stream()
        .map(FeedbackServiceImpl::mapToFeedback)
        .toList();
    return new PageImpl<>(feedbacks, pageable, results.getTotalElements());
  }

  private static Feedback mapToFeedback(FeedbackProjection feedbackProjection) {
    var feedback = Feedback.builder()
        .userId(feedbackProjection.getUserId())
        .productId(feedbackProjection.getProductId())
        .content(feedbackProjection.getContent())
        .rating(feedbackProjection.getRating())
        .feedbackStatus(feedbackProjection.getFeedbackStatus())
        .moderatorName(feedbackProjection.getModeratorName())
        .reviewDate(feedbackProjection.getReviewDate())
        .version(feedbackProjection.getVersion())
        .productNames(feedbackProjection.getProductNames())
        .build();

    feedback.setId(feedbackProjection.getId());
    feedback.setCreatedAt(feedbackProjection.getCreatedAt());
    feedback.setUpdatedAt(feedbackProjection.getUpdatedAt());

    return feedback;
  }

  @Override
  public Page<Feedback> findFeedbacks(String productId, Pageable pageable) {
    validateProductExists(productId);
    Pageable refinedPageable = PageRequest.of(0, CommonConstants.PAGE_SIZE_10);
    if (pageable != null) {
      refinedPageable = refinePagination(pageable);
    }

    List<Feedback> feedbacks = feedbackRepository.findByProductIdAndIsLatestTrueAndFeedbackStatusNotIn(productId,
        List.of(FeedbackStatus.REJECTED, FeedbackStatus.PENDING), refinedPageable);
    return new PageImpl<>(feedbacks, refinedPageable, feedbacks.size());
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

    List<Feedback> existingUserFeedback =
        feedbackRepository.findByProductIdAndUserIdAndIsLatestTrueAndFeedbackStatusNotIn(
            productId, userId, List.of(FeedbackStatus.REJECTED));
    if (existingUserFeedback == null) {
      throw new NoContentException(ErrorCode.NO_FEEDBACK_OF_USER_FOR_PRODUCT,
          String.format("No feedback with user id '%s' and product id '%s'", userId, productId));
    }
    return existingUserFeedback;
  }

  @Override
  public Feedback updateFeedbackWithNewStatus(FeedbackApprovalModel feedbackApproval) {
    return feedbackRepository.findByIdAndVersion(feedbackApproval.getFeedbackId(), feedbackApproval.getVersion())
        .map((Feedback existingFeedback) -> {
          applyUpdatesToFeedback(feedbackApproval, existingFeedback);

          return feedbackRepository.save(existingFeedback);
        }).orElseThrow(() -> new NotFoundException(ErrorCode.FEEDBACK_NOT_FOUND,
            String.format(ERROR_MESSAGE_FORMAT, feedbackApproval.getFeedbackId(), feedbackApproval.getVersion())
        ));
  }

  private void applyUpdatesToFeedback(FeedbackApprovalModel feedbackApproval, Feedback existingFeedback) {
    boolean isApproved = BooleanUtils.isTrue(feedbackApproval.getIsApproved());
    var newStatus = FeedbackStatus.REJECTED;
    if (isApproved) {
      newStatus = FeedbackStatus.APPROVED;
      List<Feedback> approvedLatestFeedbacks = feedbackRepository
          .findByProductIdAndUserIdAndIsLatestTrueAndFeedbackStatusNotIn(feedbackApproval.getProductId(),
              feedbackApproval.getUserId(), List.of(FeedbackStatus.REJECTED, FeedbackStatus.PENDING));

      if (ObjectUtils.isNotEmpty(approvedLatestFeedbacks)) {
        var currentLatest = approvedLatestFeedbacks.get(0);
        currentLatest.setIsLatest(null);
        feedbackRepository.save(currentLatest);
      }
    }
    existingFeedback.setFeedbackStatus(newStatus);
    existingFeedback.setModeratorName(feedbackApproval.getModeratorName());
    existingFeedback.setReviewDate(LocalDateTime.now());
    if (isApproved) {
      existingFeedback.setIsLatest(true);
    } else {
      existingFeedback.setIsLatest(null);
    }
  }

  @Override
  public Feedback upsertFeedback(FeedbackModelRequest feedback, String userId) throws NotFoundException {
    validateUserExists(userId);
    List<Feedback> feedbacks = feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn(
        feedback.getProductId(), userId, List.of(FeedbackStatus.REJECTED));

    var pendingFeedback = findFeedbackByStatus(feedbacks, FeedbackStatus.PENDING)
        .stream().findFirst().orElse(null);
    List<Feedback> approvedFeedbacks = findFeedbackByStatus(feedbacks, FeedbackStatus.APPROVED);

    var matchingApprovedFeedback = getMatchingApprovedFeedback(approvedFeedbacks, feedback);
    if (matchingApprovedFeedback != null) {
      if (pendingFeedback != null) {
        feedbackRepository.delete(pendingFeedback);
      }

      Feedback currentLatestApproved = feedbackRepository
          .findByProductIdAndUserIdAndIsLatestTrueAndFeedbackStatusNotIn(
              feedback.getProductId(), userId, List.of(FeedbackStatus.REJECTED)
          ).stream().findFirst().orElse(null);

      if (currentLatestApproved != null && !currentLatestApproved.getId().equals(matchingApprovedFeedback.getId())) {
        currentLatestApproved.setIsLatest(null);
      }
      matchingApprovedFeedback.setIsLatest(true);
      feedbackRepository.saveAll(List.of(Objects.requireNonNull(currentLatestApproved), matchingApprovedFeedback));

      return matchingApprovedFeedback;
    }

    return saveOrUpdateFeedback(pendingFeedback, feedback, userId);
  }

  private static Feedback getMatchingApprovedFeedback(List<Feedback> approvedFeedbacks, FeedbackModelRequest feedback) {
    return approvedFeedbacks.stream()
        .filter(existing ->
            existing.getContent().trim().equalsIgnoreCase(feedback.getContent().trim()) &&
                existing.getRating().equals(feedback.getRating())
        ).findFirst()
        .orElse(null);
  }

  private static List<Feedback> findFeedbackByStatus(List<Feedback> feedbacks, FeedbackStatus feedbackStatus) {
    return feedbacks.stream()
        .filter((Feedback feedback) -> {
          if (FeedbackStatus.APPROVED == feedbackStatus) {
            return feedbackStatus == feedback.getFeedbackStatus() || feedback.getFeedbackStatus() == null;
          }
          return feedbackStatus == feedback.getFeedbackStatus();
        }).toList();
  }

  private Feedback saveOrUpdateFeedback(Feedback pendingFeedback, FeedbackModelRequest feedbackModel, String userId) {
    if (pendingFeedback == null) {
      pendingFeedback = new Feedback();
      pendingFeedback.setUserId(userId);
      pendingFeedback.setProductId(feedbackModel.getProductId());
      pendingFeedback.setFeedbackStatus(FeedbackStatus.PENDING);
      pendingFeedback.setIsLatest(true);
    }
    pendingFeedback.setRating(feedbackModel.getRating());
    pendingFeedback.setContent(feedbackModel.getContent());

    return feedbackRepository.save(pendingFeedback);
  }

  @Override
  public List<ProductRating> getProductRatingById(String productId) {
    List<Feedback> feedbacks = feedbackRepository.findByProductIdAndIsLatestTrueAndFeedbackStatusNotIn(productId,
        List.of(FeedbackStatus.PENDING, FeedbackStatus.REJECTED), Pageable.unpaged());
    int totalFeedbacks = feedbacks.size();

    if (totalFeedbacks == 0) {
      return IntStream.rangeClosed(MIN_STAR, MAX_STAR).mapToObj(star -> new ProductRating(star, 0, 0)).toList();
    }

    Map<Integer, Long> ratingCountMap = feedbacks.stream()
        .collect(Collectors.groupingBy(Feedback::getRating, Collectors.counting()));

    return IntStream.rangeClosed(MIN_STAR, MAX_STAR).mapToObj((int star) -> {
      long count = ratingCountMap.getOrDefault(star, 0L);
      int percent = (int) ((count * MAX_PERCENT) / totalFeedbacks);
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
    var pageRequest = (PageRequest) pageable;
    if (pageable != null) {
      List<Sort.Order> orders = new ArrayList<>();
      for (var sort : pageable.getSort()) {
        var feedbackSortOption = FeedbackSortOption.of(sort.getProperty());
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
