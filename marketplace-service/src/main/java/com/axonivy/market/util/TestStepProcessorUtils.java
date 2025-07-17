package com.axonivy.market.util;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.TestEnviroment;
import com.axonivy.market.enums.WorkFlowType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestStepProcessorUtils {

  private static final String PATTERN_TEST_CASE = "^✅\\s+(.+?)(Real Server Test|Mock Server Test)?$";
  private static final String PATTERN_TEST_CASE_FAILED = "^❌\\s+(.+?)(Real Server Test|Mock Server Test)?$";
  private static final Pattern TEST_CASE_PATTERN = Pattern.compile(PATTERN_TEST_CASE);
  private static final Pattern TEST_CASE_FAILED_PATTERN = Pattern.compile(PATTERN_TEST_CASE_FAILED);

  public static List<TestStep> parseTestSteps(JsonNode testData, GithubRepo repo, WorkFlowType workflowType) {
    List<TestStep> steps = new ArrayList<>();
    var summary = testData.path("output").path("summary").asText();
    var lines = summary.split(CommonConstants.NEW_LINE);

    for (var line : lines) {
      line = line.trim();
      Matcher matcher = TEST_CASE_PATTERN.matcher(line);
      Matcher matcherFailed = TEST_CASE_FAILED_PATTERN.matcher(line);
      if (matcher.find()) {
        String testName = matcher.group(1).trim();
        String testTypeString = matcher.group(2);

        TestEnviroment testType = TestEnviroment.OTHER;
        if (testTypeString != null) {
          if (testTypeString.contains("Real")) {
            testType = TestEnviroment.REAL;
          } else if (testTypeString.contains("Mock")) {
            testType = TestEnviroment.MOCK;
          }
        }
        var step = TestStep.builder()
            .name(testName)
            .status(TestStatus.PASSED)
            .type(workflowType)
            .testType(testType)
            .repository(repo)
            .build();
        steps.add(step);
      } else if (matcherFailed.find()) {
        String testName = matcherFailed.group(1).trim();
        String testTypeString = matcherFailed.group(2);

        TestEnviroment testType = TestEnviroment.OTHER;
        if (testTypeString != null) {
          if (testTypeString.contains("Real")) {
            testType = TestEnviroment.REAL;
          } else if (testTypeString.contains("Mock")) {
            testType = TestEnviroment.MOCK;
          }
        }
        var step = TestStep.builder()
            .name(testName)
            .status(TestStatus.FAILED)
            .type(workflowType)
            .testType(testType)
            .repository(repo)
            .build();
        steps.add(step);
      }
    }
    return steps;
  }
}