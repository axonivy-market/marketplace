package com.axonivy.market.util;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestEnviroment;
import com.axonivy.market.enums.TestStatus;
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
  private static final int TEST_TYPE_INDEX = 2;
  private static final int TEST_NAME_INDEX = 1;
  private static final Pattern TEST_CASE_PATTERN = Pattern.compile(PATTERN_TEST_CASE,
          Pattern.UNICODE_CHARACTER_CLASS);
  private static final Pattern TEST_CASE_FAILED_PATTERN = Pattern.compile(PATTERN_TEST_CASE_FAILED,
          Pattern.UNICODE_CHARACTER_CLASS);

  private static TestEnviroment getTestEnviroment(String testTypeString) {
    TestEnviroment environment = TestEnviroment.OTHER;
    if (testTypeString != null) {
      if (testTypeString.contains("Real")) {
        environment = TestEnviroment.REAL;
      } else if (testTypeString.contains("Mock")) {
        environment = TestEnviroment.MOCK;
      }
    }
    return environment;
  }

  private static TestStep parseTestStepLine(String line, GithubRepo repo, WorkFlowType workflowType) {
    var matcher = TEST_CASE_PATTERN.matcher(line);
    var matcherFailed = TEST_CASE_FAILED_PATTERN.matcher(line);
    if (matcher.find()) {
      String testName = matcher.group(TEST_NAME_INDEX).trim();
      var testTypeString = matcher.group(TEST_TYPE_INDEX);
      var testType = getTestEnviroment(testTypeString);
      return createTestStep(testName, TestStatus.PASSED, workflowType, testType, repo);
    } else if (matcherFailed.find()) {
      String testName = matcherFailed.group(TEST_NAME_INDEX).trim();
      var testTypeString = matcherFailed.group(TEST_TYPE_INDEX);
      var testType = getTestEnviroment(testTypeString);
      return createTestStep(testName, TestStatus.FAILED, workflowType, testType, repo);
    }
    return null;
  }

  private static TestStep createTestStep(String name, TestStatus status, WorkFlowType workflowType,
                                      TestEnviroment testType, GithubRepo repo) {
    return TestStep.builder()
        .name(name)
        .status(status)
        .type(workflowType)
        .testType(testType)
        .repository(repo)
        .build();
  }

  public static List<TestStep> parseTestSteps(JsonNode testData, GithubRepo repo, WorkFlowType workflowType) {
    List<TestStep> steps = new ArrayList<>();
    var summary = testData.path("output").path("summary").asText();
    var lines = summary.split(CommonConstants.NEW_LINE);
    for (var line : lines) {
      line = line.trim();
      var step = parseTestStepLine(line, repo, workflowType);
      if (step != null) {
        steps.add(step);
      }
    }
    return steps;
  }
}