package com.axonivy.market.model;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.enums.TestEnviroment;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import java.util.EnumMap;
import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GithubReposModel extends RepresentationModel<GithubReposModel> {

  @EqualsAndHashCode.Include
  @Schema(description = "Repository name", example = "my-awesome-repo")
  private String name;

  @EqualsAndHashCode.Include
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
  private Map<WorkFlowType, Map<TestEnviroment, Map<TestStatus, Integer>>> testResults;

  public static Map<WorkFlowType, Map<TestEnviroment, Map<TestStatus, Integer>>>
  processTestResults(GithubRepo githubRepo) {
    Map<WorkFlowType, Map<TestEnviroment, Map<TestStatus, Integer>>> testResults = new EnumMap<>(WorkFlowType.class);

    if (githubRepo.getTestSteps() != null) {
      for (var testStep : githubRepo.getTestSteps()) {
        WorkFlowType workflowType = testStep.getType();
        TestEnviroment envType = testStep.getTestType();
        TestStatus status = testStep.getStatus();
        if (status == TestStatus.SKIPPED) {
          continue;
        }

        if (envType == TestEnviroment.REAL || envType == TestEnviroment.MOCK) {
          testResults
              .computeIfAbsent(workflowType, k -> new EnumMap<>(TestEnviroment.class))
              .computeIfAbsent(envType, k -> new EnumMap<>(TestStatus.class))
              .merge(status, 1, Integer::sum);
        } else {
          testResults
              .computeIfAbsent(workflowType, k -> new EnumMap<>(TestEnviroment.class))
              .computeIfAbsent(TestEnviroment.OTHER, k -> new EnumMap<>(TestStatus.class))
              .merge(status, 1, Integer::sum);
        }

        testResults
            .computeIfAbsent(workflowType, k -> new EnumMap<>(TestEnviroment.class))
            .computeIfAbsent(TestEnviroment.ALL, k -> new EnumMap<>(TestStatus.class))
            .merge(status, 1, Integer::sum);
      }
    }
    return testResults;
  }

  public static GithubReposModel createGihubRepoModel(GithubRepo githubRepo) {
    Map<WorkFlowType, Map<TestEnviroment, Map<TestStatus, Integer>>> testResults = processTestResults(githubRepo);
    String lastUpdated = determineLastUpdated(githubRepo);
    return GithubReposModel.builder()
        .name(githubRepo.getName())
        .htmlUrl(githubRepo.getHtmlUrl())
        .language(githubRepo.getLanguage())
        .lastUpdated(lastUpdated)
        .ciBadgeUrl(githubRepo.getCiBadgeUrl())
        .devBadgeUrl(githubRepo.getDevBadgeUrl())
        .testResults(testResults)
        .build();
  }

  public static String determineLastUpdated(GithubRepo githubRepo) {
    if (githubRepo.getLastUpdated() != null) {
      return githubRepo.getLastUpdated().toInstant().toString();
    }
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GithubReposModel that = (GithubReposModel) o;

    return name != null ? name.equals(that.name) : that.name == null &&
        htmlUrl != null ? htmlUrl.equals(that.htmlUrl) : that.htmlUrl == null;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (htmlUrl != null ? htmlUrl.hashCode() : 0);
    return result;
  }

}