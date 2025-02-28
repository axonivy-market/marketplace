package com.axonivy.market.assembler;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.model.GitHubReleaseModel;
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;

import org.springframework.stereotype.Component;

@Log4j2
@Component
public class GithubReleaseModelAssembler extends RepresentationModelAssemblerSupport<GitHubReleaseModel, GitHubReleaseModel> {

  public GithubReleaseModelAssembler() {
    super(ProductDetailsController.class, GitHubReleaseModel.class);
  }

  @Override
  public GitHubReleaseModel toModel(GitHubReleaseModel githubReleaseModel) {
    return githubReleaseModel;
  }
}
