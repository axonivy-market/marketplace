package com.axonivy.market.util;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestResults;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestResultsUtils {

  private static final int TEST_TYPE_INDEX = 2;
  public static List<TestResults> processTestResults(GithubRepo githubRepo) {
    if (githubRepo.getTestSteps() == null) {
      return Collections.emptyList();
    }
    Map<String, Integer> groupedCounts = countGroupedResults(githubRepo);
    Map<String, Integer> allCounts = countAllResults(githubRepo);
    List<TestResults> results = new ArrayList<>();
    results.addAll(mapCountsToResults(groupedCounts));
    results.addAll(mapCountsToResults(allCounts));
    return results;
  }

  private static Map<String, Integer> countGroupedResults(GithubRepo githubRepo) {
    Map<String, Integer> groupedCounts = new HashMap<>();
    for (TestStep step : githubRepo.getTestSteps()) {
      if (step.getStatus() == TestStatus.SKIPPED) {
        continue;
      }
      var workflowType = step.getType();
      TestStatus status = step.getStatus();
      var envType = step.getTestType().toString();
      if ("OTHER".equals(envType)) {
        envType = "MOCK";
      }
      String key = workflowType + "-" + envType + "-" + status;
      groupedCounts.merge(key, 1, Integer::sum);
    }
    return groupedCounts;
  }

  private static Map<String, Integer> countAllResults(GithubRepo githubRepo) {
    Map<String, Integer> allCounts = new HashMap<>();
    for (TestStep step : githubRepo.getTestSteps()) {
      if (step.getStatus() == TestStatus.SKIPPED) {
        continue;
      }
      var workflowType = step.getType();
      TestStatus status = step.getStatus();
      String allKey = workflowType + "-ALL-" + status;
      allCounts.merge(allKey, 1, Integer::sum);
    }
    return allCounts;
  }

  private static List<TestResults> mapCountsToResults(Map<String, Integer> counts) {
    List<TestResults> results = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      String[] parts = entry.getKey().split("-");
      results.add(TestResults.builder()
          .workflow(WorkFlowType.valueOf(parts[0]))
          .environment(parts[1])
          .status(TestStatus.valueOf(parts[TEST_TYPE_INDEX]))
          .count(entry.getValue())
          .build());
    }
    return results;
  }
}
