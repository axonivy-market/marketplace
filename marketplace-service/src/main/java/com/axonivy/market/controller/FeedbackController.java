package com.axonivy.market.controller;

import com.axonivy.market.assembler.FeedbackModelAssembler;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.FeedbackApprovalModel;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.model.FeedbackModelRequest;
import com.axonivy.market.model.ProductRating;
import com.axonivy.market.service.FeedbackService;
import com.axonivy.market.service.JwtService;
import com.axonivy.market.util.validator.AuthorizationUtils;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.constants.RequestParamConstants.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping(FEEDBACK)
@Tag(name = "User Feedback Controllers", description = "API collection to handle user's feedback.")
public class FeedbackController {

  private final FeedbackService feedbackService;
  private final JwtService jwtService;
  private final GitHubService gitHubService;
  private final FeedbackModelAssembler feedbackModelAssembler;

  private final PagedResourcesAssembler<Feedback> pagedResourcesAssembler;

  public FeedbackController(FeedbackService feedbackService, JwtService jwtService, GitHubService gitHubService,
      FeedbackModelAssembler feedbackModelAssembler, PagedResourcesAssembler<Feedback> pagedResourcesAssembler) {
    this.feedbackService = feedbackService;
    this.jwtService = jwtService;
    this.gitHubService = gitHubService;
    this.feedbackModelAssembler = feedbackModelAssembler;
    this.pagedResourcesAssembler = pagedResourcesAssembler;
  }

  @GetMapping(PRODUCT_BY_ID)
  @Operation(summary = "Find feedbacks by product id with lazy loading",
      description = "Get all user feedback by product id (from meta.json) with lazy loading", parameters = {
      @Parameter(name = "page", description = "Page number to retrieve", in = ParameterIn.QUERY, example = "0",
          required = true),
      @Parameter(name = "size", description = "Number of items per page", in = ParameterIn.QUERY, example = "20",
          required = true),
      @Parameter(name = "sort",
          description = "Sorting criteria in the format: Sorting criteria(popularity|alphabetically|recent), Sorting " +
              "order(asc|desc)",
          in = ParameterIn.QUERY, example = "[\"popularity\",\"asc\"]", required = true)})
  public ResponseEntity<PagedModel<FeedbackModel>> findFeedbacks(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "portal",
          in = ParameterIn.PATH) String productId,
      @ParameterObject Pageable pageable) {
    Page<Feedback> results = feedbackService.findFeedbacks(productId, pageable);
    if (results.isEmpty()) {
      return generateEmptyPagedModel();
    }
    var responseContent = new PageImpl<>(results.getContent(), pageable, results.getTotalElements());
    var pageResources = pagedResourcesAssembler.toModel(responseContent, feedbackModelAssembler);
    return new ResponseEntity<>(pageResources, HttpStatus.OK);
  }

  @GetMapping(BY_ID)
  @Operation(summary = "Find all feedbacks by product id",
      description = "Get all feedbacks by product id(from meta.json) which is used in mobile viewport.")
  public ResponseEntity<FeedbackModel> findFeedback(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "portal",
          in = ParameterIn.PATH) String id) {
    Feedback feedback = feedbackService.findFeedback(id);
    FeedbackModel model = feedbackModelAssembler.toModel(feedback);
    addModelLinks(model, feedback);
    return ResponseEntity.ok(model);
  }

  @GetMapping()
  @Operation(summary = "Find all feedbacks by user id and product id",
      description = "Get current user feedback on target product.")
  public ResponseEntity<List<FeedbackModel>> findFeedbackByUserIdAndProductId(
      @RequestParam(USER_ID) @Parameter(description = "Id of current user from DB", example = "1234",
          in = ParameterIn.QUERY) String userId,
      @RequestParam("productId") @Parameter(description = "Product id (from meta.json)", example = "portal",
          in = ParameterIn.QUERY) String productId) {
    List<Feedback> feedbacks = feedbackService.findFeedbackByUserIdAndProductId(userId, productId);
    return new ResponseEntity<>(feedbackModelAssembler.toModel(feedbacks), HttpStatus.OK);
  }

  @GetMapping(FEEDBACK_APPROVAL)
  @Operation(hidden = true)
  public ResponseEntity<PagedModel<FeedbackModel>> findAllFeedbacks(
      @RequestHeader(value = AUTHORIZATION) String authorizationHeader, @ParameterObject Pageable pageable) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserInOrganizationAndTeam(token,
        GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME);
    Page<Feedback> results = feedbackService.findAllFeedbacks(pageable);
    if (results.isEmpty()) {
      return generateEmptyPagedModel();
    }
    var responseContent = new PageImpl<>(results.getContent(), pageable, results.getTotalElements());
    var pageResources = pagedResourcesAssembler.toModel(responseContent, feedbackModelAssembler);
    return new ResponseEntity<>(pageResources, HttpStatus.OK);
  }

  @PutMapping(FEEDBACK_APPROVAL)
  @Operation(hidden = true)
  public ResponseEntity<FeedbackModel> updateFeedbackWithNewStatus(
      @RequestBody @Valid FeedbackApprovalModel feedbackApproval) {
    Feedback feedback = feedbackService.updateFeedbackWithNewStatus(feedbackApproval);
    FeedbackModel model = feedbackModelAssembler.toModel(feedback);
    addModelLinks(model, feedback);
    return ResponseEntity.ok(model);
  }

  @PostMapping
  @Operation(summary = "Create user feedback",
      description = "Save user feedback of product with their token from Github account.")
  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Example request body for feedback",
      content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
          schema = @Schema(implementation = FeedbackModelRequest.class)))
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Successfully created user feedback"),
      @ApiResponse(responseCode = "401", description = "Unauthorized request")})
  public ResponseEntity<Void> createFeedback(@RequestBody @Valid FeedbackModelRequest feedbackRequest,
      @RequestHeader(value = X_AUTHORIZATION) @Parameter(description = "JWT Bearer token", example = "Bearer 123456",
          in = ParameterIn.HEADER) String bearerToken) {
    String token = AuthorizationUtils.getBearerToken(bearerToken);

    // Validate the token
    if (token == null || !jwtService.validateToken(token)) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Unauthorized if token is missing or invalid
    }

    Claims claims = jwtService.getClaimsFromToken(token);
    Feedback newFeedback = feedbackService.upsertFeedback(feedbackRequest, claims.getSubject());

    URI location = ServletUriComponentsBuilder.fromCurrentRequest().path(BY_ID).buildAndExpand(newFeedback.getId())
        .toUri();

    return ResponseEntity.created(location).build();
  }

  @Operation(summary = "Find rating information of product by its id.",
      description = "Get overall rating of product by its id.")
  @GetMapping(PRODUCT_RATING_BY_ID)
  public ResponseEntity<List<ProductRating>> getProductRating(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "portal",
          in = ParameterIn.PATH) String productId) {
    return ResponseEntity.ok(feedbackService.getProductRatingById(productId));
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<PagedModel<FeedbackModel>> generateEmptyPagedModel() {
    var emptyPagedModel = (PagedModel<FeedbackModel>) pagedResourcesAssembler.toEmptyModel(Page.empty(),
        FeedbackModel.class);
    return new ResponseEntity<>(emptyPagedModel, HttpStatus.OK);
  }

  private void addModelLinks(FeedbackModel model, Feedback feedback){
    model.add(linkTo(methodOn(FeedbackController.class).findFeedback(feedback.getId())).withSelfRel());
  }
}
