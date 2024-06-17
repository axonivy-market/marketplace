package com.axonivy.market.assembler;

import com.axonivy.market.controller.FeedbackController;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.model.FeedbackModel;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FeedbackModelAssembler extends RepresentationModelAssemblerSupport<Feedback, FeedbackModel> {

  public FeedbackModelAssembler() {
    super(Feedback.class, FeedbackModel.class);
  }

  @Override
  public FeedbackModel toModel(Feedback feedback) {
    FeedbackModel resource = new FeedbackModel();
    resource.add(linkTo(methodOn(FeedbackController.class).findFeedback(feedback.getId()))
        .withSelfRel());
    return createResource(resource, feedback);
  }

  private FeedbackModel createResource(FeedbackModel model, Feedback feedback) {
    model.setId(feedback.getId());
    model.setUserId(feedback.getUserId());
    model.setContent(feedback.getContent());
    model.setRating(feedback.getRating());
    model.setCreatedAt(feedback.getCreatedAt());
    model.setUpdatedAt(feedback.getUpdatedAt());
    return model;
  }

}
