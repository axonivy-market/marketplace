package com.axonivy.market.assembler;

import com.axonivy.market.controller.ProductDetailsController;
import com.axonivy.market.model.GithubReleaseModel;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.GHRelease;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;


import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Log4j2
@Component
public class GithubReleaseModelAssembler extends RepresentationModelAssemblerSupport<GithubReleaseModel, GithubReleaseModel> {

  public GithubReleaseModelAssembler() {
    super(ProductDetailsController.class, GithubReleaseModel.class);
  }

  @Override
  public GithubReleaseModel toModel(GithubReleaseModel githubReleaseModel) {
    return githubReleaseModel;
//    GithubReleaseModel resource = new GithubReleaseModel();
//    return createResource(resource, ghRelease);
  }

//  private GithubReleaseModel createResource(GithubReleaseModel model, GHRelease ghRelease) {
//    return model;
//  }
}
