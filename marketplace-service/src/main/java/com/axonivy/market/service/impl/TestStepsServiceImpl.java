package com.axonivy.market.service.impl;

import com.axonivy.market.assembler.TestStepsModelAssembler;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.model.TestStepsModel;
import com.axonivy.market.repository.GithubRepoRepository;
import com.axonivy.market.repository.TestStepsRepository;
import com.axonivy.market.service.TestStepsService;
import com.axonivy.market.util.TestStepProcessorUtils;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@Log4j2
@AllArgsConstructor
public class TestStepsServiceImpl implements TestStepsService {

  private final GithubRepoRepository githubRepoRepository;
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
  public void createTestSteps(GithubRepo repo, JsonNode testData, WorkFlowType workflowType) {
    if (testData != null) {
      List<TestStep> steps = TestStepProcessorUtils.parseTestSteps(testData, repo, workflowType);
      if (!steps.isEmpty()) {
        githubRepoRepository.save(repo);
        githubRepoRepository.flush();
        testStepsRepository.saveAll(steps);
        testStepsRepository.flush();
      } else {
        log.warn("No test steps found in test data for repo: {}, workflow: {}", repo.getName(), workflowType);
      }
    }
  }
}