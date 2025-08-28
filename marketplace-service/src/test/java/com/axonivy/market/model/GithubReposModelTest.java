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
    githubRepo.setName("my-awesome-repo");
    githubRepo.setHtmlUrl("https://github.com/axonivy-market/my-awesome-repo");
    githubRepo.setCiLastBuilt(java.sql.Timestamp.valueOf("2025-07-14 10:35:00"));
    githubRepo.setDevLastBuilt(java.sql.Timestamp.valueOf("2025-07-14 10:35:00"));
    githubRepo.setE2eLastBuilt(java.sql.Timestamp.valueOf("2025-07-14 10:35:00"));
    TestStep step1 = new TestStep("Example name 1", TestStatus.PASSED, WorkFlowType.CI);
    TestStep step2 = new TestStep("Example name 2", TestStatus.FAILED, WorkFlowType.CI);
    TestStep step3 = new TestStep("Example name 3", TestStatus.PASSED, WorkFlowType.DEV);
    githubRepo.setTestSteps(List.of(step1, step2, step3));

    GithubReposModel model = GithubReposModel.from(githubRepo);

    assertEquals("my-awesome-repo", model.getName(), "Repository name should match");
    assertEquals("https://github.com/axonivy-market/my-awesome-repo", model.getHtmlUrl(), "HTML URL should match");
    assertEquals("Java", model.getLanguage(), "Language should match");
    assertEquals(java.sql.Timestamp.valueOf("2025-07-14 10:35:00"), model.getCiLastBuilt(),
        "Last updated timestamp should match");
    assertEquals(java.sql.Timestamp.valueOf("2025-07-14 10:35:00"), model.getDevLastBuilt(),
        "Last updated timestamp should match");
    assertEquals(java.sql.Timestamp.valueOf("2025-07-14 10:35:00"), model.getE2eLastBuilt(),
        "Last updated timestamp should match");

    List<TestResults> testResults = model.getTestResults();
    assertNotNull(testResults, "Test results should not be null");
    assertFalse(testResults.isEmpty(), "Test results should not be empty");
  }
}