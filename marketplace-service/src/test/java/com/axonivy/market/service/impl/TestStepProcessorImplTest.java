package com.axonivy.market.service.impl;

import com.axonivy.market.entity.TestSteps;
import com.axonivy.market.entity.WorkflowRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestStepProcessorImplTest {

  private final TestStepProcessorImpl processor = new TestStepProcessorImpl();
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void parseTestStepsEmptySummaryReturnsEmptyList() {
    JsonNode testData = mapper.createObjectNode()
        .putObject("output")
        .put("summary", "");
    WorkflowRepo workflow = new WorkflowRepo();

    List<TestSteps> steps = processor.parseTestSteps(testData, workflow);

    assertTrue(steps.isEmpty());
  }

  @Test
  void parseTestStepsNoMatchReturnsEmptyList() {
    String summary = "❌ Failed case\nSome other text";
    JsonNode testData = mapper.createObjectNode()
        .putObject("output")
        .put("summary", summary);
    WorkflowRepo workflow = new WorkflowRepo();

    List<TestSteps> steps = processor.parseTestSteps(testData, workflow);

    assertTrue(steps.isEmpty());
  }

  @Test
  void parseTestStepsMissingOutputReturnsEmptyList() {
    JsonNode testData = mapper.createObjectNode(); // no "output"
    WorkflowRepo workflow = new WorkflowRepo();

    List<TestSteps> steps = processor.parseTestSteps(testData, workflow);

    assertTrue(steps.isEmpty());
  }

  @Test
  void parseTestStepsMissingSummaryReturnsEmptyList() {
    JsonNode testData = mapper.createObjectNode()
        .putObject("output"); // no "summary"
    WorkflowRepo workflow = new WorkflowRepo();

    List<TestSteps> steps = processor.parseTestSteps(testData, workflow);

    assertTrue(steps.isEmpty());
  }

  @Test
  void parseTestStepsWhitespaceSummaryReturnsEmptyList() {
    JsonNode testData = mapper.createObjectNode()
        .putObject("output")
        .put("summary", "   \n   ");
    WorkflowRepo workflow = new WorkflowRepo();

    List<TestSteps> steps = processor.parseTestSteps(testData, workflow);

    assertTrue(steps.isEmpty());
  }

}