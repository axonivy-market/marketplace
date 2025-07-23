package com.axonivy.market.util;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestResults;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.axonivy.market.enums.TestEnviroment.*;
import static org.junit.jupiter.api.Assertions.*;

class TestResultsUtilsTest {

  @Test
  void testProcessTestResults_withValidData() {
    GithubRepo githubRepo = new GithubRepo();
    TestStep step1 = new TestStep("Example name 1", TestStatus.PASSED,WorkFlowType.CI,MOCK);
    TestStep step2 = new TestStep("Example name 2", TestStatus.FAILED,WorkFlowType.CI, REAL);
    TestStep step3 = new TestStep("Example name 3", TestStatus.PASSED,WorkFlowType.DEV, OTHER);
    githubRepo.setTestSteps(List.of(step1, step2, step3));

    List<TestResults> results = TestResultsUtils.processTestResults(githubRepo);

    assertEquals(6, results.size(), "Should return 6 test results");
    assertTrue(results.stream().anyMatch(r -> r.getEnvironment().equals("MOCK") && r.getCount() == 1), "Should " +
        "include MOCK environment");
    assertTrue(results.stream().anyMatch(r -> r.getEnvironment().equals("REAL") && r.getCount() == 1), "Should include REAL environment");
  }

  @Test
  void testProcessTestResults_withSkippedSteps() {
    GithubRepo githubRepo = new GithubRepo();
    TestStep step1 = new TestStep("Example name 1", TestStatus.SKIPPED,WorkFlowType.CI,MOCK);
    TestStep step2 = new TestStep("Example name 2", TestStatus.PASSED,WorkFlowType.CI,REAL);
    githubRepo.setTestSteps(List.of(step1, step2));

    List<TestResults> results = TestResultsUtils.processTestResults(githubRepo);

    assertEquals(2, results.size(), "Should return 2 test results");
    assertTrue(results.stream().anyMatch(r -> r.getEnvironment().equals("REAL") && r.getCount() == 1), "Should include REAL environment");
    assertTrue(results.stream().anyMatch(r -> r.getEnvironment().equals("ALL") && r.getCount() == 1), "Should include ALL environment");
  }

  @Test
  void testProcessTestResults_withEmptyTestSteps() {
    GithubRepo githubRepo = new GithubRepo();
    githubRepo.setTestSteps(List.of());

    List<TestResults> results = TestResultsUtils.processTestResults(githubRepo);

    assertTrue(results.isEmpty(), "Should return an empty list for no test steps");
  }

  @Test
  void testProcessTestResults_withNullTestSteps() {
    GithubRepo githubRepo = new GithubRepo();
    githubRepo.setTestSteps(null);

    List<TestResults> results = TestResultsUtils.processTestResults(githubRepo);

    assertTrue(results.isEmpty(), "Should return an empty list for null test steps");
  }
}