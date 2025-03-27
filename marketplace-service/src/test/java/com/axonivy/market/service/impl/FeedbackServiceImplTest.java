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

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(1, result.getContent().size());
    assertEquals("user1", result.getContent().get(0).getUserId());
    assertEquals("product1", result.getContent().get(0).getProductId());
    assertEquals("Great product!", result.getContent().get(0).getContent());
    assertEquals(5, result.getContent().get(0).getRating());
    assertEquals(FeedbackStatus.APPROVED, result.getContent().get(0).getFeedbackStatus());
    assertEquals("moderator", result.getContent().get(0).getModeratorName());
    assertEquals(1, result.getContent().get(0).getVersion());
    assertEquals("Product Name", result.getContent().get(0).getProductNames().get("en"));

    verify(feedbackRepository, times(1)).findFeedbackWithProductNames(pageable);
  }

  @Test
  void testFindAllFeedbacks_EmptyResult() {
    Pageable pageable = PageRequest.of(0, 20);
    Page<FeedbackProjection> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
    when(feedbackRepository.findFeedbackWithProductNames(pageable)).thenReturn(emptyPage);

    Page<Feedback> result = feedbackService.findAllFeedbacks(pageable);

    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
    assertTrue(result.getContent().isEmpty());

    verify(feedbackRepository, times(1)).findFeedbackWithProductNames(pageable);
  }

  @Test
  void testFindFeedbacks() throws NotFoundException {
    String productId = "product1";
    Pageable pageable = PageRequest.of(0, 10);
    Page<Feedback> page = new PageImpl<>(Collections.singletonList(feedback));

    when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
    when(feedbackRepository.findLatestApprovedFeedbacks(productId,
        List.of(FeedbackStatus.REJECTED, FeedbackStatus.PENDING), pageable)).thenReturn(page);

    Page<Feedback> result = feedbackService.findFeedbacks(productId, pageable);
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(productRepository, times(1)).findById(productId);
    verify(feedbackRepository, times(1)).findLatestApprovedFeedbacks(productId,
        List.of(FeedbackStatus.REJECTED, FeedbackStatus.PENDING), pageable);
  }

  @Test
  void testFindFeedbacks_ProductNotFound() {
    String productId = "product1";
    Pageable pageable = PageRequest.of(0, 10);

    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> feedbackService.findFeedbacks(productId, pageable));
    assertEquals(ErrorCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode());
    verify(productRepository, times(1)).findById(productId);
    verify(feedbackRepository, times(0)).findLatestApprovedFeedbacks(productId,
        List.of(FeedbackStatus.REJECTED, FeedbackStatus.PENDING), pageable);
  }

  @Test
  void testFindFeedback() throws NotFoundException {
    String feedbackId = "1";

    when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedback));

    Feedback result = feedbackService.findFeedback(feedbackId);
    assertNotNull(result);
    assertEquals(feedbackId, result.getId());
    verify(feedbackRepository, times(1)).findById(feedbackId);
  }

  @Test
  void testFindFeedback_NotFound() {
    String feedbackId = "1";

    when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class, () -> feedbackService.findFeedback(feedbackId));
    assertEquals(ErrorCode.FEEDBACK_NOT_FOUND.getCode(), exception.getCode());
    verify(feedbackRepository, times(1)).findById(feedbackId);
  }

  @Test
  void testFindFeedbackByUserIdAndProductId() {
    String productId = "product1";

    when(githubUserRepository.findById(userId)).thenReturn(Optional.of(new GithubUser()));
    when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
    when(feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED))).thenReturn(List.of(feedback));

    var result = feedbackService.findFeedbackByUserIdAndProductId(userId, productId);
    assertNotNull(result);
    assertEquals(userId, result.get(0).getUserId());
    assertEquals(productId, result.get(0).getProductId());
    verify(githubUserRepository, times(1)).findById(userId);
    verify(productRepository, times(1)).findById(productId);
    verify(feedbackRepository, times(1)).findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED));
  }

  @Test
  void testFindFeedbackByUserIdAndProductId_NoContent() {
    String productId = "product1";
    userId = "";
    when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
    when(feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED))).thenReturn(null);

    NoContentException exception = assertThrows(NoContentException.class,
        () -> feedbackService.findFeedbackByUserIdAndProductId(userId, productId));
    assertEquals(ErrorCode.NO_FEEDBACK_OF_USER_FOR_PRODUCT.getCode(), exception.getCode());
    verify(productRepository, times(1)).findById(productId);
    verify(feedbackRepository, times(1)).findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED));
  }

  @Test
  void testFindFeedbackByUserIdAndProductId_NotFound() {
    userId = "notFoundUser";

    when(githubUserRepository.findById(userId)).thenReturn(Optional.empty());
    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> feedbackService.findFeedbackByUserIdAndProductId(userId, "product"));
    assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), exception.getCode());
    verify(githubUserRepository, times(1)).findById(userId);
  }

  @Test
  void testUpdateFeedbackWithNewStatus_Approved() {
    String feedbackId = "1";
    int version = 3;
    FeedbackApprovalModel approvalModel = mockFeedbackApproval();
    approvalModel.setIsApproved(true);
    approvalModel.setVersion(3);

    when(feedbackRepository.findByIdAndVersion(feedbackId, version)).thenReturn(Optional.of(feedback));
    when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

    Feedback result = feedbackService.updateFeedbackWithNewStatus(approvalModel);

    assertNotNull(result);
    assertEquals(feedbackId, result.getId());
    assertEquals(FeedbackStatus.APPROVED, result.getFeedbackStatus());
    assertEquals(approvalModel.getModeratorName(), result.getModeratorName());
    assertNotNull(result.getReviewDate());

    verify(feedbackRepository, times(1)).findByIdAndVersion(feedbackId, version);
    verify(feedbackRepository, times(1)).save(any(Feedback.class));
  }

  @Test
  void testUpdateFeedbackWithNewStatus_Rejected() {
    String feedbackId = "1";
    int version = 3;
    FeedbackApprovalModel approvalModel = mockFeedbackApproval();
    approvalModel.setIsApproved(false);
    approvalModel.setVersion(3);

    when(feedbackRepository.findByIdAndVersion(feedbackId, version)).thenReturn(Optional.of(feedback));
    when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

    Feedback result = feedbackService.updateFeedbackWithNewStatus(approvalModel);

    assertNotNull(result);
    assertEquals(feedbackId, result.getId());
    assertEquals(FeedbackStatus.REJECTED, result.getFeedbackStatus());
    assertEquals(approvalModel.getModeratorName(), result.getModeratorName());
    assertNotNull(result.getReviewDate());

    verify(feedbackRepository, times(1)).findByIdAndVersion(feedbackId, version);
    verify(feedbackRepository, times(1)).save(any(Feedback.class));
  }

  @Test
  void testUpsertFeedback_Insert() throws NotFoundException {
    String productId = "product1";

    when(githubUserRepository.findById(userId)).thenReturn(Optional.of(new GithubUser()));
    when(feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED))).thenReturn(Collections.emptyList());
    when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

    Feedback result = feedbackService.upsertFeedback(feedbackModelRequest, userId);

    assertNotNull(result);
    assertEquals(feedbackModel.getUserId(), result.getUserId());
    assertEquals(feedbackModel.getProductId(), result.getProductId());
    assertEquals(feedbackModel.getRating(), result.getRating());
    assertEquals(feedbackModel.getContent(), result.getContent());
    verify(githubUserRepository, times(1)).findById(userId);
    verify(feedbackRepository, times(1)).findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED));
    verify(feedbackRepository, times(1)).save(any(Feedback.class));
  }

  @Test
  void testUpsertFeedback_Update() throws NotFoundException {
    String productId = "product1";

    when(githubUserRepository.findById(userId)).thenReturn(Optional.of(new GithubUser()));
    when(feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED))).thenReturn(List.of(feedback));
    when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

    Feedback result = feedbackService.upsertFeedback(feedbackModelRequest, userId);
    assertNotNull(result);
    assertEquals(feedbackModel.getUserId(), result.getUserId());
    assertEquals(feedbackModel.getProductId(), result.getProductId());
    assertEquals(feedbackModel.getRating(), result.getRating());
    assertEquals(feedbackModel.getContent(), result.getContent());
    assertEquals(FeedbackStatus.PENDING, result.getFeedbackStatus());
    verify(githubUserRepository, times(1)).findById(userId);
    verify(feedbackRepository, times(1)).findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED));
    verify(feedbackRepository, times(1)).save(any(Feedback.class));
  }

  @Test
  void testUpsertFeedback_MatchingApprovedFeedback() throws NotFoundException {
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
    when(feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED))).thenReturn(List.of(existingApproved, existingPending));

    Feedback result = feedbackService.upsertFeedback(feedbackModelRequest, userId);

    assertNotNull(result);
    assertEquals(existingApproved, result);
    verify(githubUserRepository, times(1)).findById(userId);
    verify(feedbackRepository, times(1)).findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED));
    verify(feedbackRepository, times(1)).delete(existingPending);
    verify(feedbackRepository, never()).save(any(Feedback.class));
  }

  @Test
  void testUpsertFeedback_NoMatchingFeedback() throws NotFoundException {
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

    assertNotNull(result);
    assertEquals(feedbackModelRequest.getRating(), result.getRating());
    assertEquals(feedbackModelRequest.getContent(), result.getContent());
    assertEquals(FeedbackStatus.PENDING, result.getFeedbackStatus());

    verify(githubUserRepository, times(1)).findById(userId);
    verify(feedbackRepository, times(1)).findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED));
    verify(feedbackRepository, times(1)).save(any(Feedback.class));
  }

  @Test
  void testGetProductRatingById() {
    String productId = "product1";
    List<Feedback> feedbacks = Collections.singletonList(feedback);
    List<FeedbackStatus> feedbackStatuses = Arrays.asList(FeedbackStatus.PENDING, FeedbackStatus.REJECTED);
    when(feedbackRepository.findByProductIdAndFeedbackStatusNotIn(productId, feedbackStatuses)).thenReturn(feedbacks);

    List<ProductRating> ratings = feedbackService.getProductRatingById(productId);
    assertNotNull(ratings);
    assertEquals(5, ratings.size());
    assertEquals(1, ratings.get(4).getCommentNumber());
    assertEquals(100, ratings.get(4).getPercent());
    verify(feedbackRepository, times(1)).findByProductIdAndFeedbackStatusNotIn(productId, feedbackStatuses);
  }

  @Test
  void testGetProductRatingById_NoFeedbacks() {
    String productId = "product1";

    when(feedbackRepository.findByProductIdAndFeedbackStatusNotIn(eq(productId), anyList())).thenReturn(
        Collections.emptyList());

    List<ProductRating> ratings = feedbackService.getProductRatingById(productId);
    assertNotNull(ratings);
    assertEquals(5, ratings.size());
    for (ProductRating rating : ratings) {
      assertEquals(0, rating.getCommentNumber());
      assertEquals(0, rating.getPercent());
    }
    verify(feedbackRepository, times(1)).findByProductIdAndFeedbackStatusNotIn(eq(productId), anyList());
  }

  @Test
  void testValidateProductExists() {
    String productId = "product1";

    when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));

    assertDoesNotThrow(() -> feedbackService.validateProductExists(productId));
    verify(productRepository, times(1)).findById(productId);
  }

  @Test
  void testValidateProductExists_NotFound() {
    String productId = "product1";

    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> feedbackService.validateProductExists(productId));
    assertEquals(ErrorCode.PRODUCT_NOT_FOUND.getCode(), exception.getCode());
    verify(productRepository, times(1)).findById(productId);
  }

  @Test
  void testValidateUserExists() {
    when(githubUserRepository.findById(userId)).thenReturn(Optional.of(new GithubUser()));

    assertDoesNotThrow(() -> feedbackService.validateUserExists(userId));
    verify(githubUserRepository, times(1)).findById(userId);
  }

  @Test
  void testValidateUserExists_NotFound() {
    when(githubUserRepository.findById(userId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> feedbackService.validateUserExists(userId));
    assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), exception.getCode());
    verify(githubUserRepository, times(1)).findById(userId);
  }
}