package com.axonivy.market.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.axonivy.market.constants.EntityConstants.GH_REPO_META;

import java.io.Serial;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = GH_REPO_META)
public class GitHubRepoMeta extends AbstractAuditableEntity<String> {

  @Serial
  private static final long serialVersionUID = 1;

  @Id
  private String repoURL;
  private String repoName;
  private Long lastChange;
  private String lastSHA1;

  @Override
  public String getId() {
    return repoURL;
  }

  @Override
  public void setId(String id) {
    this.repoURL = id;
  }
}
