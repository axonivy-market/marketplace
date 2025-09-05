package com.axonivy.market.entity;

import com.axonivy.market.constants.GitHubConstants.Repository;
import com.axonivy.market.model.WorkflowInformation;
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
import org.apache.logging.log4j.util.Strings;
import org.kohsuke.github.GHRepository;

import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.constants.EntityConstants.GITHUB_REPO;
import static com.axonivy.market.constants.EntityConstants.REPOSITORY_ID;

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
  @Deprecated(forRemoval = true, since = "1.17.0")
  private String name;
  private String productId;
  private String htmlUrl;
  @Deprecated(forRemoval = true, since = "1.17.0")
  private String language;
  @Deprecated(forRemoval = true, since = "1.17.0")
  private Date lastUpdated;
  @Deprecated(forRemoval = true, since = "1.17.0")
  private String ciBadgeUrl;
  @Deprecated(forRemoval = true, since = "1.17.0")
  private String devBadgeUrl;
  @Deprecated(forRemoval = true, since = "1.17.0")
  private String e2eBadgeUrl;
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = REPOSITORY_ID)
  private List<WorkflowInformation> workflowInformation;
  private Boolean focused;
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = REPOSITORY_ID)
  private List<TestStep> testSteps;

  public static GithubRepo from(GHRepository repo, String productId) throws IOException {
    return GithubRepo.builder()
        .productId(getProductId(repo.getName(), productId))
        .htmlUrl(repo.getHtmlUrl().toString())
        .workflowInformation(new ArrayList<>())
        .testSteps(new ArrayList<>())
        .build();
  }

  private static String getProductId(String repoName, String productId) {
    return PREFIX_TO_PRODUCT.entrySet().stream()
        .filter(e -> repoName.startsWith(e.getKey()))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(productId);
  }

  private static final Map<String, String> PREFIX_TO_PRODUCT = Map.of(
      Repository.MSGRAPH_CONNECTOR, Repository.MSGRAPH_CONNECTOR,
      Repository.DOC_FACTORY, Repository.DOC_FACTORY,
      Repository.DEMO_PROJECTS, Strings.EMPTY
  );
}
