package com.axonivy.market.util;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.model.TestResults;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestResultsUtils {

  public static List<TestResults> processTestResults(GithubRepo githubRepo) {
    if (githubRepo.getTestSteps() == null) {
      return Collections.emptyList();
    }
    Map<String, Integer> groupedCounts = countGroupedResults(githubRepo);
    List<TestResults> testResults = mapCountsToResults(groupedCounts, githubRepo);
    testResults.forEach(results -> updateBadgeUrl(results, githubRepo));
    return testResults;
  }

  private static void updateBadgeUrl(TestResults results, GithubRepo githubRepo) {
    String badgeUrl = switch (results.getWorkflow()) {
      case CI -> githubRepo.getCiBadgeUrl();
      case DEV -> githubRepo.getDevBadgeUrl();
      case E2E -> githubRepo.getE2eBadgeUrl();
    };
    results.setBadgeUrl(badgeUrl);
  }

  private static Map<String, Integer> countGroupedResults(GithubRepo githubRepo) {
    Map<String, Integer> groupedCounts = new HashMap<>();
    for (TestStep step : githubRepo.getTestSteps()) {
      if (step.getStatus() == TestStatus.SKIPPED) {
        continue;
      }
      var workflowType = step.getType();
      TestStatus status = step.getStatus();

      String key = workflowType + "-" + status;
      groupedCounts.merge(key, 1, Integer::sum);
    }
    return groupedCounts;
  }

  private static List<TestResults> mapCountsToResults(Map<String, Integer> counts, GithubRepo githubRepo) {
    List<TestResults> results = new ArrayList<>();
    if (StringUtils.isNotBlank(githubRepo.getCiBadgeUrl())) {
      results.add(buildInitialTestResults(WorkFlowType.CI));
    }
    if (StringUtils.isNotBlank(githubRepo.getDevBadgeUrl())) {
      results.add(buildInitialTestResults(WorkFlowType.DEV));
    }
    if (StringUtils.isNotBlank(githubRepo.getE2eBadgeUrl())) {
      results.add(buildInitialTestResults(WorkFlowType.E2E));
    }
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      String[] parts = entry.getKey().split("-");
      var workFlowType = WorkFlowType.valueOf(parts[0]);
      var targetTestResults =
          results.stream().filter(testResults -> workFlowType == testResults.getWorkflow()).findAny().orElseGet(
              () -> updateTestResultFromWorkflow(workFlowType, results)
          );
      targetTestResults.getResults().put(parts[1], entry.getValue());
    }
    return results;
  }

  private static TestResults updateTestResultFromWorkflow(WorkFlowType workFlowType, List<TestResults> results) {
    var testResult = TestResults.builder()
        .workflow(workFlowType)
        .results(new HashMap<>())
        .build();
    results.add(testResult);
    return testResult;
  }

  private static TestResults buildInitialTestResults(WorkFlowType workFlowType) {
    return TestResults.builder()
        .workflow(workFlowType)
        .results(new HashMap<>())
        .build();
  }
}
