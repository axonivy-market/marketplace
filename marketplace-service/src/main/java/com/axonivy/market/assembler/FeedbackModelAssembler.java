package com.axonivy.market.assembler;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.service.GithubUserService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.List;

@Log4j2
@Component
public class FeedbackModelAssembler implements RepresentationModelAssembler<Feedback, FeedbackModel> {

  private final GithubUserService githubUserService;

  public FeedbackModelAssembler(GithubUserService githubUserService) {
    this.githubUserService = githubUserService;
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
    GithubUser githubUser;
    try {
      githubUser = githubUserService.findUser(feedback.getUserId());
    } catch (NotFoundException e) {
      log.warn(e.getMessage());
      githubUser = new GithubUser();
    }
    model.setId(feedback.getId());
    model.setUserId(githubUser.getId());
    model.setUsername(StringUtils.isBlank(githubUser.getName()) ? githubUser.getUsername() : githubUser.getName());
    model.setUserAvatarUrl(githubUser.getAvatarUrl());
    model.setUserProvider(githubUser.getProvider());
    model.setProductId(feedback.getProductId());
    model.setProductNames(feedback.getProductNames());
    model.setContent(feedback.getContent());
    model.setRating(feedback.getRating());
    model.setCreatedAt(feedback.getCreatedAt());
    model.setUpdatedAt(feedback.getUpdatedAt());
    model.setFeedbackStatus(feedback.getFeedbackStatus());
    model.setModeratorName(feedback.getModeratorName());
    model.setReviewDate(feedback.getReviewDate());
    model.setVersion(feedback.getVersion());
    return model;
  }

}
