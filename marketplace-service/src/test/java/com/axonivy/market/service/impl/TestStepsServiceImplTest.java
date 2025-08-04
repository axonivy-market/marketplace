package com.axonivy.market.service.impl;

import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.model.TestStepsModel;
import com.axonivy.market.repository.TestStepsRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestStepsServiceImplTest {
    @Mock
    private TestStepsRepository testStepsRepository;
    @InjectMocks
    private TestStepsServiceImpl testStepsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFetchTestReportReturnsModels() {
      TestStep testStep = TestStep.builder().name("step1").status(TestStatus.PASSED).type(WorkFlowType.CI).build();
      List<TestStep> testSteps = List.of(testStep);
      TestStepsModel expectedModel = new TestStepsModel();
      expectedModel.setName("step1");
      expectedModel.setStatus(TestStatus.PASSED);
      expectedModel.setType(WorkFlowType.CI);
      when(testStepsRepository.findByRepoAndWorkflowAndType("repo1", WorkFlowType.CI)).thenReturn(testSteps);
      List<TestStepsModel> result = testStepsService.fetchTestReport("repo1", WorkFlowType.CI);
      assertEquals(1, result.size(),
          "Expected one TestStepsModel in the result");
      assertEquals(expectedModel, result.get(0),
          "Expected the TestStepsModel to match the expected model");
    }

    @Test
    void testFetchTestReportEmpty() {
        when(testStepsRepository.findByRepoAndWorkflowAndType("repo1", WorkFlowType.CI)).thenReturn(Collections.emptyList());
        List<TestStepsModel> result = testStepsService.fetchTestReport("repo1", WorkFlowType.CI);
        assertTrue(result.isEmpty(),
                "Expected an empty list when no test steps are found");
    }

    @Test
    void testCreateTestStepsWithTestData() throws JsonProcessingException {
        String json = "{\"output\":{\"summary\":\"✅ step1 Mock Server Test\\n❌ step2 Real Server Test\"}}";
        JsonNode testData = new ObjectMapper().readTree(json);
        List<TestStep> steps = testStepsService.createTestSteps(testData, WorkFlowType.CI);
        assertEquals(2, steps.size(),
                "Expected two test steps to be created from the test data");
        assertEquals("step1", steps.get(0).getName(),
                "Expected first step name to be 'step1'");
        assertEquals(TestStatus.PASSED, steps.get(0).getStatus(),
                "Expected first step status to be PASSED");
        assertEquals("step2", steps.get(1).getName(),
                "Expected second step name to be 'step2'");
        assertEquals(TestStatus.FAILED, steps.get(1).getStatus(),
                "Expected second step status to be FAILED");
    }

    @Test
    void testCreateTestStepsNullTestData() {
        List<TestStep> steps = testStepsService.createTestSteps(null, WorkFlowType.CI);
        assertTrue(steps.isEmpty(),
                "Expected an empty list when test data is null");
    }
}