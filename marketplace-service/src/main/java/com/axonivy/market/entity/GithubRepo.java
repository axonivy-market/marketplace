package com.axonivy.market.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.kohsuke.github.GHRepository;

import static com.axonivy.market.constants.EntityConstants.GITHUB_REPO;

import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = GITHUB_REPO)
public class GithubRepo extends GenericIdEntity {
  @Serial
  private static final long serialVersionUID = 1L;
  private String name;
  private String htmlUrl;
  @Deprecated(forRemoval = true, since = "1.17.0")
  private String language;
  private Date lastUpdated;
  @Deprecated(forRemoval = true, since = "1.17.0")
  private String ciBadgeUrl;
  @Deprecated(forRemoval = true, since = "1.17.0")
  private String devBadgeUrl;
  @Deprecated(forRemoval = true, since = "1.17.0")
  private String e2eBadgeUrl;
  private String ciConclusion;
  private String devConclusion;
  private String e2eConclusion;
  private Boolean focused;
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "repository_id")
  private List<TestStep> testSteps;

  public static GithubRepo from(GHRepository repo) throws IOException {
    return GithubRepo.builder()
        .name(repo.getName())
        .htmlUrl(repo.getHtmlUrl().toString())
        .lastUpdated(repo.getUpdatedAt())
        .testSteps(new ArrayList<>())
        .build();
  }
}
