package com.axonivy.market.controller;

import com.axonivy.market.assembler.FeedbackModelAssembler;
import com.axonivy.market.assembler.ProductModelAssembler;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.User;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.model.ProductModel;
import com.axonivy.market.repository.FeedbackRepository;
import com.axonivy.market.service.FeedbackService;
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
    private final FeedbackModelAssembler assembler;

    public FeedbackController(FeedbackService feedbackService, FeedbackModelAssembler assembler) {
        this.feedbackService = feedbackService;
        this.assembler = assembler;
    }

    @GetMapping
    public ResponseEntity<List<Feedback>> getAllFeedback() {
        return ResponseEntity.ok(feedbackService.getAllFeedbacks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackModel> findFeedback(@PathVariable String id) {
        Feedback feedback = feedbackService.getFeedback(id);
        return ResponseEntity.ok(assembler.toModel(feedback));
    }

    @PostMapping
    public ResponseEntity<Feedback> createFeedback(@RequestBody Feedback feedback) {
        Feedback newFeedback = feedbackService.createFeedback(feedback);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newFeedback.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
