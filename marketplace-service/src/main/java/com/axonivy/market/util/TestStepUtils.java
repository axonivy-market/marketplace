package com.axonivy.market.util;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.GHRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.axonivy.market.entity.TestStep.createTestStep;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestStepUtils {

  private static final String BADGE_URL = "https://github.com/%s/actions/workflows/%s/badge.svg";
  private static final String PATTERN_TEST_CASE = "^✅\\s+([^\\s].*?)(Real Server Test|Mock Server Test)?$";
  private static final String PATTERN_TEST_CASE_FAILED = "^❌\\s+([^\\s].*?)(Real Server Test|Mock Server Test)?$";
  private static final int TEST_TYPE_INDEX = 2;
  private static final int TEST_NAME_INDEX = 1;
  private static final Pattern TEST_CASE_PATTERN = Pattern.compile(PATTERN_TEST_CASE,
      Pattern.UNICODE_CHARACTER_CLASS);
  private static final Pattern TEST_CASE_FAILED_PATTERN = Pattern.compile(PATTERN_TEST_CASE_FAILED,
      Pattern.UNICODE_CHARACTER_CLASS);

  private static TestStep parseTestStepLine(String line, WorkFlowType workflowType) {
    var matcher = TEST_CASE_PATTERN.matcher(line);
    var matcherFailed = TEST_CASE_FAILED_PATTERN.matcher(line);
    if (matcher.find()) {
      String testName = matcher.group(TEST_NAME_INDEX).trim();
      var testTypeString = matcher.group(TEST_TYPE_INDEX);
      return createTestStep(testName, TestStatus.PASSED, workflowType);
    } else if (matcherFailed.find()) {
      String testName = matcherFailed.group(TEST_NAME_INDEX).trim();
      var testTypeString = matcherFailed.group(TEST_TYPE_INDEX);
      return createTestStep(testName, TestStatus.FAILED, workflowType);
    }
    return null;
  }


  public static List<TestStep> parseTestSteps(JsonNode testData, WorkFlowType workflowType) {
    List<TestStep> steps = new ArrayList<>();
    var summary = testData.path("output").path("summary").asText();
    var lines = summary.split(CommonConstants.NEW_LINE);
    for (var line : lines) {
      line = line.trim();
      var step = parseTestStepLine(line, workflowType);
      if (step != null) {
        steps.add(step);
      }
    }
    return steps;
  }

  public static String buildBadgeUrl(GHRepository repo, String workflowFileName) {
    return String.format(BADGE_URL, repo.getFullName(), workflowFileName);
  }
}