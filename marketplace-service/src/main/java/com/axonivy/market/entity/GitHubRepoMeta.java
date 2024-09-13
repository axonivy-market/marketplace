package com.axonivy.market.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

import static com.axonivy.market.constants.EntityConstants.GH_REPO_META;

@Getter
@Setter
@Document(GH_REPO_META)
public class GitHubRepoMeta {
  @Id
  private String repoURL;
  private String repoName;
  private Long lastChange;
  private String lastSHA1;
  @LastModifiedDate
  private Date updatedAt;
}
