package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.WorkflowRepo;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WorkflowRepoProcessorImpl {

  public WorkflowRepo createWorkflowRepo(JsonNode testData, GithubRepo repo, String workflowType) {
    WorkflowRepo workflow = new WorkflowRepo();
    workflow.setId(UUID.randomUUID().toString());
    workflow.setRepository(repo);
    workflow.setType(workflowType);

    calculateAndSetStats(workflow, testData);
    return workflow;
  }

  public void updateWorkflowStats(WorkflowRepo workflow, JsonNode testData) {
    calculateAndSetStats(workflow, testData);
  }

  private void calculateAndSetStats(WorkflowRepo workflow, JsonNode testData) {
    String summary = testData.path("output").path("summary").asText();

    int totalPassed = 0;
    int totalFailed = 0;
    int mockPassed = 0;
    int mockFailed = 0;
    int realPassed = 0;
    int realFailed = 0;

    Pattern statsPattern = Pattern.compile(
        "\\*\\*(\\d+)\\*\\* tests were completed in \\*\\*\\d+s\\*\\* with \\*\\*(\\d+)\\*\\* passed, \\*\\*(\\d+)\\*\\* failed");

    Pattern classPattern = Pattern.compile("TEST-([^/]+\\.xml)");

    String[] lines = summary.split("\\n");
    String currentTestClass = "";

    for (String line : lines) {
      line = line.trim();

      if (line.contains("TEST-") && line.endsWith(".xml</a>")) {
        Matcher classMatcher = classPattern.matcher(line);
        if (classMatcher.find()) {
          String xmlFileName = classMatcher.group(1);
          currentTestClass = xmlFileName.substring(5, xmlFileName.length() - 4);
        }
        continue;
      }

      Matcher statsMatcher = statsPattern.matcher(line);
      if (statsMatcher.find()) {
        int passed = Integer.parseInt(statsMatcher.group(2));
        int failed = Integer.parseInt(statsMatcher.group(3));

        totalPassed += passed;
        totalFailed += failed;

        if (isRealTest(currentTestClass)) {
          realPassed += passed;
          realFailed += failed;
        } else {
          mockPassed += passed;
          mockFailed += failed;
        }
      }
    }

    workflow.setPassed(totalPassed);
    workflow.setFailed(totalFailed);
    workflow.setMockPassed(mockPassed);
    workflow.setMockFailed(mockFailed);
    workflow.setRealPassed(realPassed);
    workflow.setRealFailed(realFailed);
  }

  private boolean isRealTest(String testClassName) {
    if (testClassName == null || testClassName.isEmpty()) {
      return false;
    }

    String lower = testClassName.toLowerCase();

    if (lower.contains("webtest") ||
        lower.contains("bpmclient") ||
        lower.contains("integration") ||
        lower.contains("e2e") ||
        lower.contains("selenium") ||
        lower.contains("ui") ||
        lower.contains("functional") ||
        lower.contains("acceptance")) {
      return true;
    }

    if (lower.contains("mock") ||
        lower.contains("unit") ||
        lower.contains("stub") ||
        lower.contains("fake") ||
        lower.contains("test") && !lower.contains("webtest")) {
      return false;
    }

    return false;
  }
}