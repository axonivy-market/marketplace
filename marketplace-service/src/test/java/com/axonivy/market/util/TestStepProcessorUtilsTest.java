package com.axonivy.market.util;

import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestEnviroment;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestStepProcessorUtilsTest {

  @Test
  void testParseTestStepLinePassedMock() {
    String line = "✅ test1 Mock Server Test";
    TestStep step = TestStepProcessorUtils.parseTestSteps(mockJson(line), WorkFlowType.CI).get(0);
    assertEquals("test1", step.getName());
    assertEquals(TestStatus.PASSED, step.getStatus());
    assertEquals(WorkFlowType.CI, step.getType());
    assertEquals(TestEnviroment.MOCK, step.getTestType());
  }

  @Test
  void testParseTestStepLinePassedReal() {
    String line = "✅ test2 Real Server Test";
    TestStep step = TestStepProcessorUtils.parseTestSteps(mockJson(line), WorkFlowType.DEV).get(0);
    assertEquals("test2", step.getName());
    assertEquals(TestStatus.PASSED, step.getStatus());
    assertEquals(WorkFlowType.DEV, step.getType());
    assertEquals(TestEnviroment.REAL, step.getTestType());
  }

  @Test
  void testParseTestStepLineFailedMock() {
    String line = "❌ test3 Mock Server Test";
    TestStep step = TestStepProcessorUtils.parseTestSteps(mockJson(line), WorkFlowType.CI).get(0);
    assertEquals("test3", step.getName());
    assertEquals(TestStatus.FAILED, step.getStatus());
    assertEquals(WorkFlowType.CI, step.getType());
    assertEquals(TestEnviroment.MOCK, step.getTestType());
  }

  @Test
  void testParseTestStepLineFailedReal() {
    String line = "❌ test4 Real Server Test";
    TestStep step = TestStepProcessorUtils.parseTestSteps(mockJson(line), WorkFlowType.DEV).get(0);
    assertEquals("test4", step.getName());
    assertEquals(TestStatus.FAILED, step.getStatus());
    assertEquals(WorkFlowType.DEV, step.getType());
    assertEquals(TestEnviroment.REAL, step.getTestType());
  }

  @Test
  void testParseTestStepLinePassedOther() {
    String line = "✅ test5";
    TestStep step = TestStepProcessorUtils.parseTestSteps(mockJson(line), WorkFlowType.CI).get(0);
    assertEquals("test5", step.getName());
    assertEquals(TestStatus.PASSED, step.getStatus());
    assertEquals(WorkFlowType.CI, step.getType());
    assertEquals(TestEnviroment.OTHER, step.getTestType());
  }

  @Test
  void testParseTestStepLineFailedOther() {
    String line = "❌ test6";
    TestStep step = TestStepProcessorUtils.parseTestSteps(mockJson(line), WorkFlowType.DEV).get(0);
    assertEquals("test6", step.getName());
    assertEquals(TestStatus.FAILED, step.getStatus());
    assertEquals(WorkFlowType.DEV, step.getType());
    assertEquals(TestEnviroment.OTHER, step.getTestType());
  }

  @Test
  void testParseTestStepsMultipleLines() {
    String summary = "✅ test1 Mock Server Test\n❌ test2 Real Server Test\n✅ test3\n❌ test4";
    List<TestStep> steps = TestStepProcessorUtils.parseTestSteps(mockJson(summary), WorkFlowType.CI);
    assertEquals(4, steps.size());
    assertEquals("test1", steps.get(0).getName());
    assertEquals(TestStatus.PASSED, steps.get(0).getStatus());
    assertEquals(TestEnviroment.MOCK, steps.get(0).getTestType());
    assertEquals("test2", steps.get(1).getName());
    assertEquals(TestStatus.FAILED, steps.get(1).getStatus());
    assertEquals(TestEnviroment.REAL, steps.get(1).getTestType());
    assertEquals("test3", steps.get(2).getName());
    assertEquals(TestStatus.PASSED, steps.get(2).getStatus());
    assertEquals(TestEnviroment.OTHER, steps.get(2).getTestType());
    assertEquals("test4", steps.get(3).getName());
    assertEquals(TestStatus.FAILED, steps.get(3).getStatus());
    assertEquals(TestEnviroment.OTHER, steps.get(3).getTestType());
  }

  @Test
  void testParseTestStepsEmptySummary() {
    List<TestStep> steps = TestStepProcessorUtils.parseTestSteps(mockJson(""), WorkFlowType.CI);
    assertTrue(steps.isEmpty());
  }

  private JsonNode mockJson(String summary) {
    ObjectMapper mapper = new ObjectMapper();
    String json = String.format("{\"output\":{\"summary\":\"%s\"}}", summary.replace("\n", "\\n"));
    try {
      return mapper.readTree(json);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}