package com.axonivy.market.model;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.TestResults;
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
    githubRepo.setLanguage("Java");
    githubRepo.setLastUpdated(java.sql.Timestamp.valueOf("2025-07-14 10:35:00"));
    githubRepo.setCiBadgeUrl("https://github.com/actions/workflows/ci.yml/badge.svg");
    githubRepo.setDevBadgeUrl("https://github.com/actions/workflows/dev.yml/badge.svg");

    GithubReposModel model = GithubReposModel.from(githubRepo);

    assertEquals("my-awesome-repo", model.getName(), "Repository name should match");
    assertEquals("https://github.com/axonivy-market/my-awesome-repo", model.getHtmlUrl(), "HTML URL should match");
    assertEquals("Java", model.getLanguage(), "Language should match");
    assertEquals("2025-07-14 10:35:00.0", model.getLastUpdated(), "Last updated timestamp should match");
    assertEquals("https://github.com/actions/workflows/ci.yml/badge.svg", model.getCiBadgeUrl(), "CI badge URL should match");
    assertEquals("https://github.com/actions/workflows/dev.yml/badge.svg", model.getDevBadgeUrl(), "DEV badge URL should match");

    List<TestResults> testResults = model.getTestResults();
    assertNotNull(testResults, "Test results should not be null");
    assertFalse(testResults.isEmpty(), "Test results should not be empty");
  }
}