package com.axonivy.market.util;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestResults;
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
    return mapCountsToResults(groupedCounts);
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

  private static List<TestResults> mapCountsToResults(Map<String, Integer> counts) {
    List<TestResults> results = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : counts.entrySet()) {
      String[] parts = entry.getKey().split("-");
      WorkFlowType workFlowType = WorkFlowType.valueOf(parts[0]);
      TestResults targetTestResults =
          results.stream().filter(testResults -> workFlowType == testResults.getWorkflow()).findAny().orElseGet(() -> {
            var a = TestResults.builder()
          .workflow(workFlowType)
          .results(new HashMap<>())
          .build();
            results.add(a);
            return a;
          });
      targetTestResults.getResults().put(parts[1], entry.getValue());
    }
    return results;
  }
}
