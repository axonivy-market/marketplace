package com.axonivy.market.model;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestStep;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.util.Collections;
import java.util.List;

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

  @Schema(description = "CI workflow badge URL", example = "https://github.com/actions/workflows/ci.yml/badge.svg")
  private String ciBadgeUrl;

  @Schema(description = "DEV workflow badge URL", example = "https://github.com/actions/workflows/dev.yml/badge.svg")
  private String devBadgeUrl;

  @Schema(description = "List of workflow test result summaries")
  private List<TestStepsModel> testStepsModels;
  public static GithubReposModel createGihubRepoModel(GithubRepo githubRepo) {
    List<TestStepsModel> testStepsModelList;

    if (githubRepo.getTestSteps() != null) {
      testStepsModelList = githubRepo.getTestSteps().stream()
          .map((TestStep testStep) -> {
            var testStepsModel = new TestStepsModel();
            testStepsModel.setName(testStep.getName());
            testStepsModel.setStatus(testStep.getStatus());
            testStepsModel.setType(testStep.getType());
            testStepsModel.setTestType(testStep.getTestType());
            return testStepsModel;
          })
          .toList();
    } else {
      testStepsModelList = Collections.emptyList();
    }

    String lastUpdated = null;
    if (githubRepo.getLastUpdated() != null) {
      lastUpdated = githubRepo.getLastUpdated().toInstant().toString();
    }

    return GithubReposModel.builder()
        .name(githubRepo.getName())
        .htmlUrl(githubRepo.getHtmlUrl())
        .language(githubRepo.getLanguage())
        .lastUpdated(lastUpdated)
        .ciBadgeUrl(githubRepo.getCiBadgeUrl())
        .devBadgeUrl(githubRepo.getDevBadgeUrl())
        .testStepsModels(testStepsModelList)
        .build();
  }

}