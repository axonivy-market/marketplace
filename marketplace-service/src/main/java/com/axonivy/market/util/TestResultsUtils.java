package com.axonivy.market.util;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TesResults;
import com.axonivy.market.enums.TestEnviroment;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.axonivy.market.enums.TestEnviroment.MOCK;
import static com.axonivy.market.enums.TestEnviroment.OTHER;
import static com.axonivy.market.enums.TestStatus.SKIPPED;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestResultsUtils {

  public static List<TesResults> processTestResults(GithubRepo githubRepo) {
    var groupedCounts = githubRepo.getTestSteps().stream().filter(step -> !SKIPPED.equals(step.getStatus()))
        .map(step -> {
          var workflowType = step.getType();
          var envType = step.getTestType();
          var status = step.getStatus();
          if (OTHER.equals(envType)) {
            envType = MOCK;
          }
          return List.of(workflowType, envType, status);
        }).collect(Collectors.groupingBy(key -> key, Collectors.counting()));

    var allCounts = githubRepo.getTestSteps().stream().filter(step -> !SKIPPED.equals(step.getStatus())).map(step -> {
      var workflowType = step.getType();
      var status = step.getStatus();
      return List.of(workflowType, TestEnviroment.ALL, status);
    }).collect(Collectors.groupingBy(key -> key, Collectors.counting()));

    return Stream.concat(groupedCounts.entrySet().stream(), allCounts.entrySet().stream()).map(entry -> {
      var keyList = entry.getKey();
      return TesResults.builder().workflow((WorkFlowType) keyList.get(0)).environment(
          (TestEnviroment) keyList.get(1)).status((TestStatus) keyList.get(2)).count(
          entry.getValue().intValue()).build();
    }).collect(Collectors.toList());
  }

}

