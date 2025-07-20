package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.model.TestStepsModel;
import com.axonivy.market.repository.TestStepsRepository;
import com.axonivy.market.assembler.TestStepsModelAssembler;
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
    @Mock
    private TestStepsModelAssembler testStepsModelAssembler;
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
        TestStepsModel model = new TestStepsModel();
        when(testStepsRepository.findByRepoAndWorkflowAndType("repo1", WorkFlowType.CI)).thenReturn(testSteps);
        when(testStepsModelAssembler.toModel(testStep)).thenReturn(model);
        List<TestStepsModel> result = testStepsService.fetchTestReport("repo1", WorkFlowType.CI);
        assertEquals(1, result.size());
        assertEquals(model, result.get(0));
    }

    @Test
    void testFetchTestReportEmpty() {
        when(testStepsRepository.findByRepoAndWorkflowAndType("repo1", WorkFlowType.CI)).thenReturn(Collections.emptyList());
        List<TestStepsModel> result = testStepsService.fetchTestReport("repo1", WorkFlowType.CI);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateTestStepsWithTestData() throws JsonProcessingException {
        GithubRepo repo = new GithubRepo();
        String json = "{\"output\":{\"summary\":\"✅ step1 Mock Server Test\\n❌ step2 Real Server Test\"}}";
        JsonNode testData = new ObjectMapper().readTree(json);
        List<TestStep> steps = testStepsService.createTestSteps(repo, testData, WorkFlowType.CI);
        assertEquals(2, steps.size());
        assertEquals("step1", steps.get(0).getName());
        assertEquals(TestStatus.PASSED, steps.get(0).getStatus());
        assertEquals("step2", steps.get(1).getName());
        assertEquals(TestStatus.FAILED, steps.get(1).getStatus());
    }

    @Test
    void testCreateTestStepsNullTestData() {
        GithubRepo repo = new GithubRepo();
        List<TestStep> steps = testStepsService.createTestSteps(repo, null, WorkFlowType.CI);
        assertTrue(steps.isEmpty());
    }
}