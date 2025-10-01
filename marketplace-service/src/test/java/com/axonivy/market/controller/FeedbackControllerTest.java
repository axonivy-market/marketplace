package com.axonivy.market.controller;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.assembler.FeedbackModelAssembler;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.enums.FeedbackStatus;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.FeedbackApprovalModel;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.model.FeedbackModelRequest;
import com.axonivy.market.service.FeedbackService;
import com.axonivy.market.service.JwtService;
import com.axonivy.market.service.GithubUserService;
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

import java.time.LocalDateTime;
import java.util.Collections;
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
  private GithubUserService githubUserService;

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
    feedbackModelAssembler = new FeedbackModelAssembler(githubUserService);
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

    assertEquals(HttpStatus.OK, result.getStatusCode(),
        "Response status should be 200 OK when no feedbacks are found.");
    assertTrue(result.hasBody(),
        "Response should still contain a body even when no feedbacks are found.");
    assertEquals(0, Objects.requireNonNull(result.getBody()).getContent().size(),
        "Response body content size should be 0 when no feedbacks are found.");
  }

  @Test
  void testFindFeedbacks() {
    PageRequest pageable = PageRequest.of(0, 20);
    Feedback mockFeedback = createFeedbackMock();
    GithubUser mockGithubUser = createUserMock();

    Page<Feedback> mockFeedbacks = new PageImpl<>(List.of(mockFeedback), pageable, 1);
    when(service.findFeedbacks(any(), any())).thenReturn(mockFeedbacks);
    when(githubUserService.findUser(any())).thenReturn(mockGithubUser);
    var mockFeedbackModel = feedbackModelAssembler.toModel(mockFeedback);
    var mockPagedModel = PagedModel.of(List.of(mockFeedbackModel), new PagedModel.PageMetadata(1, 0, 1));
    when(pagedResourcesAssembler.toModel(any(), any(FeedbackModelAssembler.class))).thenReturn(mockPagedModel);
    var result = feedbackController.findFeedbacks(PRODUCT_ID_SAMPLE, pageable);

    assertEquals(HttpStatus.OK, result.getStatusCode(),
        "Response status should be 200 OK when feedbacks exist.");
    assertTrue(result.hasBody(),
        "Response should contain a body when feedbacks exist.");
    assertEquals(1, Objects.requireNonNull(result.getBody()).getContent().size(),
        "Response body content size should match the number of feedbacks returned.");
    assertEquals(USER_NAME_SAMPLE, result.getBody().getContent().iterator().next().getUsername(),
        "The username in the feedback model should match the expected GitHub username.");
  }

  @Test
  void testFindFeedback() {
    Feedback mockFeedback = createFeedbackMock();
    GithubUser mockGithubUser = createUserMock();
    when(service.findFeedback(FEEDBACK_ID_SAMPLE)).thenReturn(mockFeedback);
    when(githubUserService.findUser(any())).thenReturn(mockGithubUser);
    var result = feedbackController.findFeedback(FEEDBACK_ID_SAMPLE);

    assertEquals(HttpStatus.OK, result.getStatusCode(),
        "Response status should be 200 OK when feedback is found.");
    assertTrue(result.hasBody(),
        "Response should contain a body when feedback is found.");
    assertEquals(USER_NAME_SAMPLE, Objects.requireNonNull(result.getBody()).getUsername(),
        "The username in the feedback should match the expected GitHub username.");
  }

  @Test
  void testFindFeedbackByUserIdAndProductId() {
    Feedback mockFeedback = createFeedbackMock();
    GithubUser mockGithubUser = createUserMock();
    when(service.findFeedbackByUserIdAndProductId(any(), any())).thenReturn(List.of(mockFeedback));
    when(githubUserService.findUser(any())).thenReturn(mockGithubUser);
    var result = feedbackController.findFeedbackByUserIdAndProductId(USER_ID_SAMPLE, PRODUCT_ID_SAMPLE);

    assertEquals(HttpStatus.OK, result.getStatusCode(),
        "Response status should be 200 OK when feedbacks for a specific user and product are found.");
    assertTrue(result.hasBody(),
        "Response should contain a body when feedbacks for a specific user and product are found.");
    assertEquals(USER_NAME_SAMPLE, Objects.requireNonNull(result.getBody()).get(0).getUsername(),
        "The username of the feedback should match the expected GitHub username.");
  }

  @Test
  void testFindAllFeedbacks() {
    PageRequest pageable = PageRequest.of(0, 20);
    Feedback mockFeedback = createFeedbackMock();
    String authHeader = "Bearer sample-token";
    GithubUser mockGithubUser = createUserMock();

    Page<Feedback> mockFeedbacks = new PageImpl<>(List.of(mockFeedback), pageable, 1);
    when(service.findAllFeedbacks(any())).thenReturn(mockFeedbacks);
    when(githubUserService.findUser(any())).thenReturn(mockGithubUser);
    var mockFeedbackModel = feedbackModelAssembler.toModel(mockFeedback);
    var mockPagedModel = PagedModel.of(List.of(mockFeedbackModel), new PagedModel.PageMetadata(1, 0, 1));
    when(pagedResourcesAssembler.toModel(any(), any(FeedbackModelAssembler.class))).thenReturn(mockPagedModel);

    var result = feedbackController.findAllFeedbacks(authHeader, pageable);

    assertEquals(HttpStatus.OK, result.getStatusCode(),
        "Response status should be 200 OK when all feedbacks are retrieved.");
    assertTrue(result.hasBody(),
        "Response should contain a body when all feedbacks are retrieved.");
    assertEquals(1, Objects.requireNonNull(result.getBody()).getContent().size(),
        "The number of feedbacks in the response body should match the expected count.");
    assertEquals(FEEDBACK_ID_SAMPLE, result.getBody().getContent().iterator().next().getId(),
        "The feedback ID in the response should match the expected feedback ID.");
  }

  @Test
  void testFindAllFeedbacksEmpty() {
    PageRequest pageable = PageRequest.of(0, 20);
    Page<Feedback> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
    String authHeader = "Bearer sample-token";

    when(service.findAllFeedbacks(pageable)).thenReturn(emptyPage);
    when(pagedResourcesAssembler.toEmptyModel(any(), any())).thenReturn(PagedModel.empty());

    var result = feedbackController.findAllFeedbacks(authHeader, pageable);

    assertEquals(HttpStatus.OK, result.getStatusCode(),
        "Response status should be 200 OK when no feedbacks are found.");
    assertTrue(result.hasBody(),
        "Response should still contain a body even when no feedbacks are found.");
    assertEquals(0, Objects.requireNonNull(result.getBody()).getContent().size(),
        "Response body content size should be 0 when no feedbacks are found.");
  }

  @Test
  void testUpdateFeedbackWithNewStatus() {
    FeedbackApprovalModel feedbackApproval = new FeedbackApprovalModel();
    feedbackApproval.setFeedbackId(FEEDBACK_ID_SAMPLE);
    feedbackApproval.setIsApproved(true);

    Feedback updatedFeedback = createFeedbackMock();
    GithubUser mockGithubUser = createUserMock();
    FeedbackModel mockFeedbackModel = new FeedbackModel();
    mockFeedbackModel.setId(FEEDBACK_ID_SAMPLE);
    mockFeedbackModel.setUsername(USER_NAME_SAMPLE);

    when(service.updateFeedbackWithNewStatus(feedbackApproval)).thenReturn(updatedFeedback);
    when(githubUserService.findUser(any())).thenReturn(mockGithubUser);

    var result = feedbackController.updateFeedbackWithNewStatus(feedbackApproval);

    assertEquals(HttpStatus.OK, result.getStatusCode(),
        "Response status should be 200 OK when feedback status is successfully updated.");
    assertTrue(result.hasBody(),
        "Response should contain a body after updating feedback status.");
    assertEquals(FEEDBACK_ID_SAMPLE, Objects.requireNonNull(result.getBody()).getId(),
        "The feedback ID in the response should match the updated feedback ID.");
    assertEquals(FeedbackStatus.APPROVED, result.getBody().getFeedbackStatus(),
        "The feedback status should be APPROVED after updating.");
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

    assertEquals(HttpStatus.CREATED, result.getStatusCode(),
        "Response status should be 201 CREATED when a new feedback is successfully created.");
    assertTrue(Objects.requireNonNull(result.getHeaders().getLocation()).toString().contains(mockFeedback.getId()),
        "The Location header should contain the ID of the newly created feedback.");
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
    mockFeedback.setReviewDate(LocalDateTime.now());
    return mockFeedback;
  }

  private FeedbackModelRequest createFeedbackModelRequestMock() {
    FeedbackModelRequest mockFeedback = new FeedbackModelRequest();
    mockFeedback.setProductId(PRODUCT_ID_SAMPLE);
    mockFeedback.setContent("Great product!");
    mockFeedback.setRating(5);
    return mockFeedback;
  }

  private GithubUser createUserMock() {
    GithubUser mockGithubUser = new GithubUser();
    mockGithubUser.setId(USER_ID_SAMPLE);
    mockGithubUser.setUsername("testUser");
    mockGithubUser.setName("Test User");
    mockGithubUser.setAvatarUrl("http://avatar.url");
    mockGithubUser.setProvider("local");
    return mockGithubUser;
  }

  private Claims createMockClaims() {
    Claims claims = new io.jsonwebtoken.impl.DefaultClaims();
    claims.setSubject(USER_ID_SAMPLE);
    return claims;
  }
}