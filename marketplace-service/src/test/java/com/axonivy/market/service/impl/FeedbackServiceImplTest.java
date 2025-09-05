package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.enums.FeedbackStatus;
import com.axonivy.market.exceptions.model.NoContentException;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.FeedbackApprovalModel;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.model.FeedbackModelRequest;
import com.axonivy.market.model.FeedbackProjection;
import com.axonivy.market.model.ProductRating;
import com.axonivy.market.repository.FeedbackRepository;
import com.axonivy.market.repository.GithubUserRepository;
import com.axonivy.market.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest extends BaseSetup {

  @Mock
  private FeedbackRepository feedbackRepository;

  @Mock
  private GithubUserRepository githubUserRepository;

  @Mock
  private ProductRepository productRepository;

  @InjectMocks
  private FeedbackServiceImpl feedbackService;

  private Feedback feedback;
  private FeedbackModel feedbackModel;
  private FeedbackModelRequest feedbackModelRequest;
  private String userId;

  @BeforeEach
  void setUp() {
    userId = "user1";

    feedback = new Feedback();
    feedback.setId("1");
    feedback.setUserId(userId);
    feedback.setProductId("product1");
    feedback.setRating(5);
    feedback.setContent("Great product!");
    feedback.setFeedbackStatus(FeedbackStatus.PENDING);
    feedback.setVersion(3);

    feedbackModel = new FeedbackModel();
    feedbackModel.setUserId(userId);
    feedbackModel.setProductId("product1");
    feedbackModel.setRating(5);
    feedbackModel.setContent("Great product!");

    feedbackModelRequest = new FeedbackModelRequest();
    feedbackModelRequest.setProductId("product1");
    feedbackModelRequest.setRating(5);
    feedbackModelRequest.setContent("Great product!");
  }

  @Test
  void testFindAllFeedbacks() {
    Pageable pageable = PageRequest.of(0, 20);
    FeedbackProjection feedbackProjection = mock(FeedbackProjection.class);
    when(feedbackProjection.getId()).thenReturn("1");
    when(feedbackProjection.getUserId()).thenReturn("user1");
    when(feedbackProjection.getProductId()).thenReturn("product1");
    when(feedbackProjection.getContent()).thenReturn("Great product!");
    when(feedbackProjection.getRating()).thenReturn(5);
    when(feedbackProjection.getFeedbackStatus()).thenReturn(FeedbackStatus.APPROVED);
    when(feedbackProjection.getModeratorName()).thenReturn("moderator");
    when(feedbackProjection.getReviewDate()).thenReturn(new Date());
    when(feedbackProjection.getVersion()).thenReturn(1);
    when(feedbackProjection.getCreatedAt()).thenReturn(new Date());
    when(feedbackProjection.getUpdatedAt()).thenReturn(new Date());
    when(feedbackProjection.getProductNames()).thenReturn(Map.of("en", "Product Name"));

    Page<FeedbackProjection> projectionPage = new PageImpl<>(List.of(feedbackProjection), pageable, 1);
    when(feedbackRepository.findFeedbackWithProductNames(pageable)).thenReturn(projectionPage);

    Page<Feedback> result = feedbackService.findAllFeedbacks(pageable);

    assertNotNull(result, "Resulting page of feedbacks should not be null");
    assertEquals(1, result.getTotalElements(), "Total elements in page should be 1");
    assertEquals(1, result.getContent().size(), "Content list size should be 1");

    Feedback feedback = result.getContent().get(0);

    assertEquals("user1", feedback.getUserId(), "Feedback userId should match mocked value");
    assertEquals("product1", feedback.getProductId(), "Feedback productId should match mocked value");
    assertEquals("Great product!", feedback.getContent(), "Feedback content should match mocked value");
    assertEquals(5, feedback.getRating(), "Feedback rating should match mocked value");
    assertEquals(FeedbackStatus.APPROVED, feedback.getFeedbackStatus(), "Feedback status should match mocked value");
    assertEquals("moderator", feedback.getModeratorName(), "Moderator name should match mocked value");
    assertEquals(1, feedback.getVersion(), "Feedback version should match mocked value");
    assertEquals("Product Name", feedback.getProductNames().get("en"), "Product name in English should match mocked value");

    verify(feedbackRepository, times(1)).findFeedbackWithProductNames(pageable);
  }

  @Test
  void testFindAllFeedbacksEmptyResult() {
    Pageable pageable = PageRequest.of(0, 20);
    Page<FeedbackProjection> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
    when(feedbackRepository.findFeedbackWithProductNames(pageable)).thenReturn(emptyPage);

    Page<Feedback> result = feedbackService.findAllFeedbacks(pageable);

    assertNotNull(result, "Resulting page should not be null even when repository returns empty");
    assertEquals(0, result.getTotalElements(), "Total elements in page should be 0 for empty result");
    assertTrue(result.getContent().isEmpty(), "Content list should be empty for empty result");

    verify(feedbackRepository, times(1)).findFeedbackWithProductNames(pageable);
  }

  @Test
  void testFindFeedbacks() throws NotFoundException {
    String productId = "product1";
    Pageable pageable = PageRequest.of(0, 10);
    List<Feedback> feedbackList = Collections.singletonList(feedback);

    when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
    when(feedbackRepository.findByProductIdAndIsLatestTrueAndFeedbackStatusNotIn(
        productId, List.of(FeedbackStatus.REJECTED, FeedbackStatus.PENDING), pageable)
    ).thenReturn(feedbackList);

    Page<Feedback> result = feedbackService.findFeedbacks(productId, pageable);

    assertNotNull(result, "Resulting page should not be null");
    assertEquals(1, result.getTotalElements(), "Total elements in page should match the number of feedbacks returned");
    assertEquals(feedbackList, result.getContent(), "Content of the page should match the mocked feedback list");

    verify(productRepository, times(1)).findById(productId);
    verify(feedbackRepository, times(1)).findByProductIdAndIsLatestTrueAndFeedbackStatusNotIn(
        productId, List.of(FeedbackStatus.REJECTED, FeedbackStatus.PENDING), pageable);
  }

  @Test
  void testFindFeedbacksProductNotFound() {
    String productId = "product1";
    Pageable pageable = PageRequest.of(0, 10);

    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> feedbackService.findFeedbacks(productId, pageable),
        "Expected NotFoundException when product is not found");

    assertEquals(ErrorCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode(),
        "Exception code should indicate PRODUCT_NOT_FOUND");

    verify(productRepository, times(1)).findById(productId);
    verify(feedbackRepository, times(0)).findByProductIdAndIsLatestTrueAndFeedbackStatusNotIn(
        productId, List.of(FeedbackStatus.REJECTED, FeedbackStatus.PENDING), pageable);
  }

  @Test
  void testFindFeedback() throws NotFoundException {
    String feedbackId = "1";

    when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedback));

    Feedback result = feedbackService.findFeedback(feedbackId);
    assertNotNull(result, "Returned feedback should not be null");
    assertEquals(feedbackId, result.getId(), "Feedback ID should match the requested ID");
    verify(feedbackRepository, times(1)).findById(feedbackId);
  }

  @Test
  void testFindFeedbackNotFound() {
    String feedbackId = "1";

    when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> feedbackService.findFeedback(feedbackId),
        "Expected NotFoundException when feedback is not found");

    assertEquals(ErrorCode.FEEDBACK_NOT_FOUND.getCode(), exception.getCode(),
        "Exception code should indicate FEEDBACK_NOT_FOUND");
    verify(feedbackRepository, times(1)).findById(feedbackId);
  }

  @Test
  void testFindFeedbackByUserIdAndProductId() {
    String productId = "product1";

    when(githubUserRepository.findById(userId)).thenReturn(Optional.of(new GithubUser()));
    when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
    when(feedbackRepository.findByProductIdAndUserIdAndIsLatestTrueAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED))).thenReturn(List.of(feedback));

    var result = feedbackService.findFeedbackByUserIdAndProductId(userId, productId);

    assertNotNull(result, "Returned feedback list should not be null");
    assertEquals(userId, result.get(0).getUserId(), "Feedback userId should match the requested userId");
    assertEquals(productId, result.get(0).getProductId(), "Feedback productId should match the requested productId");

    verify(githubUserRepository, times(1)).findById(userId);
    verify(productRepository, times(1)).findById(productId);
    verify(feedbackRepository, times(1))
        .findByProductIdAndUserIdAndIsLatestTrueAndFeedbackStatusNotIn(productId, userId, List.of(FeedbackStatus.REJECTED));
  }

  @Test
  void testFindFeedbackByUserIdAndProductIdNoContent() {
    String productId = "product1";
    userId = "";
    when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
    when(feedbackRepository.findByProductIdAndUserIdAndIsLatestTrueAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED))).thenReturn(null);

    NoContentException exception = assertThrows(NoContentException.class,
        () -> feedbackService.findFeedbackByUserIdAndProductId(userId, productId),
        "Expected NoContentException when no feedback exists for the given user and product");

    assertEquals(ErrorCode.NO_FEEDBACK_OF_USER_FOR_PRODUCT.getCode(), exception.getCode(),
        "Exception code should indicate NO_FEEDBACK_OF_USER_FOR_PRODUCT");

    verify(productRepository, times(1)).findById(productId);
    verify(feedbackRepository, times(1))
        .findByProductIdAndUserIdAndIsLatestTrueAndFeedbackStatusNotIn(productId, userId, List.of(FeedbackStatus.REJECTED));
  }

  @Test
  void testFindFeedbackByUserIdAndProductIdNotFound() {
    userId = "notFoundUser";

    when(githubUserRepository.findById(userId)).thenReturn(Optional.empty());
    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> feedbackService.findFeedbackByUserIdAndProductId(userId, "product"),
        "Expected NotFoundException when user is not found");

    assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), exception.getCode(),
        "Exception code should indicate USER_NOT_FOUND");
    verify(githubUserRepository, times(1)).findById(userId);
  }

  @Test
  void testUpdateFeedbackWithNewStatusApproved() {
    String feedbackId = "1";
    int version = 3;
    FeedbackApprovalModel approvalModel = mockFeedbackApproval();
    approvalModel.setIsApproved(true);
    approvalModel.setVersion(3);

    when(feedbackRepository.findByIdAndVersion(feedbackId, version)).thenReturn(Optional.of(feedback));
    when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

    Feedback result = feedbackService.updateFeedbackWithNewStatus(approvalModel);

    assertNotNull(result, "Updated feedback should not be null");
    assertEquals(feedbackId, result.getId(), "Feedback ID should match the requested ID");
    assertEquals(FeedbackStatus.APPROVED, result.getFeedbackStatus(), "Feedback status should be updated to APPROVED");
    assertEquals(approvalModel.getModeratorName(), result.getModeratorName(), "Moderator name should match the approval model");
    assertNotNull(result.getReviewDate(), "Review date should be set when feedback is approved");

    verify(feedbackRepository, times(1)).findByIdAndVersion(feedbackId, version);
    verify(feedbackRepository, times(1)).save(any(Feedback.class));
  }

  @Test
  void testUpdateFeedbackWithNewStatusRejected() {
    String feedbackId = "1";
    int version = 3;
    FeedbackApprovalModel approvalModel = mockFeedbackApproval();
    approvalModel.setIsApproved(false);
    approvalModel.setVersion(3);

    when(feedbackRepository.findByIdAndVersion(feedbackId, version)).thenReturn(Optional.of(feedback));
    when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

    Feedback result = feedbackService.updateFeedbackWithNewStatus(approvalModel);

    assertNotNull(result, "Updated feedback should not be null");
    assertEquals(feedbackId, result.getId(), "Feedback ID should match the requested ID");
    assertEquals(FeedbackStatus.REJECTED, result.getFeedbackStatus(), "Feedback status should be updated to REJECTED");
    assertEquals(approvalModel.getModeratorName(), result.getModeratorName(), "Moderator name should match the approval model");
    assertNotNull(result.getReviewDate(), "Review date should be set when feedback is rejected");

    verify(feedbackRepository, times(1)).findByIdAndVersion(feedbackId, version);
    verify(feedbackRepository, times(1)).save(any(Feedback.class));
  }

  @Test
  void testUpsertFeedbackInsert() throws NotFoundException {
    String productId = "product1";

    when(githubUserRepository.findById(userId)).thenReturn(Optional.of(new GithubUser()));
    when(feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED))).thenReturn(Collections.emptyList());
    when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

    Feedback result = feedbackService.upsertFeedback(feedbackModelRequest, userId);

    assertNotNull(result, "Upserted feedback should not be null");
    assertEquals(feedbackModel.getUserId(), result.getUserId(), "Feedback userId should match the request");
    assertEquals(feedbackModel.getProductId(), result.getProductId(), "Feedback productId should match the request");
    assertEquals(feedbackModel.getRating(), result.getRating(), "Feedback rating should match the request");
    assertEquals(feedbackModel.getContent(), result.getContent(), "Feedback content should match the request");

    verify(githubUserRepository, times(1)).findById(userId);
    verify(feedbackRepository, times(1)).findByProductIdAndUserIdAndFeedbackStatusNotIn(productId,
        userId, List.of(FeedbackStatus.REJECTED));
    verify(feedbackRepository, times(1)).save(any(Feedback.class));
  }

  @Test
  void testUpsertFeedbackUpdate() throws NotFoundException {
    String productId = "product1";

    when(githubUserRepository.findById(userId)).thenReturn(Optional.of(new GithubUser()));
    when(feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED))).thenReturn(List.of(feedback));
    when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

    Feedback result = feedbackService.upsertFeedback(feedbackModelRequest, userId);

    assertNotNull(result, "Upserted feedback should not be null");
    assertEquals(feedbackModel.getUserId(), result.getUserId(), "Feedback userId should match the request");
    assertEquals(feedbackModel.getProductId(), result.getProductId(), "Feedback productId should match the request");
    assertEquals(feedbackModel.getRating(), result.getRating(), "Feedback rating should match the request");
    assertEquals(feedbackModel.getContent(), result.getContent(), "Feedback content should match the request");
    assertEquals(FeedbackStatus.PENDING, result.getFeedbackStatus(), "Feedback status should be set to PENDING for updated feedback");

    verify(githubUserRepository, times(1)).findById(userId);
    verify(feedbackRepository, times(1)).findByProductIdAndUserIdAndFeedbackStatusNotIn(productId,
        userId, List.of(FeedbackStatus.REJECTED));
    verify(feedbackRepository, times(1)).save(any(Feedback.class));
  }

  @Test
  void testUpsertFeedbackMatchingApprovedFeedback() throws NotFoundException {
    String productId = "product1";

    Feedback existingApproved = new Feedback();
    existingApproved.setId("approved1");
    existingApproved.setUserId(userId);
    existingApproved.setProductId(productId);
    existingApproved.setRating(5);
    existingApproved.setContent("Great product!");
    existingApproved.setFeedbackStatus(FeedbackStatus.APPROVED);

    Feedback existingPending = new Feedback();
    existingPending.setId("pending1");
    existingPending.setFeedbackStatus(FeedbackStatus.PENDING);

    when(githubUserRepository.findById(userId)).thenReturn(Optional.of(new GithubUser()));
    when(feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn(
        productId, userId, List.of(FeedbackStatus.REJECTED)))
        .thenReturn(List.of(existingApproved, existingPending));

    when(feedbackRepository.findByProductIdAndUserIdAndIsLatestTrueAndFeedbackStatusNotIn(
        productId, userId, List.of(FeedbackStatus.REJECTED)))
        .thenReturn(Collections.singletonList(existingApproved));

    Feedback result = feedbackService.upsertFeedback(feedbackModelRequest, userId);

    assertNotNull(result, "Returned feedback should not be null");
    assertEquals(existingApproved, result, "Returned feedback should match the existing approved feedback");

    verify(githubUserRepository, times(1)).findById(userId);
    verify(feedbackRepository, times(1))
        .findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId, List.of(FeedbackStatus.REJECTED));
    verify(feedbackRepository, times(1)).delete(existingPending);
    verify(feedbackRepository, times(1)).saveAll(anyList());
  }

  @Test
  void testUpsertFeedbackNoMatchingFeedback() throws NotFoundException {
    String productId = "product1";

    Feedback existingApproved = new Feedback();
    existingApproved.setId("approved1");
    existingApproved.setUserId(userId);
    existingApproved.setProductId(productId);
    existingApproved.setRating(4);
    existingApproved.setContent("Different content");
    existingApproved.setFeedbackStatus(FeedbackStatus.APPROVED);

    when(githubUserRepository.findById(userId)).thenReturn(Optional.of(new GithubUser()));
    when(feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED))).thenReturn(List.of(existingApproved));
    when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

    Feedback result = feedbackService.upsertFeedback(feedbackModelRequest, userId);

    assertNotNull(result, "Upserted feedback should not be null");
    assertEquals(feedbackModelRequest.getRating(), result.getRating(), "Feedback rating should match the request");
    assertEquals(feedbackModelRequest.getContent(), result.getContent(), "Feedback content should match the request");
    assertEquals(FeedbackStatus.PENDING, result.getFeedbackStatus(), "Feedback status should be PENDING for new feedback");

    verify(githubUserRepository, times(1)).findById(userId);
    verify(feedbackRepository, times(1)).findByProductIdAndUserIdAndFeedbackStatusNotIn(productId,
        userId, List.of(FeedbackStatus.REJECTED));
    verify(feedbackRepository, times(1)).save(any(Feedback.class));
  }

  @Test
  void testGetProductRatingById() {
    String productId = "product1";
    List<Feedback> feedbacks = Collections.singletonList(feedback);
    List<FeedbackStatus> feedbackStatuses = Arrays.asList(FeedbackStatus.PENDING, FeedbackStatus.REJECTED);
    when(feedbackRepository.findByProductIdAndIsLatestTrueAndFeedbackStatusNotIn(productId, feedbackStatuses,
        Pageable.unpaged())).thenReturn(feedbacks);

    List<ProductRating> ratings = feedbackService.getProductRatingById(productId);

    assertNotNull(ratings, "Ratings list should not be null");
    assertEquals(5, ratings.size(), "Ratings list should contain 5 elements for 1–5 stars");
    assertEquals(1, ratings.get(4).getCommentNumber(), "Comment number for 5-star rating should be 1");
    assertEquals(100, ratings.get(4).getPercent(), "Percentage for 5-star rating should be 100");

    verify(feedbackRepository, times(1)).findByProductIdAndIsLatestTrueAndFeedbackStatusNotIn(
        productId, feedbackStatuses, Pageable.unpaged());
  }

  @Test
  void testGetProductRatingByIdNoFeedbacks() {
    String productId = "product1";

    when(feedbackRepository.findByProductIdAndIsLatestTrueAndFeedbackStatusNotIn(anyString(), anyList(),
        any())).thenReturn(
        Collections.emptyList());

    List<ProductRating> ratings = feedbackService.getProductRatingById(productId);

    assertNotNull(ratings, "Ratings list should not be null even when there are no feedbacks");
    assertEquals(5, ratings.size(), "Ratings list should always contain 5 elements for 1–5 stars");

    for (ProductRating rating : ratings) {
      assertEquals(0, rating.getCommentNumber(), "Comment number should be 0 when there are no feedbacks");
      assertEquals(0, rating.getPercent(), "Percentage should be 0 when there are no feedbacks");
    }
    verify(feedbackRepository, times(1)).findByProductIdAndIsLatestTrueAndFeedbackStatusNotIn(
        anyString(), anyList(), any());
  }

  @Test
  void testValidateProductExists() {
    String productId = "product1";

    when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));

    assertDoesNotThrow(() -> feedbackService.validateProductExists(productId),
        "validateProductExists should not throw an exception when the product exists");
    verify(productRepository, times(1)).findById(productId);
  }

  @Test
  void testValidateProductExistsNotFound() {
    String productId = "product1";

    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> feedbackService.validateProductExists(productId),
        "Expected NotFoundException when product does not exist");

    assertEquals(ErrorCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode(),
        "Exception code should indicate PRODUCT_NOT_FOUND");
    verify(productRepository, times(1)).findById(productId);
  }

  @Test
  void testValidateUserExists() {
    when(githubUserRepository.findById(userId)).thenReturn(Optional.of(new GithubUser()));

    assertDoesNotThrow(() -> feedbackService.validateUserExists(userId),
        "validateUserExists should not throw an exception when the user exists");
    verify(githubUserRepository, times(1)).findById(userId);
  }

  @Test
  void testValidateUserExistsNotFound() {
    when(githubUserRepository.findById(userId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> feedbackService.validateUserExists(userId),
        "Expected NotFoundException when the user does not exist");

    assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), exception.getCode(),
        "Exception code should indicate USER_NOT_FOUND");
    verify(githubUserRepository, times(1)).findById(userId);
  }
}