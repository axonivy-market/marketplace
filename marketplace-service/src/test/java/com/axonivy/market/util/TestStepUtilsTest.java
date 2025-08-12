package com.axonivy.market.util;

import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestStepUtilsTest {

  @Test
  void testParseTestStepLinePassedMock() {
    String line = "✅ test1 Mock Server Test";
    TestStep step = TestStepUtils.parseTestSteps(mockJson(line), WorkFlowType.CI).get(0);
    assertEquals("test1", step.getName(), "Test step name should match");
    assertEquals(TestStatus.PASSED, step.getStatus(), "Test step status should be PASSED");
    assertEquals(WorkFlowType.CI, step.getType(), "Test step type should be CI");
  }

  @Test
  void testParseTestStepLinePassedReal() {
    String line = "✅ test2 Real Server Test";
    TestStep step = TestStepUtils.parseTestSteps(mockJson(line), WorkFlowType.DEV).get(0);
    assertEquals("test2", step.getName(), "Test step name should match");
    assertEquals(TestStatus.PASSED, step.getStatus(), "Test step status should be PASSED");
    assertEquals(WorkFlowType.DEV, step.getType(), "Test step type should be DEV");
  }

  @Test
  void testParseTestStepLineFailedMock() {
    String line = "❌ test3 Mock Server Test";
    TestStep step = TestStepUtils.parseTestSteps(mockJson(line), WorkFlowType.CI).get(0);
    assertEquals("test3", step.getName(), "Test step name should match");
    assertEquals(TestStatus.FAILED, step.getStatus(), "Test step status should be FAILED");
    assertEquals(WorkFlowType.CI, step.getType(), "Test step type should be CI");
  }

  @Test
  void testParseTestStepLineFailedReal() {
    String line = "❌ test4 Real Server Test";
    TestStep step = TestStepUtils.parseTestSteps(mockJson(line), WorkFlowType.DEV).get(0);
    assertEquals("test4", step.getName(), "Test step name should match");
    assertEquals(TestStatus.FAILED, step.getStatus(), "Test step status should be FAILED");
    assertEquals(WorkFlowType.DEV, step.getType(), "Test step type should be DEV");
  }

  @Test
  void testParseTestStepLinePassedOther() {
    String line = "✅ test5";
    TestStep step = TestStepUtils.parseTestSteps(mockJson(line), WorkFlowType.CI).get(0);
    assertEquals("test5", step.getName(), "Test step name should match");
    assertEquals(TestStatus.PASSED, step.getStatus(), "Test step status should be PASSED");
    assertEquals(WorkFlowType.CI, step.getType(), "Test step type should be CI");
  }

  @Test
  void testParseTestStepLineFailedOther() {
    String line = "❌ test6";
    TestStep step = TestStepUtils.parseTestSteps(mockJson(line), WorkFlowType.DEV).get(0);
    assertEquals("test6", step.getName(), "Test step name should match");
    assertEquals(TestStatus.FAILED, step.getStatus(), "Test step status should be FAILED");
    assertEquals(WorkFlowType.DEV, step.getType(), "Test step type should be DEV");
  }

  @Test
  void testParseTestStepsMultipleLines() {
    String summary = "✅ test1 Mock Server Test\n❌ test2 Real Server Test\n✅ test3\n❌ test4";
    List<TestStep> steps = TestStepUtils.parseTestSteps(mockJson(summary), WorkFlowType.CI);
    assertEquals(4, steps.size(), "There should be 4 test steps parsed");
    assertEquals("test1", steps.get(0).getName(), "Test step 1 name should match");
    assertEquals(TestStatus.PASSED, steps.get(0).getStatus(), "Test step 1 status should be PASSED");
    assertEquals("test2", steps.get(1).getName(), "Test step 2 name should match");
    assertEquals(TestStatus.FAILED, steps.get(1).getStatus(), "Test step 2 status should be FAILED");
    assertEquals("test3", steps.get(2).getName(), "Test step 3 name should match");
    assertEquals(TestStatus.PASSED, steps.get(2).getStatus(), "Test step 3 status should be PASSED");
    assertEquals("test4", steps.get(3).getName(), "Test step 4 name should match");
    assertEquals(TestStatus.FAILED, steps.get(3).getStatus(), "Test step 4 status should be FAILED");
  }

  @Test
  void testParseTestStepsEmptySummary() {
    List<TestStep> steps = TestStepUtils.parseTestSteps(mockJson(""), WorkFlowType.CI);
    assertTrue(steps.isEmpty(), "There should be no test steps parsed for empty summary");
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