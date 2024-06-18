package com.axonivy.market.controller;

import com.axonivy.market.assembler.FeedbackModelAssembler;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.repository.FeedbackRepository;
import com.axonivy.market.service.FeedbackService;
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

import static com.axonivy.market.constants.RequestMappingConstants.FEEDBACK;

@RestController
@RequestMapping(FEEDBACK)
public class FeedbackController {

  private final FeedbackService service;
  private final FeedbackModelAssembler assembler;

  private final PagedResourcesAssembler<Feedback> pagedResourcesAssembler;

  public FeedbackController(FeedbackService feedbackService, FeedbackModelAssembler assembler, PagedResourcesAssembler<Feedback> pagedResourcesAssembler) {
    this.service = feedbackService;
    this.assembler = assembler;
    this.pagedResourcesAssembler = pagedResourcesAssembler;
  }

  @Operation(summary = "Find all feedbacks by product id")
  @GetMapping()
  public ResponseEntity<PagedModel<FeedbackModel>> findFeedbacks(@RequestParam String productId, Pageable pageable) {
    Page<Feedback> results = service.findFeedbacks(productId, pageable);
    if (results.isEmpty()) {
      return generateEmptyPagedModel();
    }
    var responseContent = new PageImpl<>(results.getContent(), pageable, results.getTotalElements());
    var pageResources = pagedResourcesAssembler.toModel(responseContent, assembler);
    return new ResponseEntity<>(pageResources, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Feedback> findFeedback(@PathVariable String id) {
    Feedback feedback = service.findFeedback(id);
    return ResponseEntity.ok(feedback);
  }

  @PostMapping
  public ResponseEntity<Void> createFeedback(@RequestBody @Valid Feedback feedback) {
    Feedback newFeedback = service.createFeedback(feedback);

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
}
