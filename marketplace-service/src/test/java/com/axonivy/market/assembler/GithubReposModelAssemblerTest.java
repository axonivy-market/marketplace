package com.axonivy.market.assembler;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.model.GithubReposModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GithubReposModelAssemblerTest {

  @Test
  void testToModelWithValidGithubRepo() {
    GithubRepo githubRepo = new GithubRepo();
    githubRepo.setName("test-repo");
    githubRepo.setHtmlUrl("https://github.com/test/test-repo");
    githubRepo.setLanguage("Java");
    githubRepo.setLastUpdated(java.sql.Timestamp.valueOf("2025-07-23 10:00:00"));
    githubRepo.setCiBadgeUrl("https://github.com/actions/workflows/ci.yml/badge.svg");
    githubRepo.setDevBadgeUrl("https://github.com/actions/workflows/dev.yml/badge.svg");

    GithubReposModelAssembler assembler = new GithubReposModelAssembler();
    GithubReposModel model = assembler.toModel(githubRepo);

    assertNotNull(model, "Model should not be null");
    assertEquals("test-repo", model.getName(), "Repository name should match");
    assertEquals("https://github.com/test/test-repo", model.getHtmlUrl(), "HTML URL should match");
    assertEquals("Java", model.getLanguage(), "Language should match");
    assertEquals("2025-07-23 10:00:00.0", model.getLastUpdated(), "Last updated timestamp should match");
    assertEquals("https://github.com/actions/workflows/ci.yml/badge.svg", model.getCiBadgeUrl(), "CI badge URL should match");
    assertEquals("https://github.com/actions/workflows/dev.yml/badge.svg", model.getDevBadgeUrl(), "DEV badge URL should match");
  }
}
