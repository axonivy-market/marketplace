package com.axonivy.market.service;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.User;
import com.axonivy.market.exceptions.model.NotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

  private Feedback feedback;

  @BeforeEach
  void setUp() {
    feedback = new Feedback();
    feedback.setId("feedbackId");
    feedback.setUserId("userId");
    feedback.setProductId("productId");
    feedback.setRating(5);
    feedback.setContent("Great product!");

    Product product = new Product();
    product.setId("productId");

    User user = new User();
    user.setId("userId");

    lenient().when(userRepository.findById(anyString())).thenReturn(Optional.of(user));
    lenient().when(productRepository.findById(anyString())).thenReturn(Optional.of(product));
  }

  @Test
  void testFindFeedbacks_Success() throws NotFoundException {
    Page<Feedback> page = new PageImpl<>(Collections.singletonList(feedback));
    when(feedbackRepository.searchByProductId(anyString(), any(Pageable.class))).thenReturn(page);

    Page<Feedback> result = feedbackService.findFeedbacks("productId", Pageable.unpaged());

    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    verify(feedbackRepository, times(1)).searchByProductId(anyString(), any(Pageable.class));
  }

  @Test
  void testFindFeedbacks_ProductNotFound() {
    when(productRepository.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> feedbackService.findFeedbacks("invalidProductId", Pageable.unpaged()));
  }

  @Test
  void testFindFeedback_Success() throws NotFoundException {
    when(feedbackRepository.findById(anyString())).thenReturn(Optional.of(feedback));

    Feedback result = feedbackService.findFeedback("feedbackId");

    assertNotNull(result);
    assertEquals("feedbackId", result.getId());
    verify(feedbackRepository, times(1)).findById(anyString());
  }

  @Test
  void testFindFeedback_NotFound() {
    when(feedbackRepository.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> feedbackService.findFeedback("invalidFeedbackId"));
  }

  @Test
  void testFindFeedbackByUserIdAndProductId_UserNotFound() {
    when(userRepository.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> feedbackService.findFeedbackByUserIdAndProductId("invalidUserId", "productId"));
  }

  @Test
  void testFindFeedbackByUserIdAndProductId_ProductNotFound() {
    when(productRepository.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> feedbackService.findFeedbackByUserIdAndProductId("userId", "invalidProductId"));
  }

  @Test
  void testUpsertFeedback_UserNotFound() {
    when(userRepository.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> feedbackService.upsertFeedback(feedback));
  }

  @Test
  void testUpsertFeedback_ProductNotFound() {
    when(productRepository.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> feedbackService.upsertFeedback(feedback));
  }
}
