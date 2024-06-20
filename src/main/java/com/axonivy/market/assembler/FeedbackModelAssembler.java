package com.axonivy.market.assembler;

import com.axonivy.market.controller.FeedbackController;
import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.User;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.repository.UserRepository;
import com.axonivy.market.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class FeedbackModelAssembler extends RepresentationModelAssemblerSupport<Feedback, FeedbackModel> {

  private final UserService userService;

  public FeedbackModelAssembler(UserService userService) {
    super(Feedback.class, FeedbackModel.class);
    this.userService = userService;
  }

  @Override
  public FeedbackModel toModel(Feedback feedback) {
    FeedbackModel resource = new FeedbackModel();
    resource.add(linkTo(methodOn(FeedbackController.class).findFeedback(feedback.getId()))
        .withSelfRel());
    return createResource(resource, feedback);
  }

  private FeedbackModel createResource(FeedbackModel model, Feedback feedback) {
    User user = userService.findUser(feedback.getUserId());
    model.setId(feedback.getId());
    model.setUsername(StringUtils.isBlank(user.getName()) ? user.getUsername() : user.getName());
    model.setUserAvatarUrl(user.getAvatarUrl());
    model.setUserProvider(user.getProvider());
    model.setProductId(feedback.getProductId());
    model.setContent(feedback.getContent());
    model.setRating(feedback.getRating());
    model.setCreatedAt(feedback.getCreatedAt());
    model.setUpdatedAt(feedback.getUpdatedAt());
    return model;
  }

}
