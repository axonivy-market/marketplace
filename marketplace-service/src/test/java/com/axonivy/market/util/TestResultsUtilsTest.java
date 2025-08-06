package com.axonivy.market.util;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.model.TestResults;
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

  @Test
  void testProcessTestResultsWithE2ETestSteps() {
    GithubRepo githubRepo = new GithubRepo();
    var testStep = new TestStep();
    testStep.setType(WorkFlowType.E2E);
    testStep.setStatus(TestStatus.PASSED);
    testStep.setName("test");
    githubRepo.setTestSteps(List.of(testStep));

    List<TestResults> results = TestResultsUtils.processTestResults(githubRepo);

    assertFalse(results.isEmpty(), "Should not return an empty list for github repo exist test steps");
    assertEquals(1, results.get(0).getResults().get("PASSED"), "passed count should be 1");
  }
}