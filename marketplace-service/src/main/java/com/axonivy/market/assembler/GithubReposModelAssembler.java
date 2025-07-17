package com.axonivy.market.assembler;

import com.axonivy.market.entity.GithubRepo;

import com.axonivy.market.model.GithubReposModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class GithubReposModelAssembler implements RepresentationModelAssembler<GithubRepo, GithubReposModel> {
  @Override
  public GithubReposModel toModel(GithubRepo githubRepo) {
    return GithubReposModel.createGihubRepoModel(githubRepo);
  }
}
