package com.axonivy.market.entity;

import static com.axonivy.market.constants.EntityConstants.GH_REPO_META;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(GH_REPO_META)
public class GitHubRepoMeta {
  @Id
  private String repoURL;
  private String repoName;
  private Long lastChange;
  private String lastSHA1;
}
