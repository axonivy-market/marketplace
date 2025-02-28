package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.assembler.FeedbackModelAssembler;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.User;
import com.axonivy.market.enums.FeedbackStatus;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.FeedbackApprovalModel;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.model.FeedbackModelRequest;
import com.axonivy.market.service.FeedbackService;
import com.axonivy.market.service.JwtService;
import com.axonivy.market.service.UserService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackControllerTest extends BaseSetup {

  private static final String PRODUCT_ID_SAMPLE = "product-id";
  private static final String FEEDBACK_ID_SAMPLE = "feedback-id";
  private static final String USER_ID_SAMPLE = "user-id";
  private static final String USER_NAME_SAMPLE = "Test User";
  private static final String TOKEN_SAMPLE = "token-sample";

  @Mock
  private FeedbackService service;

  @Mock
  private JwtService jwtService;

  @Mock
  private UserService userService;

  @Mock
  private GitHubService gitHubService;

  @Mock
  private FeedbackModelAssembler feedbackModelAssembler;

  @Mock
  private PagedResourcesAssembler<Feedback> pagedResourcesAssembler;

  @InjectMocks
  private FeedbackController feedbackController;

  @BeforeEach
  void setup() {
    feedbackModelAssembler = new FeedbackModelAssembler(userService);
    feedbackController = new FeedbackController(service, jwtService, gitHubService, feedbackModelAssembler,
        pagedResourcesAssembler);
  }

  @Test
  void testFindFeedbacksAsEmpty() {
    PageRequest pageable = PageRequest.of(0, 20);
    Page<Feedback> mockFeedbacks = new PageImpl<>(List.of(), pageable, 0);
    when(service.findFeedbacks(any(), any())).thenReturn(mockFeedbacks);
    when(pagedResourcesAssembler.toEmptyModel(any(), any())).thenReturn(PagedModel.empty());
    var result = feedbackController.findFeedbacks(PRODUCT_ID_SAMPLE, pageable);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertEquals(0, Objects.requireNonNull(result.getBody()).getContent().size());
  }

  @Test
  void testFindFeedbacks() {
    PageRequest pageable = PageRequest.of(0, 20);
    Feedback mockFeedback = createFeedbackMock();
    User mockUser = createUserMock();

    Page<Feedback> mockFeedbacks = new PageImpl<>(List.of(mockFeedback), pageable, 1);
    when(service.findFeedbacks(any(), any())).thenReturn(mockFeedbacks);
    when(userService.findUser(any())).thenReturn(mockUser);
    var mockFeedbackModel = feedbackModelAssembler.toModel(mockFeedback);
    var mockPagedModel = PagedModel.of(List.of(mockFeedbackModel), new PagedModel.PageMetadata(1, 0, 1));
    when(pagedResourcesAssembler.toModel(any(), any(FeedbackModelAssembler.class))).thenReturn(mockPagedModel);
    var result = feedbackController.findFeedbacks(PRODUCT_ID_SAMPLE, pageable);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertEquals(1, Objects.requireNonNull(result.getBody()).getContent().size());
    assertEquals(USER_NAME_SAMPLE, result.getBody().getContent().iterator().next().getUsername());
  }

  @Test
  void testFindFeedback() {
    Feedback mockFeedback = createFeedbackMock();
    User mockUser = createUserMock();
    when(service.findFeedback(FEEDBACK_ID_SAMPLE)).thenReturn(mockFeedback);
    when(userService.findUser(any())).thenReturn(mockUser);
    var result = feedbackController.findFeedback(FEEDBACK_ID_SAMPLE);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertEquals(USER_NAME_SAMPLE, Objects.requireNonNull(result.getBody()).getUsername());
  }

  @Test
  void testFindFeedbackByUserIdAndProductId() {
    Feedback mockFeedback = createFeedbackMock();
    User mockUser = createUserMock();
    when(service.findFeedbackByUserIdAndProductId(any(), any())).thenReturn(List.of(mockFeedback));
    when(userService.findUser(any())).thenReturn(mockUser);
    var result = feedbackController.findFeedbackByUserIdAndProductId(USER_ID_SAMPLE, PRODUCT_ID_SAMPLE);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertEquals(USER_NAME_SAMPLE, Objects.requireNonNull(result.getBody()).get(0).getUsername());
  }

  @Test
  void testFindAllFeedbacks() {
    PageRequest pageable = PageRequest.of(0, 20);
    Feedback mockFeedback = createFeedbackMock();
    String authHeader = "Bearer sample-token";
    User mockUser = createUserMock();

    Page<Feedback> mockFeedbacks = new PageImpl<>(List.of(mockFeedback), pageable, 1);
    when(service.findAllFeedbacks(any())).thenReturn(mockFeedbacks);
    when(userService.findUser(any())).thenReturn(mockUser);
    var mockFeedbackModel = feedbackModelAssembler.toModel(mockFeedback);
    var mockPagedModel = PagedModel.of(List.of(mockFeedbackModel), new PagedModel.PageMetadata(1, 0, 1));
    when(pagedResourcesAssembler.toModel(any(), any(FeedbackModelAssembler.class))).thenReturn(mockPagedModel);

    var result = feedbackController.findAllFeedbacks(authHeader, pageable);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertEquals(1, Objects.requireNonNull(result.getBody()).getContent().size());
    assertEquals(FEEDBACK_ID_SAMPLE, result.getBody().getContent().iterator().next().getId());
  }

  @Test
  void testFindAllFeedbacksEmpty() {
    PageRequest pageable = PageRequest.of(0, 20);
    Page<Feedback> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
    String authHeader = "Bearer sample-token";

    when(service.findAllFeedbacks(pageable)).thenReturn(emptyPage);
    when(pagedResourcesAssembler.toEmptyModel(any(), any())).thenReturn(PagedModel.empty());

    var result = feedbackController.findAllFeedbacks(authHeader, pageable);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertEquals(0, Objects.requireNonNull(result.getBody()).getContent().size());
  }

  @Test
  void testUpdateFeedbackWithNewStatus() {
    FeedbackApprovalModel feedbackApproval = new FeedbackApprovalModel();
    feedbackApproval.setFeedbackId(FEEDBACK_ID_SAMPLE);
    feedbackApproval.setIsApproved(true);

    Feedback updatedFeedback = createFeedbackMock();
    User mockUser = createUserMock();
    FeedbackModel mockFeedbackModel = new FeedbackModel();
    mockFeedbackModel.setId(FEEDBACK_ID_SAMPLE);
    mockFeedbackModel.setUsername(USER_NAME_SAMPLE);

    when(service.updateFeedbackWithNewStatus(feedbackApproval)).thenReturn(updatedFeedback);
    when(userService.findUser(any())).thenReturn(mockUser);

    var result = feedbackController.updateFeedbackWithNewStatus(feedbackApproval);

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertEquals(FEEDBACK_ID_SAMPLE, Objects.requireNonNull(result.getBody()).getId());
    assertEquals(FeedbackStatus.APPROVED, result.getBody().getFeedbackStatus());
  }

  @Test
  void testCreateFeedback() {
    FeedbackModelRequest mockFeedbackModel = createFeedbackModelRequestMock();
    Feedback mockFeedback = createFeedbackMock();
    Claims mockClaims = createMockClaims();
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    when(jwtService.validateToken(TOKEN_SAMPLE)).thenReturn(true);
    when(jwtService.getClaimsFromToken(TOKEN_SAMPLE)).thenReturn(mockClaims);
    when(service.upsertFeedback(any(), any())).thenReturn(mockFeedback);

    var result = feedbackController.createFeedback(mockFeedbackModel, "Bearer " + TOKEN_SAMPLE);
    assertEquals(HttpStatus.CREATED, result.getStatusCode());
    assertTrue(Objects.requireNonNull(result.getHeaders().getLocation()).toString().contains(mockFeedback.getId()));
  }

  private Feedback createFeedbackMock() {
    Feedback mockFeedback = new Feedback();
    mockFeedback.setId(FEEDBACK_ID_SAMPLE);
    mockFeedback.setUserId(USER_ID_SAMPLE);
    mockFeedback.setProductId(PRODUCT_ID_SAMPLE);
    mockFeedback.setContent("Great product!");
    mockFeedback.setRating(5);
    mockFeedback.setFeedbackStatus(FeedbackStatus.APPROVED);
    mockFeedback.setModeratorName("Admin");
    mockFeedback.setReviewDate(new Date());
    return mockFeedback;
  }

  private FeedbackModelRequest createFeedbackModelRequestMock() {
    FeedbackModelRequest mockFeedback = new FeedbackModelRequest();
    mockFeedback.setProductId(PRODUCT_ID_SAMPLE);
    mockFeedback.setContent("Great product!");
    mockFeedback.setRating(5);
    return mockFeedback;
  }

  private User createUserMock() {
    User mockUser = new User();
    mockUser.setId(USER_ID_SAMPLE);
    mockUser.setUsername("testUser");
    mockUser.setName("Test User");
    mockUser.setAvatarUrl("http://avatar.url");
    mockUser.setProvider("local");
    return mockUser;
  }

  private Claims createMockClaims() {
    Claims claims = new io.jsonwebtoken.impl.DefaultClaims();
    claims.setSubject(USER_ID_SAMPLE);
    return claims;
  }
}