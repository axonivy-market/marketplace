package com.axonivy.market.entity;

import static com.axonivy.market.constants.EntityConstants.GITHUB_REPO_META;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(GITHUB_REPO_META)
public class GithubRepoMeta {
  @Id
  private String repoURL;
  private String repoName;
  private Long lastChange;
}
