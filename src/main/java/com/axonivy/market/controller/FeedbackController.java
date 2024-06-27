package com.axonivy.market.controller;

import com.axonivy.market.assembler.FeedbackModelAssembler;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.service.FeedbackService;
import com.axonivy.market.service.JwtService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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

import static com.axonivy.market.constants.RequestMappingConstants.FEEDBACK;

@RestController
@RequestMapping(FEEDBACK)
public class FeedbackController {

  private final FeedbackService service;
  private final JwtService jwtService;
  private final FeedbackModelAssembler assembler;

  private final PagedResourcesAssembler<Feedback> pagedResourcesAssembler;

  public FeedbackController(FeedbackService feedbackService, JwtService jwtService, FeedbackModelAssembler assembler, PagedResourcesAssembler<Feedback> pagedResourcesAssembler) {
    this.service = feedbackService;
    this.jwtService = jwtService;
    this.assembler = assembler;
    this.pagedResourcesAssembler = pagedResourcesAssembler;
  }

  @Operation(summary = "Find all feedbacks by product id")
  @GetMapping("/product/{productId}")
  public ResponseEntity<PagedModel<FeedbackModel>> findFeedbacks(@PathVariable String productId, Pageable pageable) {
    Page<Feedback> results = service.findFeedbacks(productId, pageable);
    if (results.isEmpty()) {
      return generateEmptyPagedModel();
    }
    var responseContent = new PageImpl<>(results.getContent(), pageable, results.getTotalElements());
    var pageResources = pagedResourcesAssembler.toModel(responseContent, assembler);
    return new ResponseEntity<>(pageResources, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<FeedbackModel> findFeedback(@PathVariable String id) {
    Feedback feedback = service.findFeedback(id);
    return ResponseEntity.ok(assembler.toModel(feedback));
  }

  @Operation(summary = "Find all feedbacks by user id and product id")
  @GetMapping()
  public ResponseEntity<FeedbackModel> findFeedbackByUserIdAndProductId(
      @RequestParam() String userId,
      @RequestParam String productId) {
    Feedback feedback = service.findFeedbackByUserIdAndProductId(userId, productId);
    return ResponseEntity.ok(assembler.toModel(feedback));
  }

  @PostMapping
  public ResponseEntity<Void> createFeedback(@RequestBody @Valid Feedback feedback, HttpServletRequest request, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {

    // Get the JWT token from the cookie
    String token = getTokenFromCookie(request);

    // If token is not found in cookie, try to get it from Authorization header
    if (token == null && authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      token = authorizationHeader.substring(7); // Remove "Bearer " prefix
    }

    // Validate the token
    if (token == null || !jwtService.validateToken(token)) {
      return ResponseEntity.status(401).build(); // Unauthorized if token is missing or invalid
    }

    // Optionally, get claims from the token
    Claims claims = jwtService.getClaimsFromToken(token);
    // You can use claims to perform any user-specific logic
    feedback.setUserId(claims.getSubject());
    Feedback newFeedback = service.upsertFeedback(feedback);

    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}")
        .buildAndExpand(newFeedback.getId())
        .toUri();

    return ResponseEntity.created(location).build();
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<PagedModel<FeedbackModel>> generateEmptyPagedModel() {
    var emptyPagedModel = (PagedModel<FeedbackModel>) pagedResourcesAssembler
        .toEmptyModel(Page.empty(), FeedbackModel.class);
    return new ResponseEntity<>(emptyPagedModel, HttpStatus.OK);
  }

  private String getTokenFromCookie(HttpServletRequest request) {
    String token = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("jwt".equals(cookie.getName())) {
          token = cookie.getValue();
          break;
        }
      }
    }
    return token;
  }
}
