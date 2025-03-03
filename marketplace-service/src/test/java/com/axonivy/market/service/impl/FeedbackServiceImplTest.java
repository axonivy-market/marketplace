package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.User;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.enums.FeedbackStatus;
import com.axonivy.market.exceptions.model.NoContentException;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.FeedbackApprovalModel;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.model.FeedbackModelRequest;
import com.axonivy.market.model.ProductRating;
import com.axonivy.market.repository.FeedbackRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.repository.UserRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest extends BaseSetup {

  @Mock
  private FeedbackRepository feedbackRepository;

  @Mock
  private UserRepository userRepository;

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

    Page<Feedback> expectedPage = new PageImpl<>(List.of(feedback), pageable, 1);
    when(feedbackRepository.findAll(pageable)).thenReturn(expectedPage);

    Page<Feedback> result = feedbackService.findAllFeedbacks(pageable);

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(1, result.getContent().size());
    assertEquals(feedback, result.getContent().get(0));

    verify(feedbackRepository, times(1)).findAll(pageable);
  }

  @Test
  void testFindAllFeedbacks_EmptyResult() {
    Pageable pageable = PageRequest.of(0, 20);
    Page<Feedback> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
    when(feedbackRepository.findAll(pageable)).thenReturn(emptyPage);

    Page<Feedback> result = feedbackService.findAllFeedbacks(pageable);

    assertNotNull(result);
    assertEquals(0, result.getTotalElements());
    assertTrue(result.getContent().isEmpty());

    verify(feedbackRepository, times(1)).findAll(pageable);
  }

  @Test
  void testFindFeedbacks() throws NotFoundException {
    String productId = "product1";
    Pageable pageable = PageRequest.of(0, 10);
    Page<Feedback> page = new PageImpl<>(Collections.singletonList(feedback));

    when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
    when(feedbackRepository.findByProductIdAndFeedbackStatusNotIn(productId,
        List.of(FeedbackStatus.REJECTED, FeedbackStatus.PENDING), pageable)).thenReturn(page);

    Page<Feedback> result = feedbackService.findFeedbacks(productId, pageable);
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(productRepository, times(1)).findById(productId);
    verify(feedbackRepository, times(1)).findByProductIdAndFeedbackStatusNotIn(productId,
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
    verify(feedbackRepository, times(0)).findByProductIdAndFeedbackStatusNotIn(productId,
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

    when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
    when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
    when(feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED))).thenReturn(List.of(feedback));

    var result = feedbackService.findFeedbackByUserIdAndProductId(userId, productId);
    assertNotNull(result);
    assertEquals(userId, result.get(0).getUserId());
    assertEquals(productId, result.get(0).getProductId());
    verify(userRepository, times(1)).findById(userId);
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

    when(userRepository.findById(userId)).thenReturn(Optional.empty());
    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> feedbackService.findFeedbackByUserIdAndProductId(userId, "product"));
    assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), exception.getCode());
    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  void testUpdateFeedbackWithNewStatus_ApprovedWithExisting() {
    String feedbackId = "1";
    FeedbackApprovalModel approvalModel = mockFeedbackApproval();
    approvalModel.setIsApproved(true);

    Feedback existingApproved = new Feedback();
    existingApproved.setId("2");
    existingApproved.setFeedbackStatus(FeedbackStatus.APPROVED);
    existingApproved.setProductId("product1");
    existingApproved.setUserId("user1");

    when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedback));
    when(feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn("product1", "user1",
        List.of(FeedbackStatus.REJECTED))).thenReturn(List.of(existingApproved));
    when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

    Feedback result = feedbackService.updateFeedbackWithNewStatus(approvalModel);

    assertNotNull(result);
    assertEquals(feedbackId, result.getId());
    assertEquals(FeedbackStatus.APPROVED, result.getFeedbackStatus());
    assertEquals(approvalModel.getModeratorName(), result.getModeratorName());
    assertNotNull(result.getReviewDate());

    verify(feedbackRepository, times(1)).findById(feedbackId);
    verify(feedbackRepository, times(1)).findByProductIdAndUserIdAndFeedbackStatusNotIn("product1", "user1",
        List.of(FeedbackStatus.REJECTED));
    verify(feedbackRepository, times(1)).delete(existingApproved);
    verify(feedbackRepository, times(1)).save(any(Feedback.class));
  }

  @Test
  void testUpdateFeedbackWithNewStatus_Rejected() {
    String feedbackId = "1";
    FeedbackApprovalModel approvalModel = mockFeedbackApproval();
    approvalModel.setIsApproved(false);

    Feedback existingPending = new Feedback();
    existingPending.setId("2");
    existingPending.setFeedbackStatus(FeedbackStatus.PENDING);
    existingPending.setProductId("product1");
    existingPending.setUserId("user1");

    when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(feedback));
    when(feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn("product1", "user1",
        List.of(FeedbackStatus.REJECTED))).thenReturn(List.of(existingPending));
    when(feedbackRepository.save(any(Feedback.class))).thenReturn(feedback);

    Feedback result = feedbackService.updateFeedbackWithNewStatus(approvalModel);

    assertNotNull(result);
    assertEquals(feedbackId, result.getId());
    assertEquals(FeedbackStatus.REJECTED, result.getFeedbackStatus());
    assertEquals(approvalModel.getModeratorName(), result.getModeratorName());
    assertNotNull(result.getReviewDate());

    verify(feedbackRepository, times(1)).findById(feedbackId);
    verify(feedbackRepository, times(1)).findByProductIdAndUserIdAndFeedbackStatusNotIn("product1", "user1",
        List.of(FeedbackStatus.REJECTED));
    verify(feedbackRepository, times(1)).delete(existingPending);
    verify(feedbackRepository, times(1)).save(any(Feedback.class));
  }

  @Test
  void testUpsertFeedback_Insert() throws NotFoundException {
    String productId = "product1";

    when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
    when(feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED))).thenReturn(Collections.emptyList());
    when(feedbackRepository.insert(any(Feedback.class))).thenReturn(feedback);

    Feedback result = feedbackService.upsertFeedback(feedbackModelRequest, userId);

    assertNotNull(result);
    assertEquals(feedbackModel.getUserId(), result.getUserId());
    assertEquals(feedbackModel.getProductId(), result.getProductId());
    assertEquals(feedbackModel.getRating(), result.getRating());
    assertEquals(feedbackModel.getContent(), result.getContent());
    verify(userRepository, times(1)).findById(userId);
    verify(feedbackRepository, times(1)).findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED));
    verify(feedbackRepository, times(1)).insert(any(Feedback.class));
  }

  @Test
  void testUpsertFeedback_Update() throws NotFoundException {
    String productId = "product1";

    when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
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
    verify(userRepository, times(1)).findById(userId);
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

    when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
    when(feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED))).thenReturn(List.of(existingApproved, existingPending));

    Feedback result = feedbackService.upsertFeedback(feedbackModelRequest, userId);

    assertNotNull(result);
    assertEquals(existingApproved, result);
    verify(userRepository, times(1)).findById(userId);
    verify(feedbackRepository, times(1)).findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED));
    verify(feedbackRepository, times(1)).delete(existingPending);
    verify(feedbackRepository, never()).save(any(Feedback.class));
    verify(feedbackRepository, never()).insert(any(Feedback.class));
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

    when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));
    when(feedbackRepository.findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED))).thenReturn(List.of(existingApproved));
    when(feedbackRepository.insert(any(Feedback.class))).thenReturn(feedback);

    Feedback result = feedbackService.upsertFeedback(feedbackModelRequest, userId);

    assertNotNull(result);
    assertEquals(feedbackModelRequest.getRating(), result.getRating());
    assertEquals(feedbackModelRequest.getContent(), result.getContent());
    assertEquals(FeedbackStatus.PENDING, result.getFeedbackStatus());

    verify(userRepository, times(1)).findById(userId);
    verify(feedbackRepository, times(1)).findByProductIdAndUserIdAndFeedbackStatusNotIn(productId, userId,
        List.of(FeedbackStatus.REJECTED));
    verify(feedbackRepository, times(1)).insert(any(Feedback.class));
    verify(feedbackRepository, never()).save(any(Feedback.class));
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
    when(userRepository.findById(userId)).thenReturn(Optional.of(new User()));

    assertDoesNotThrow(() -> feedbackService.validateUserExists(userId));
    verify(userRepository, times(1)).findById(userId);
  }

  @Test
  void testValidateUserExists_NotFound() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    NotFoundException exception = assertThrows(NotFoundException.class,
        () -> feedbackService.validateUserExists(userId));
    assertEquals(ErrorCode.USER_NOT_FOUND.getCode(), exception.getCode());
    verify(userRepository, times(1)).findById(userId);
  }
}