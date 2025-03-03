package com.axonivy.market.assembler;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.User;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log4j2
@Component
public class FeedbackModelAssembler implements RepresentationModelAssembler<Feedback, FeedbackModel> {

  private final UserService userService;

  public FeedbackModelAssembler(UserService userService) {
    this.userService = userService;
  }

  @Override
  public FeedbackModel toModel(Feedback feedback) {
    FeedbackModel resource = new FeedbackModel();
    return createResource(resource, feedback);
  }

  public List<FeedbackModel> toModel(List<Feedback> feedbacks) {
    return feedbacks.stream()
        .map(this::toModel)
        .toList();
  }

  private FeedbackModel createResource(FeedbackModel model, Feedback feedback) {
    User user;
    try {
      user = userService.findUser(feedback.getUserId());
    } catch (NotFoundException e) {
      log.warn(e.getMessage());
      user = new User();
    }
    model.setId(feedback.getId());
    model.setUserId(user.getId());
    model.setUsername(StringUtils.isBlank(user.getName()) ? user.getUsername() : user.getName());
    model.setUserAvatarUrl(user.getAvatarUrl());
    model.setUserProvider(user.getProvider());
    model.setProductId(feedback.getProductId());
    model.setContent(feedback.getContent());
    model.setRating(feedback.getRating());
    model.setCreatedAt(feedback.getCreatedAt());
    model.setUpdatedAt(feedback.getUpdatedAt());
    model.setFeedbackStatus(feedback.getFeedbackStatus());
    model.setModeratorName(feedback.getModeratorName());
    model.setReviewDate(feedback.getReviewDate());
    return model;
  }

}
