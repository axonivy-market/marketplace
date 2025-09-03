package com.axonivy.market.model;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestStepsModelTest {
  @Test
  void testFromGithubRepo() {
    GithubRepo repo = new GithubRepo();
    repo.setName("demo-repo");
    repo.setHtmlUrl("https://github.com/org/demo-repo");

    TestStep result = new TestStep();
    result.setName("Example name test");
    result.setStatus(TestStatus.PASSED);
    result.setType(WorkFlowType.CI);
    repo.setTestSteps(List.of(result));

    GithubReposModel model = GithubReposModel.from(repo);

    assertEquals("demo-repo", model.getName(),
            "Expected the name to match the repo name");
    assertEquals("https://github.com/org/demo-repo", model.getHtmlUrl(),
        "Expected the HTML URL to match the repo URL");

    assertNotNull(model.getTestResults(),
            "Expected test results to be initialized");
    assertEquals(2, model.getTestResults().size(),
            "Expected two test results for CI and DEV workflows");

    TestResults testResult = model.getTestResults().get(0);
    assertEquals(WorkFlowType.CI, testResult.getWorkflow(),
            "Expected the workflow type to be CI");
    assertEquals(1, testResult.getResults().get("PASSED"),
        "Expected the count of test steps to be 1");
  }
}