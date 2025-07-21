package com.axonivy.market.util;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TesResults;
import com.axonivy.market.enums.TestEnviroment;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.enums.TestEnviroment.MOCK;
import static com.axonivy.market.enums.TestEnviroment.REAL;
import static com.axonivy.market.enums.TestStatus.SKIPPED;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestResultsUtils {

  public static List<TesResults> processTestResults(GithubRepo githubRepo) {
    List<TesResults> results = new ArrayList<>();
    Map<String, Integer> counters = calculateCounters(githubRepo);
    for (var entry : counters.entrySet()) {
      addResult(results, entry);
    }
    return results;
  }

  public static Map<String, Integer> calculateCounters(GithubRepo githubRepo) {
    Map<String, Integer> counters = new HashMap<>();
    for (var testStep : githubRepo.getTestSteps()) {
      var workflowType = testStep.getType();
      var envType = testStep.getTestType();
      var status = testStep.getStatus();
      if (SKIPPED.equals(status)) {
        continue;
      }
      var key = generateKey(workflowType, envType, status);
      counters.merge(key, 1, Integer::sum);
      var allKey = workflowType + "-ALL-" + status;
      counters.merge(allKey, 1, Integer::sum);
    }
    return counters;
  }

  public static String generateKey(WorkFlowType workflowType, TestEnviroment envType, TestStatus status) {
    if (REAL.equals(envType) || MOCK.equals(envType)) {
      return workflowType + "-" + envType + "-" + status;
    }
    return workflowType + "-OTHER-" + status;
  }

  public static void addResult(List<TesResults> results, Map.Entry<String, Integer> entry) {
    String[] parts = entry.getKey().split("-");
    if (parts.length == 3) {
      var item = TesResults.builder()
          .workflow(parts[0])
          .environment(parts[1])
          .status(parts[2])
          .count(entry.getValue())
          .build();
      results.add(item);
    }
  }
}

