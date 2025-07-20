package com.axonivy.market.util;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.enums.TestEnviroment;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.axonivy.market.constants.CommonConstants.*;
import static com.axonivy.market.enums.TestEnviroment.MOCK;
import static com.axonivy.market.enums.TestEnviroment.REAL;
import static com.axonivy.market.enums.TestStatus.SKIPPED;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestResultsUtils {
    public static Object processTestResults(GithubRepo githubRepo) {
        List<Object> results = new ArrayList<>();
        if (githubRepo.getTestSteps() != null) {
            Map<String, Integer> counters = calculateCounters(githubRepo);
            for (var entry : counters.entrySet()) {
                addResult(results, entry);
            }
        }
        return results;
    }

    public static Map<String, Integer> calculateCounters(GithubRepo githubRepo) {
        Map<String, Integer> counters = new HashMap<>();
        for (var testStep : githubRepo.getTestSteps()) {
            WorkFlowType workflowType = testStep.getType();
            TestEnviroment envType = testStep.getTestType();
            TestStatus status = testStep.getStatus();
            if (SKIPPED.equals(status)) {
                continue;
            }
            String key = generateKey(workflowType, envType, status);
            counters.merge(key, 1, Integer::sum);
            String allKey = workflowType + "-ALL-" + status;
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

    public static void addResult(List<Object> results, Map.Entry<String, Integer> entry) {
        String[] parts = entry.getKey().split("-");
        if (parts.length == 3) {
            Map<String, Object> item = new HashMap<>();
            item.put(WORKFLOW, parts[0]);
            item.put(TEST_ENVIRONMENT, parts[1]);
            item.put(STATUS, parts[2]);
            item.put(COUNT, entry.getValue());
            results.add(item);
        }
    }

  public static String determineLastUpdated(GithubRepo githubRepo) {
    if (githubRepo.getLastUpdated() != null) {
      return githubRepo.getLastUpdated().toString();
    }
    return null;
  }
}

