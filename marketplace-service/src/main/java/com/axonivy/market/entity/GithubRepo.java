package com.axonivy.market.entity;

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
import org.kohsuke.github.GHObject;

import java.io.IOException;
import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

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

  private String name;
  private String productId;
  private String htmlUrl;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = REPOSITORY_ID)
  private Set<WorkflowInformation> workflowInformation;
  private Boolean focused;
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = REPOSITORY_ID)
  private Set<TestStep> testSteps;

  public static GithubRepo from(GHObject repo, String productId) throws IOException {
    return GithubRepo.builder()
        .productId(productId)
        .htmlUrl(repo.getHtmlUrl().toString())
        .workflowInformation(new HashSet<>())
        .testSteps(new HashSet<>())
        .build();
  }
}
