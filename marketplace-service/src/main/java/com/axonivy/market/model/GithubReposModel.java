package com.axonivy.market.model;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestResults;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Date;
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

  @Schema(description = "Main programming language used", example = "Java")
  private String language;

  @Schema(description = "Last updated date of the repository", example = "2025-07-14T10:35:00Z")
  private Date lastUpdated;

  @Schema(description = "CI workflow badge URL", example = "https://github.com/actions/workflows/ci.yml/badge.svg")
  private String ciBadgeUrl;

  @Schema(description = "DEV workflow badge URL", example = "https://github.com/actions/workflows/dev.yml/badge.svg")
  private String devBadgeUrl;

  @Schema(description = "Indicates if the repository is a focused repository", example = "true")
  private boolean focused;
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
        .language(githubRepo.getLanguage())
        .lastUpdated(githubRepo.getLastUpdated())
        .ciBadgeUrl(githubRepo.getCiBadgeUrl())
        .devBadgeUrl(githubRepo.getDevBadgeUrl())
        .focused(githubRepo.isFocused())
        .testResults(testResults)
        .build();
  }
}