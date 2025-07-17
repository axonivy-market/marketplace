package com.axonivy.market.service.impl;

import com.axonivy.market.assembler.TestStepsModelAssembler;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestSteps;
import com.axonivy.market.model.TestStepsModel;
import com.axonivy.market.repository.GithubRepoRepository;
import com.axonivy.market.repository.TestStepsRepository;
import com.axonivy.market.service.TestStepsService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
  @Log4j2
  @AllArgsConstructor
  public class TestStepsServiceImpl implements TestStepsService {

    private final GithubRepoRepository githubRepoRepository;
    private final TestStepsRepository testStepsRepository;
    private final TestStepsModelAssembler testStepsModelAssembler;

    @Override
    public List<TestStepsModel> fetchTestReport(String repo, String type) {
      List<TestSteps> testSteps = testStepsRepository.findByRepoAndWorkflowAndType(repo, type.toUpperCase());
      return testSteps.stream()
          .map(testStepsModelAssembler::toModel)
          .collect(Collectors.toList());
    }

    @Transactional
    public GithubRepo deleteExistingGithubRepoIfExists(String repoName) {
      Optional<GithubRepo> existingRepoOptional = githubRepoRepository.findByName(repoName);
      if (existingRepoOptional.isPresent()) {
        GithubRepo existingRepo = existingRepoOptional.get();
        try {
          List<TestSteps> testSteps = testStepsRepository.findByRepository(existingRepo);
          testStepsRepository.deleteAll(testSteps);
          return existingRepo;
        } catch (Exception e) {
          log.error("Error deleting GitHub repo {}: {}", repoName, e.getMessage());
          throw e;
        }
      }
      return null;
    }

    @Transactional
    public void createTestSteps(GithubRepo repo, JsonNode testData, String workflowType) {
      if (testData != null) {
        List<TestSteps> steps = TestStepProcessorImpl.parseTestSteps(testData, repo, workflowType);
        if (!steps.isEmpty()) {
          testStepsRepository.saveAll(steps);
          testStepsRepository.flush();
        } else {
          log.warn("No test steps found in test data for repo: {}, workflow: {}", repo.getName(), workflowType);
        }
      }
    }

  }