package com.axonivy.market.service.impl;

import com.axonivy.market.entity.TestSteps;
import com.axonivy.market.entity.WorkflowRepo;
import com.axonivy.market.enums.TestStatus;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Service
public class TestStepProcessorImpl {

  public List<TestSteps> parseTestSteps(JsonNode testData, WorkflowRepo workflow) {
    List<TestSteps> steps = new ArrayList<>();
    String summary = testData.path("output").path("summary").asText();
    Pattern stepPattern = Pattern.compile("✅\\s+(.*?)\\(.*?\\)");
    Matcher matcher = stepPattern.matcher(summary);

    while (matcher.find()) {
      TestSteps step = new TestSteps();
      step.setId(UUID.randomUUID().toString());
      step.setName(matcher.group(1).trim());
      step.setStatus(TestStatus.PASSED);
      step.setWorkflow(workflow);
      steps.add(step);
    }
    return steps;
  }
}