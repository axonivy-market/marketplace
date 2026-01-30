package com.axonivy.market.assembler;

import com.axonivy.market.model.GitHubReleaseModel;
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class GithubReleaseModelAssembler implements RepresentationModelAssembler<GitHubReleaseModel,
    GitHubReleaseModel> {

  @Override
  public GitHubReleaseModel toModel(GitHubReleaseModel githubReleaseModel) {
    return githubReleaseModel;
  }
}
