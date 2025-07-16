package com.axonivy.market.model;

import com.axonivy.market.entity.GithubRepo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GithubReposModel extends RepresentationModel<GithubReposModel> {

  @Schema(description = "Repository name", example = "my-awesome-repo")
  private String name;

  @Schema(description = "Repository HTML URL", example = "https://github.com/axonivy-market/my-awesome-repo")
  private String htmlUrl;

  @Schema(description = "Main programming language used", example = "Java")
  private String language;

  @Schema(description = "Last updated date of the repository", example = "2025-07-14T10:35:00Z")
  private String lastUpdated;

  @Schema(description = "CI workflow badge URL", example = "https://github.com/org/repo/actions/workflows/ci.yml/badge.svg")
  private String ciBadgeUrl;

  @Schema(description = "DEV workflow badge URL", example = "https://github.com/org/repo/actions/workflows/dev.yml/badge.svg")
  private String devBadgeUrl;

  @Schema(description = "List of workflow test result summaries")
  private List<WorkflowRepoModel> workflowRepo;

 public static GithubReposModel createModel(GithubRepo githubRepo) {
    List<WorkflowRepoModel> workflows = githubRepo.getWorkflows() != null
        ? githubRepo.getWorkflows().stream()
        .map(wf -> {
          WorkflowRepoModel workflowModel = new WorkflowRepoModel();
          workflowModel.setType(wf.getType());
          workflowModel.setPassed(wf.getPassed());
          workflowModel.setFailed(wf.getFailed());
          workflowModel.setMockPassed(wf.getMockPassed());
          workflowModel.setMockFailed(wf.getMockFailed());
          workflowModel.setRealPassed(wf.getRealPassed());
          workflowModel.setRealFailed(wf.getRealFailed());
          return workflowModel;
        })
        .collect(Collectors.toList())
        : Collections.emptyList();

    return GithubReposModel.builder()
        .name(githubRepo.getName())
        .htmlUrl(githubRepo.getHtmlUrl())
        .language(githubRepo.getLanguage())
        .lastUpdated(githubRepo.getLastUpdated() != null
            ? githubRepo.getLastUpdated().toInstant().toString()
            : null)
        .ciBadgeUrl(githubRepo.getCiBadgeUrl())
        .devBadgeUrl(githubRepo.getDevBadgeUrl())
        .workflowRepo(workflows)
        .build();
  }
}