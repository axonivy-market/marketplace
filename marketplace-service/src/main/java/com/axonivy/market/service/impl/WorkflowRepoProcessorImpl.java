package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.WorkflowRepo;
import com.axonivy.market.service.WorkflowRepoProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WorkflowRepoProcessorImpl implements WorkflowRepoProcessor {
  private static final Pattern TITLE_STATS_PATTERN = Pattern.compile(
      "(\\d+)\\s+passed,\\s+(\\d+)\\s+failed\\s+and\\s+(\\d+)\\s+skipped");

  private static final Pattern REPORT_STATS_PATTERN = Pattern.compile(
      "\\|(\\d+)✅\\|\\|\\|");

  private static final List<String> REAL_TEST_KEYWORDS = List.of(
      "webtest", "bpmclient", "integration", "e2e", "selenium", "ui", "functional", "acceptance");

  public WorkflowRepo createWorkflowRepo(JsonNode testData, GithubRepo repo, String workflowType) {
    WorkflowRepo workflow = new WorkflowRepo();
    workflow.setId(UUID.randomUUID().toString());
    workflow.setRepository(repo);
    workflow.setType(workflowType);

    calculateAndSetStats(workflow, testData);
    return workflow;
  }

  private void calculateAndSetStats(WorkflowRepo workflow, JsonNode testData) {
    String title = testData.path("output").path("title").asText();
    String summary = testData.path("output").path("summary").asText();

    Matcher titleMatcher = TITLE_STATS_PATTERN.matcher(title);
    if (titleMatcher.find()) {
      int totalPassed = Integer.parseInt(titleMatcher.group(1));
      int totalFailed = Integer.parseInt(titleMatcher.group(2));
      int totalSkipped = Integer.parseInt(titleMatcher.group(3));

      workflow.setPassed(totalPassed);
      workflow.setFailed(totalFailed);
    }
    int mockPassed = 0;
    int mockFailed = 0;
    int realPassed = 0;
    int realFailed = 0;

    String[] lines = summary.split("\\n");
    String currentTestClass = "";

    for (String line : lines) {
      line = line.trim();

       if (line.contains("TEST-") && line.contains(".xml</a>")) {
        currentTestClass = extractTestClassName(line);
        continue;
      }

      Matcher reportMatcher = REPORT_STATS_PATTERN.matcher(line);
      if (reportMatcher.find()) {
        int passed = Integer.parseInt(reportMatcher.group(1));
        int failed = 0;

        if (isRealTest(currentTestClass)) {
          realPassed += passed;
          realFailed += failed;
        } else {
          mockPassed += passed;
          mockFailed += failed;
        }
      }
    }

    workflow.setMockPassed(mockPassed);
    workflow.setMockFailed(mockFailed);
    workflow.setRealPassed(realPassed);
    workflow.setRealFailed(realFailed);
  }

  private String extractTestClassName(String line) {
    Pattern classPattern = Pattern.compile("TEST-([^/]+\\.xml)");
    Matcher classMatcher = classPattern.matcher(line);
    if (classMatcher.find()) {
      String xmlFileName = classMatcher.group(1);
      return xmlFileName.substring(0, xmlFileName.length() - 4);
    }
    return "";
  }

  private boolean isRealTest(String testClassName) {
    String lower = testClassName.toLowerCase();
    return REAL_TEST_KEYWORDS.stream().anyMatch(lower::contains);
  }
}