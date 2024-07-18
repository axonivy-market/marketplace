package com.axonivy.market.service;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.User;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.model.ProductRating;
import com.axonivy.market.repository.FeedbackRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.repository.UserRepository;
import com.axonivy.market.service.impl.FeedbackServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest {

  @Mock
  private FeedbackRepository feedbackRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ProductRepository productRepository;

  @InjectMocks
  private FeedbackServiceImpl feedbackService;

  @BeforeEach
  void setUp() {
    // Mock initialization or setup if needed
  }

  @Test
  void testFindFeedbacks_ProductNotFound() {
    String productId = "nonExistingProduct";

    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    Pageable unpaged = Pageable.unpaged();

    assertThrows(NotFoundException.class, () -> feedbackService.findFeedbacks(productId, unpaged));

    verify(productRepository, times(1)).findById(productId);
    verify(feedbackRepository, never()).searchByProductId(any(), any());
  }

  @Test
  void testFindFeedback_Success() throws NotFoundException {
    // Mock data
    String feedbackId = "feedback123";
    Feedback mockFeedback = new Feedback();
    mockFeedback.setId(feedbackId);

    // Mock behavior
    when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(mockFeedback));

    // Test method
    Feedback result = feedbackService.findFeedback(feedbackId);

    // Verify
    assertEquals(mockFeedback, result);
    verify(feedbackRepository, times(1)).findById(feedbackId);
  }

  @Test
  void testFindFeedback_NotFound() {
    // Mock data
    String nonExistingId = "nonExistingFeedbackId";

    // Mock behavior
    when(feedbackRepository.findById(nonExistingId)).thenReturn(Optional.empty());

    // Test and verify exception
    assertThrows(NotFoundException.class, () -> feedbackService.findFeedback(nonExistingId));

    // Verify interactions
    verify(feedbackRepository, times(1)).findById(nonExistingId);
  }

  @Test
  void testFindFeedbackByUserIdAndProductId_UserNotFound() {
    // Mock data
    String nonExistingUserId = "nonExistingUser";
    String productId = "product123";

    // Mock behavior
    when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

    // Test and verify exception
    assertThrows(NotFoundException.class,
        () -> feedbackService.findFeedbackByUserIdAndProductId(nonExistingUserId, productId));

    // Verify interactions
    verify(userRepository, times(1)).findById(nonExistingUserId);
    verify(feedbackRepository, never()).findByUserIdAndProductId(any(), any());
  }

  @Test
  void testUpsertFeedback_NewFeedback() throws NotFoundException {
    // Mock data
    FeedbackModel newFeedbackModel = new FeedbackModel();
    newFeedbackModel.setUserId("user123");
    newFeedbackModel.setProductId("product123");
    newFeedbackModel.setContent("Great product!");
    newFeedbackModel.setRating(5);

    Feedback newFeedback = new Feedback();
    newFeedback.setUserId(newFeedbackModel.getUserId());
    newFeedback.setProductId(newFeedbackModel.getProductId());
    newFeedback.setContent(newFeedbackModel.getContent());
    newFeedback.setRating(newFeedbackModel.getRating());

    User u = new User();
    u.setId(newFeedbackModel.getUserId());
    when(userRepository.findById(newFeedbackModel.getUserId())).thenReturn(Optional.of(u));
    when(feedbackRepository.findByUserIdAndProductId(newFeedbackModel.getUserId(),
        newFeedbackModel.getProductId())).thenReturn(null);
    when(feedbackRepository.save(any(Feedback.class))).thenReturn(newFeedback);

    // Test method
    Feedback result = feedbackService.upsertFeedback(newFeedbackModel);

    // Verify
    assertEquals(newFeedback, result);
  }

  @Test
  void testUpsertFeedback_UpdateFeedback() throws NotFoundException {
    // Mock data
    FeedbackModel existingFeedbackModel = new FeedbackModel();
    existingFeedbackModel.setId("existingFeedback123");
    existingFeedbackModel.setUserId("user123");
    existingFeedbackModel.setProductId("product123");
    existingFeedbackModel.setContent("Good product! Very well!");
    existingFeedbackModel.setRating(5);

    Feedback existingFeedback = new Feedback();
    existingFeedback.setId("existingFeedback123");
    existingFeedback.setUserId("user123");
    existingFeedback.setProductId("product123");
    existingFeedback.setContent("Bad product!");
    existingFeedback.setRating(1);

    User u = new User();
    u.setId(existingFeedback.getUserId());
    when(userRepository.findById(existingFeedback.getUserId())).thenReturn(Optional.of(u));
    when(feedbackRepository.findByUserIdAndProductId(existingFeedback.getUserId(),
        existingFeedback.getProductId())).thenReturn(existingFeedback);
    when(feedbackRepository.save(existingFeedback)).thenReturn(existingFeedback);

    // Test method
    Feedback updatedFeedback = new Feedback();
    updatedFeedback.setId(existingFeedback.getId());
    updatedFeedback.setUserId(existingFeedback.getUserId());
    updatedFeedback.setProductId(existingFeedback.getProductId());
    updatedFeedback.setContent("Good product! Very well!");
    updatedFeedback.setRating(5);

    Feedback result = feedbackService.upsertFeedback(existingFeedbackModel);

    // Verify
    assertEquals(updatedFeedback.getId(), result.getId());
    assertEquals(updatedFeedback.getContent(), result.getContent());
    assertEquals(updatedFeedback.getRating(), result.getRating());
    verify(userRepository, times(1)).findById(existingFeedback.getUserId());
    verify(feedbackRepository, times(1)).findByUserIdAndProductId(existingFeedback.getUserId(),
        existingFeedback.getProductId());
    verify(feedbackRepository, times(1)).save(existingFeedback);
  }

  @Test
  void testGetProductRatingById_NoFeedbacks() {
    // Mock data
    String productId = "product123";

    // Mock behavior
    when(feedbackRepository.findByProductId(productId)).thenReturn(new ArrayList<>());

    // Test method
    List<ProductRating> result = feedbackService.getProductRatingById(productId);

    // Verify
    assertEquals(5, result.size()); // Expect ratings for stars 1 to 5
    result.forEach(rating -> {
      assertEquals(0, rating.getCommentNumber());
      assertEquals(0, rating.getPercent());
    });
    verify(feedbackRepository, times(1)).findByProductId(productId);
  }
}

