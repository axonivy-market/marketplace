package com.axonivy.market.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GitHubRepoMetaTest {

  @Test
  void testGetIdReturnsRepoURL() {
    GitHubRepoMeta meta = new GitHubRepoMeta();
    meta.setRepoURL("https://github.com/test/repo");

    assertEquals("https://github.com/test/repo", meta.getId(),
        "Expected getId() to return repoURL");
  }

  @Test
  void testSetIdAssignsRepoURL() {
    GitHubRepoMeta meta = new GitHubRepoMeta();
    meta.setId("https://github.com/test/repo");

    assertEquals("https://github.com/test/repo", meta.getRepoURL(),
        "Expected setId() to assign value to repoURL");
  }
}
