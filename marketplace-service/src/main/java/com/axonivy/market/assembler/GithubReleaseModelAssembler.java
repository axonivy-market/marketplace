package com.axonivy.market.assembler;

import com.axonivy.market.model.GithubReleaseModel;
import lombok.extern.log4j.Log4j2;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import org.springframework.stereotype.Component;

@Log4j2
@Component
public class GithubReleaseModelAssembler implements RepresentationModelAssembler<GithubReleaseModel, GithubReleaseModel> {

  @Override
  public GithubReleaseModel toModel(GithubReleaseModel githubReleaseModel) {
    return githubReleaseModel;
  }
}
