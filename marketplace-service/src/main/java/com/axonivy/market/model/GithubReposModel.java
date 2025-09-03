package com.axonivy.market.model;

import com.axonivy.market.entity.GithubRepo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static com.axonivy.market.util.TestResultsUtils.processTestResults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GithubReposModel {

  @EqualsAndHashCode.Include
  @Schema(description = "Repository name", example = "my-awesome-repo")
  private String name;

  @EqualsAndHashCode.Include
  @Schema(description = "Repository HTML URL", example = "https://github.com/axonivy-market/my-awesome-repo")
  private String htmlUrl;

  @Schema(
      example = """
            {
              "workflow": "CI",
               "lastBuilt": "2025-08-28 04:25:24.000",
               "conclusion": "success",
               "lastBuiltRun": "https://github.com/market/excel-connector/actions/runs/17052929095/job/48344635259"
            },
            {
              "workflow": "DEV",
               "lastBuilt": "2025-08-28 04:25:24.000",
               "conclusion": "success",
               "lastBuiltRun": "https://github.com/market/excel-connector/actions/runs/17052929095/job/48344635259"
            },
            {
              "workflow": "E2E",
               "lastBuilt": "2025-08-28 04:25:24.000",
               "conclusion": "success",
               "lastBuiltRun": "https://github.com/market/excel-connector/actions/runs/17052929095/job/48344635259"
            }
          """
  )
  private List<WorkflowInformation> workflowInformation;

  @Schema(description = "Indicates if the repository is a focused repository", example = "true")
  private Boolean focused;
  @Schema(
      description = "Test results summary by workflow type and test environment",
      example = """
            {
              "CI": {
                "all": { "passed": 5, "failed": 10 },
                "mock": { "passed": 2, "failed": 3 },
                "real": { "passed": 3, "failed": 7 }
              },
              "DEV": {
                "all": { "passed": 8, "failed": 15 },
                "mock": { "passed": 4, "failed": 6 },
                "real": { "passed": 4, "failed": 9 }
              }
            }
          """
  )
  private List<TestResults> testResults;

  public static GithubReposModel from(GithubRepo githubRepo) {
    List<TestResults> testResults = processTestResults(githubRepo);
    return GithubReposModel.builder()
        .name(githubRepo.getName())
        .htmlUrl(githubRepo.getHtmlUrl())
        .focused(githubRepo.getFocused())
        .testResults(testResults)
        .workflowInformation(githubRepo.getWorkflowInformation())
        .build();
  }
}