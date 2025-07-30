package com.axonivy.market.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;
@Getter
@Setter
@EqualsAndHashCode
public class ReposResponseModel extends RepresentationModel<ReposResponseModel> {
  private List<GithubReposModel> focusedRepos;
  private List<GithubReposModel> standardRepos;
}
