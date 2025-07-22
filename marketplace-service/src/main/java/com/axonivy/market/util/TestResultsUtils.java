package com.axonivy.market.util;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TesResults;
import com.axonivy.market.enums.TestEnviroment;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestResultsUtils {


  public static List<TesResults> processTestResults(GithubRepo githubRepo) {
    if (githubRepo.getTestSteps() == null) {
      return Collections.emptyList();
    }
    var groupedCounts = githubRepo.getTestSteps().stream()
        .filter(step -> step.getStatus() != TestStatus.SKIPPED)
        .map(step -> {
          var workflowType = step.getType();
          var envType = step.getTestType() == TestEnviroment.OTHER ? TestEnviroment.MOCK : step.getTestType();
          var status = step.getStatus();
          return List.of(workflowType, envType.name(), status);
        })
        .collect(Collectors.groupingBy(key -> key, Collectors.counting()));

    var allCounts = githubRepo.getTestSteps().stream()
        .filter(step -> step.getStatus() != TestStatus.SKIPPED)
        .collect(Collectors.groupingBy(
            step -> List.of(step.getType(), "ALL", step.getStatus()),
            Collectors.counting()
        ));

    return Stream.concat(groupedCounts.entrySet().stream(), allCounts.entrySet().stream())
        .map(entry -> {
          var keyList = entry.getKey();
          return TesResults.builder()
              .workflow((WorkFlowType) keyList.get(0))
              .environment((String) keyList.get(1))
              .status((TestStatus) keyList.get(2))
              .count(entry.getValue().intValue())
              .build();
        })
        .toList();
  }
}

