package com.axonivy.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

import java.util.Date;

import static com.axonivy.market.constants.EntityConstants.GH_REPO_META;

@Getter
@Setter
@Entity
@Table(name = GH_REPO_META)
public class GitHubRepoMeta {
  @Id
  private String repoURL;
  private String repoName;
  private Long lastChange;
  private String lastSHA1;
  @LastModifiedDate
  private Date updatedAt;
}
