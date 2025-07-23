package com.axonivy.market.model;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestResults;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestEnviroment;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestStepsModelTest {
  @Test
  void testFromGithubRepo() {
    GithubRepo repo = new GithubRepo();
    repo.setName("demo-repo");
    repo.setHtmlUrl("https://github.com/org/demo-repo");
    repo.setLanguage("Java");
    repo.setLastUpdated(Date.from(Instant.parse("2024-01-01T00:00:00Z")));
    repo.setCiBadgeUrl("https://badge/ci");
    repo.setDevBadgeUrl("https://badge/dev");

    TestStep result = new TestStep();
    result.setName("Example name test");
    result.setTestType(TestEnviroment.MOCK);
    result.setStatus(TestStatus.PASSED);
    result.setType(WorkFlowType.CI);
    repo.setTestSteps(List.of(result));

    GithubReposModel model = GithubReposModel.from(repo);

    assertEquals("demo-repo", model.getName(),
            "Expected the name to match the repo name");
    assertEquals("https://github.com/org/demo-repo", model.getHtmlUrl(),
            "Expected the HTML URL to match the repo URL");
    assertEquals("Java", model.getLanguage(),
            "Expected the language to match the repo language");
    assertEquals("Mon Jan 01 07:00:00 ICT 2024", model.getLastUpdated(),
            "Expected the last updated date to match the repo's last updated date");
    assertEquals("https://badge/ci", model.getCiBadgeUrl(),
            "Expected the CI badge URL to match the repo's CI badge URL");
    assertEquals("https://badge/dev", model.getDevBadgeUrl(),
            "Expected the DEV badge URL to match the repo's DEV badge URL");

    assertNotNull(model.getTestResults(),
            "Expected test results to be initialized");
    assertEquals(2, model.getTestResults().size(),
            "Expected two test results for CI and DEV workflows");

    TestResults testResult = model.getTestResults().get(0);
    assertEquals(WorkFlowType.CI, testResult.getWorkflow(),
            "Expected the workflow type to be CI");
    assertEquals("MOCK", testResult.getEnvironment(),
            "Expected the environment to be MOCK");
    assertEquals(TestStatus.PASSED, testResult.getStatus(),
            "Expected the test status to be PASSED");
    assertEquals(1, testResult.getCount(),
            "Expected the count of test steps to be 1");
  }
}