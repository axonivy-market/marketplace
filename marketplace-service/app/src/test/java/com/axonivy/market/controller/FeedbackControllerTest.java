package com.axonivy.market.controller;

import com.axonivy.market.aop.aspect.AuthorizedAspect;
import com.axonivy.market.assembler.FeedbackModelAssembler;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.enums.FeedbackStatus;
import com.axonivy.market.model.FeedbackApprovalModel;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.model.FeedbackModelRequest;
import com.axonivy.market.service.FeedbackService;
import com.axonivy.market.service.GithubUserService;
import com.axonivy.market.service.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(FeedbackController.class)
class FeedbackControllerTest extends WebMvcControllerTestSupport {

  private static final String PRODUCT_ID_SAMPLE = "product-id";
  private static final String FEEDBACK_ID_SAMPLE = "feedback-id";
  private static final String USER_ID_SAMPLE = "user-id";
  private static final String USER_NAME_SAMPLE = "User Name";

  private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

  @MockitoBean
  private FeedbackService service;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private GithubUserService githubUserService;

  @MockitoBean
  private FeedbackModelAssembler feedbackModelAssembler;

  @MockitoBean
  private PagedResourcesAssembler<Feedback> pagedResourcesAssembler;

  @Test
  void testFindFeedbacks() throws Exception {
    PageRequest pageable = PageRequest.of(0, 20);
    Feedback mockFeedback = createFeedbackMock();
    FeedbackModel mockFeedbackModel = createFeedbackModelMock();
    Page<Feedback> mockFeedbacks = new PageImpl<>(List.of(mockFeedback), pageable, 1);
    when(service.findFeedbacks(PRODUCT_ID_SAMPLE, pageable)).thenReturn(mockFeedbacks);
    when(feedbackModelAssembler.toModel(mockFeedback)).thenReturn(mockFeedbackModel);
    when(pagedResourcesAssembler.toModel(any(), any(FeedbackModelAssembler.class)))
        .thenReturn(PagedModel.of(List.of(mockFeedbackModel), new PagedModel.PageMetadata(1, 0, 1)));

    mockMvc.perform(get("/api/feedback/product/{id}", PRODUCT_ID_SAMPLE)
            .param("page", "0")
            .param("size", "20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$..id").value(hasItem(FEEDBACK_ID_SAMPLE)))
        .andExpect(jsonPath("$..username").value(hasItem(USER_NAME_SAMPLE)));
  }

  @Test
  void testFindFeedback() throws Exception {
    Feedback mockFeedback = createFeedbackMock();
    FeedbackModel mockFeedbackModel = createFeedbackModelMock();
    when(service.findFeedback(FEEDBACK_ID_SAMPLE)).thenReturn(mockFeedback);
    when(feedbackModelAssembler.toModel(mockFeedback)).thenReturn(mockFeedbackModel);

    mockMvc.perform(get("/api/feedback/{id}", FEEDBACK_ID_SAMPLE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(FEEDBACK_ID_SAMPLE))
        .andExpect(jsonPath("$.username").value(USER_NAME_SAMPLE));
  }

  @Test
  void testFindFeedbackByUserIdAndProductId() throws Exception {
    Feedback mockFeedback = createFeedbackMock();
    FeedbackModel mockFeedbackModel = createFeedbackModelMock();
    when(service.findFeedbackByUserIdAndProductId(USER_ID_SAMPLE, PRODUCT_ID_SAMPLE)).thenReturn(List.of(mockFeedback));
    when(feedbackModelAssembler.toModel(List.of(mockFeedback))).thenReturn(List.of(mockFeedbackModel));

    mockMvc.perform(get("/api/feedback")
            .param("userId", USER_ID_SAMPLE)
            .param("productId", PRODUCT_ID_SAMPLE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].username").value(USER_NAME_SAMPLE));
  }

  @Test
  void testUpdateFeedbackWithNewStatus() throws Exception {
    FeedbackApprovalModel feedbackApproval = new FeedbackApprovalModel();
    feedbackApproval.setFeedbackId(FEEDBACK_ID_SAMPLE);
    feedbackApproval.setIsApproved(true);

    Feedback updatedFeedback = createFeedbackMock();
    FeedbackModel mockFeedbackModel = createFeedbackModelMock();
    when(service.updateFeedbackWithNewStatus(any(), eq(MODERATOR_NAME))).thenReturn(updatedFeedback);
    when(feedbackModelAssembler.toModel(updatedFeedback)).thenReturn(mockFeedbackModel);

    mockMvc.perform(put("/api/feedback/approval")
            .with(requestedByHeader())
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .requestAttr(AuthorizedAspect.USERNAME_ATTRIBUTE, MODERATOR_NAME)
            .content(objectMapper.writeValueAsString(feedbackApproval)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(FEEDBACK_ID_SAMPLE))
        .andExpect(jsonPath("$.moderatorName").value(MODERATOR_NAME));
  }

  @Test
  void testCreateFeedback() throws Exception {
    FeedbackModelRequest mockFeedbackModel = createFeedbackModelRequestMock();
    Feedback mockFeedback = createFeedbackMock();
    Claims mockClaims = createMockClaims();
    when(jwtService.getClaimsFromToken(any())).thenReturn(mockClaims);
    when(service.upsertFeedback(any(), any())).thenReturn(mockFeedback);

    mockMvc.perform(post("/api/feedback")
            .with(requestedByHeader())
            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
            .header(com.axonivy.market.constants.RequestParamConstants.X_AUTHORIZATION, AUTHORIZATION_HEADER)
            .content(objectMapper.writeValueAsString(mockFeedbackModel)))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", org.hamcrest.Matchers.containsString(FEEDBACK_ID_SAMPLE)));
  }

  private Feedback createFeedbackMock() {
    Feedback mockFeedback = new Feedback();
    mockFeedback.setId(FEEDBACK_ID_SAMPLE);
    mockFeedback.setUserId(USER_ID_SAMPLE);
    mockFeedback.setProductId(PRODUCT_ID_SAMPLE);
    mockFeedback.setContent("Great product!");
    mockFeedback.setRating(5);
    mockFeedback.setFeedbackStatus(FeedbackStatus.APPROVED);
    mockFeedback.setReviewDate(LocalDateTime.now());
    mockFeedback.setModeratorName(MODERATOR_NAME);
    return mockFeedback;
  }

  private FeedbackModel createFeedbackModelMock() {
    FeedbackModel model = new FeedbackModel();
    model.setId(FEEDBACK_ID_SAMPLE);
    model.setUsername(USER_NAME_SAMPLE);
    model.setModeratorName(MODERATOR_NAME);
    return model;
  }

  private FeedbackModelRequest createFeedbackModelRequestMock() {
    FeedbackModelRequest mockFeedback = new FeedbackModelRequest();
    mockFeedback.setProductId(PRODUCT_ID_SAMPLE);
    mockFeedback.setContent("Great product!");
    mockFeedback.setRating(5);
    return mockFeedback;
  }

  private Claims createMockClaims() {
    Claims claims = org.mockito.Mockito.mock(Claims.class);
    when(claims.getSubject()).thenReturn(USER_ID_SAMPLE);
    return claims;
  }
}
