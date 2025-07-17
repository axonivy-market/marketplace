package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestSteps;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.TestType;
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
public class TestStepProcessorImpl {

  private static final List<String> REAL_TEST_KEYWORDS = List.of(
      "webtest", "bpmclient", "integration", "e2e", "selenium", "ui", "functional", "acceptance");


  private static String extractTestClassName(String line) {
    Pattern classPattern = Pattern.compile("TEST-([^/]+\\.xml)");
    Matcher classMatcher = classPattern.matcher(line);
    if (classMatcher.find()) {
      String xmlFileName = classMatcher.group(1);
      return xmlFileName.substring(0, xmlFileName.length() - 4);
    }
    return "";
  }

  private static boolean isRealTest(String testClassName) {
    String lower = testClassName.toLowerCase();
    return REAL_TEST_KEYWORDS.stream().anyMatch(lower::contains);
  }

  public static List<TestSteps> parseTestSteps(JsonNode testData, GithubRepo repo, String workflowType) {
    List<TestSteps> steps = new ArrayList<>();
    String summary = testData.path("output").path("summary").asText();
    String currentTestClass = "";
    Pattern testCasePattern = Pattern.compile("✅\\s+(.+)");
    String[] lines = summary.split("\\n");
    TestType testType =TestType.OTHER;;
    for (String line : lines) {
      line = line.trim();
      Matcher matcher = testCasePattern.matcher(line);

      if (line.contains("TEST-") && line.contains(".xml</a>")) {
        currentTestClass = extractTestClassName(line);
        testType = isRealTest(currentTestClass) ? TestType.REAL : TestType.MOCK;
      }
      if (matcher.find()) {
        TestSteps step = TestSteps.builder()
            .name(matcher.group(1).trim())
            .status(TestStatus.PASSED)
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