package com.axonivy.market.controller;

import static com.axonivy.market.constants.RequestMappingConstants.BY_ID;
import static com.axonivy.market.constants.RequestMappingConstants.FEEDBACK;
import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_BY_ID;
import static com.axonivy.market.constants.RequestMappingConstants.PRODUCT_RATING_BY_ID;
import static com.axonivy.market.constants.RequestParamConstants.AUTHORIZATION;
import static com.axonivy.market.constants.RequestParamConstants.ID;
import static com.axonivy.market.constants.RequestParamConstants.USER_ID;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.axonivy.market.assembler.FeedbackModelAssembler;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.model.ProductRating;
import com.axonivy.market.service.FeedbackService;
import com.axonivy.market.service.JwtService;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping(FEEDBACK)
public class FeedbackController {

  private final FeedbackService feedbackService;
  private final JwtService jwtService;
  private final FeedbackModelAssembler feedbackModelAssembler;

  private final PagedResourcesAssembler<Feedback> pagedResourcesAssembler;

  public FeedbackController(FeedbackService feedbackService, JwtService jwtService,
      FeedbackModelAssembler feedbackModelAssembler, PagedResourcesAssembler<Feedback> pagedResourcesAssembler) {
    this.feedbackService = feedbackService;
    this.jwtService = jwtService;
    this.feedbackModelAssembler = feedbackModelAssembler;
    this.pagedResourcesAssembler = pagedResourcesAssembler;
  }

  @Operation(summary = "Find all feedbacks by product id")
  @GetMapping(PRODUCT_BY_ID)
  public ResponseEntity<PagedModel<FeedbackModel>> findFeedbacks(@PathVariable(ID) String productId,
      Pageable pageable) {
    Page<Feedback> results = feedbackService.findFeedbacks(productId, pageable);
    if (results.isEmpty()) {
      return generateEmptyPagedModel();
    }
    var responseContent = new PageImpl<>(results.getContent(), pageable, results.getTotalElements());
    var pageResources = pagedResourcesAssembler.toModel(responseContent, feedbackModelAssembler);
    return new ResponseEntity<>(pageResources, HttpStatus.OK);
  }

  @GetMapping(BY_ID)
  public ResponseEntity<FeedbackModel> findFeedback(@PathVariable(ID) String id) {
    Feedback feedback = feedbackService.findFeedback(id);
    return ResponseEntity.ok(feedbackModelAssembler.toModel(feedback));
  }

  @Operation(summary = "Find all feedbacks by user id and product id")
  @GetMapping()
  public ResponseEntity<FeedbackModel> findFeedbackByUserIdAndProductId(@RequestParam(USER_ID) String userId,
      @RequestParam("productId") String productId) {
    Feedback feedback = feedbackService.findFeedbackByUserIdAndProductId(userId, productId);
    return ResponseEntity.ok(feedbackModelAssembler.toModel(feedback));
  }

  @PostMapping
  public ResponseEntity<Void> createFeedback(@RequestBody @Valid FeedbackModel feedback,
      @RequestHeader(value = AUTHORIZATION) String authorizationHeader) {
    String token = null;
    if (authorizationHeader != null && authorizationHeader.startsWith(CommonConstants.BEARER)) {
      token = authorizationHeader.substring(CommonConstants.BEARER.length()).trim(); // Remove "Bearer " prefix
    }

    // Validate the token
    if (token == null || !jwtService.validateToken(token)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Unauthorized if token is missing or invalid
    }

    Claims claims = jwtService.getClaimsFromToken(token);
    feedback.setUserId(claims.getSubject());
    Feedback newFeedback = feedbackService.upsertFeedback(feedback);

    URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(BY_ID).buildAndExpand(newFeedback.getId())
        .toUri();

    return ResponseEntity.created(location).build();
  }

  @Operation(summary = "Find rating information of product by id")
  @GetMapping(PRODUCT_RATING_BY_ID)
  public ResponseEntity<List<ProductRating>> getProductRating(@PathVariable(ID) String productId) {
    return ResponseEntity.ok(feedbackService.getProductRatingById(productId));
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<PagedModel<FeedbackModel>> generateEmptyPagedModel() {
    var emptyPagedModel = (PagedModel<FeedbackModel>) pagedResourcesAssembler.toEmptyModel(Page.empty(),
        FeedbackModel.class);
    return new ResponseEntity<>(emptyPagedModel, HttpStatus.OK);
  }
}
