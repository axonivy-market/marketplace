package com.axonivy.market.assembler;

import com.axonivy.market.entity.Feedback;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.model.GithubReleaseModel;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.GHRelease;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class GithubReleaseModelAssembler extends RepresentationModelAssemblerSupport<GHRelease, GithubReleaseModel> {
  private final GitHubService gitHubService;

  public GithubReleaseModelAssembler(GitHubService gitHubService) {
    super(GHRelease.class, GithubReleaseModel.class);
    this.gitHubService = gitHubService;
  }
  @Override
  public GithubReleaseModel toModel(GHRelease entity) {
    GithubReleaseModel resource = new GithubReleaseModel();
    resource.add();
    return null;
  }
}
