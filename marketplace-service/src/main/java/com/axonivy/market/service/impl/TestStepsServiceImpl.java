package com.axonivy.market.service.impl;

import com.axonivy.market.assembler.TestStepsModelAssembler;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.model.TestStepsModel;
import com.axonivy.market.repository.TestStepsRepository;
import com.axonivy.market.service.TestStepsService;
import com.axonivy.market.util.TestStepUtils;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Log4j2
@AllArgsConstructor
public class TestStepsServiceImpl implements TestStepsService {

    private final TestStepsRepository testStepsRepository;
    private final TestStepsModelAssembler testStepsModelAssembler;

    @Override
    public List<TestStepsModel> fetchTestReport(String repo, WorkFlowType type) {
        List<TestStep> testSteps = testStepsRepository.findByRepoAndWorkflowAndType(repo, type);
        return testSteps.stream()
                .map(testStepsModelAssembler::toModel)
                .toList();
    }

    @Transactional
    public List<TestStep> createTestSteps(JsonNode testData, WorkFlowType workflowType) {
        if (testData != null) {
            return TestStepUtils.parseTestSteps(testData, workflowType);
        }
        return Collections.emptyList();
    }
}