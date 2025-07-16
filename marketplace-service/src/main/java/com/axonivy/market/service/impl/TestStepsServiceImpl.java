package com.axonivy.market.service.impl;

import com.axonivy.market.assembler.TestStepsModelAssembler;
import com.axonivy.market.entity.TestSteps;
import com.axonivy.market.entity.WorkflowRepo;
import com.axonivy.market.model.TestStepsModel;
import com.axonivy.market.repository.TestStepsRepository;
import com.axonivy.market.service.TestStepsService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
@Log4j2
@RequiredArgsConstructor
public class TestStepsServiceImpl implements TestStepsService {

  private final TestStepsRepository testStepsRepository;
  private final TestStepsModelAssembler testStepsModelAssembler;
  private final TestStepProcessorImpl stepProcessor;

  public List<TestStepsModel> fetchTestReport(String repo, String type) {
    List<TestSteps> testSteps = testStepsRepository.findByRepoAndWorkflowAndType(repo, type.toUpperCase());
    return testSteps.stream()
        .map(testStepsModelAssembler::toModel)
        .collect(Collectors.toList());
  }

  @Transactional
  public void createNewTestSteps(WorkflowRepo workflow, JsonNode testData) {
    if (testData != null) {
      List<TestSteps> newSteps = stepProcessor.parseTestSteps(testData, workflow);
      if (newSteps != null && !newSteps.isEmpty()) {
        List<TestSteps> savedSteps = testStepsRepository.saveAll(newSteps);
        workflow.setTestSteps(savedSteps);
      }
    }
  }
}