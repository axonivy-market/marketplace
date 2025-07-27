package com.axonivy.market.util;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestResults;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestResultsUtilsTest {

  @Test
  void testProcessTestResultsWithEmptyTestSteps() {
    GithubRepo githubRepo = new GithubRepo();
    githubRepo.setTestSteps(List.of());

    List<TestResults> results = TestResultsUtils.processTestResults(githubRepo);

    assertTrue(results.isEmpty(), "Should return an empty list for no test steps");
  }

  @Test
  void testProcessTestResultsWithNullTestSteps() {
    GithubRepo githubRepo = new GithubRepo();
    githubRepo.setTestSteps(null);

    List<TestResults> results = TestResultsUtils.processTestResults(githubRepo);

    assertTrue(results.isEmpty(), "Should return an empty list for null test steps");
  }
}