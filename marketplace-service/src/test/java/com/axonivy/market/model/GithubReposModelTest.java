package com.axonivy.market.model;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.TestStatus;
import com.axonivy.market.enums.WorkFlowType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GithubReposModelTest {

  @Test
  void testFromWithValidGithubRepo() {
    GithubRepo githubRepo = new GithubRepo();
    githubRepo.setProductId("my-awesome-repo");
    githubRepo.setHtmlUrl("https://github.com/axonivy-market/my-awesome-repo");
    TestStep step1 = new TestStep("Example name 1", TestStatus.PASSED, WorkFlowType.CI);
    TestStep step2 = new TestStep("Example name 2", TestStatus.FAILED, WorkFlowType.CI);
    TestStep step3 = new TestStep("Example name 3", TestStatus.PASSED, WorkFlowType.DEV);
    githubRepo.setTestSteps(List.of(step1, step2, step3));

    WorkflowInformation ciInfo = new WorkflowInformation();
    ciInfo.setWorkflowType(WorkFlowType.CI);
    githubRepo.setWorkflowInformation(List.of(ciInfo));

    githubRepo.setFocused(true);

    GithubReposModel model = GithubReposModel.from(githubRepo);

    assertEquals("my-awesome-repo", model.getProductId(), "Product Id should match");
    assertEquals("https://github.com/axonivy-market/my-awesome-repo", model.getHtmlUrl(), "HTML URL should match");

    List<TestResults> testResults = model.getTestResults();
    assertNotNull(testResults, "Test results should not be null");
    assertFalse(testResults.isEmpty(), "Test results should not be empty");

    assertTrue(model.getFocused(), "Focused flag should map correctly");
    assertEquals(1, model.getWorkflowInformation().size(), "Workflow info should map correctly");
  }
}