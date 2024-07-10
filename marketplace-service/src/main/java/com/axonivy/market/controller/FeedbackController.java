package com.axonivy.market.controller;

import com.axonivy.market.assembler.FeedbackModelAssembler;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.model.ProductRating;
import com.axonivy.market.service.FeedbackService;
import com.axonivy.market.service.JwtService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.FEEDBACK;

@RestController
@RequestMapping(FEEDBACK)
public class FeedbackController {

  private final FeedbackService feedbackService;
  private final JwtService jwtService;
  private final FeedbackModelAssembler feedbackModelAssembler;

  private final PagedResourcesAssembler<Feedback> pagedResourcesAssembler;

  public FeedbackController(FeedbackService feedbackService, JwtService jwtService, FeedbackModelAssembler feedbackModelAssembler, PagedResourcesAssembler<Feedback> pagedResourcesAssembler) {
    this.feedbackService = feedbackService;
    this.jwtService = jwtService;
    this.feedbackModelAssembler = feedbackModelAssembler;
    this.pagedResourcesAssembler = pagedResourcesAssembler;
  }

  @Operation(summary = "Find all feedbacks by product id")
  @GetMapping("/product/{productId}")
  public ResponseEntity<PagedModel<FeedbackModel>> findFeedbacks(@PathVariable String productId, Pageable pageable) {
    Page<Feedback> results = feedbackService.findFeedbacks(productId, pageable);
    if (results.isEmpty()) {
      return generateEmptyPagedModel();
    }
    var responseContent = new PageImpl<>(results.getContent(), pageable, results.getTotalElements());
    var pageResources = pagedResourcesAssembler.toModel(responseContent, feedbackModelAssembler);
    return new ResponseEntity<>(pageResources, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<FeedbackModel> findFeedback(@PathVariable String id) {
    Feedback feedback = feedbackService.findFeedback(id);
    return ResponseEntity.ok(feedbackModelAssembler.toModel(feedback));
  }

  @Operation(summary = "Find all feedbacks by user id and product id")
  @GetMapping()
  public ResponseEntity<FeedbackModel> findFeedbackByUserIdAndProductId(
      @RequestParam String userId,
      @RequestParam String productId) {
    Feedback feedback = feedbackService.findFeedbackByUserIdAndProductId(userId, productId);
    return ResponseEntity.ok(feedbackModelAssembler.toModel(feedback));
  }

  @PostMapping
  public ResponseEntity<Void> createFeedback(@RequestBody @Valid Feedback feedback, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
    String token = null;
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      token = authorizationHeader.substring(7); // Remove "Bearer " prefix
    }

    // Validate the token
    if (token == null || !jwtService.validateToken(token)) {
      return ResponseEntity.status(401).build(); // Unauthorized if token is missing or invalid
    }

    Claims claims = jwtService.getClaimsFromToken(token);
    feedback.setUserId(claims.getSubject());
    Feedback newFeedback = feedbackService.upsertFeedback(feedback);

    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(newFeedback.getId())
        .toUri();

    return ResponseEntity.created(location).build();
  }

  @Operation(summary = "Find rating information of product by id")
  @GetMapping("/product/{productId}/rating")
  public ResponseEntity<List<ProductRating>> getProductRating(@PathVariable String productId) {
    return ResponseEntity.ok(feedbackService.getProductRatingById(productId));
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<PagedModel<FeedbackModel>> generateEmptyPagedModel() {
    var emptyPagedModel = (PagedModel<FeedbackModel>) pagedResourcesAssembler
        .toEmptyModel(Page.empty(), FeedbackModel.class);
    return new ResponseEntity<>(emptyPagedModel, HttpStatus.OK);
  }
}
