package com.axonivy.market.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReposResponseModel extends RepresentationModel<ReposResponseModel> {
  private List<GithubReposModel> focusedRepos;
  private List<GithubReposModel> standardRepos;
}
