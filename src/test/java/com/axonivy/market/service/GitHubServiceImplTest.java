package com.axonivy.market.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axonivy.market.github.service.impl.GithubServiceImpl;

@ExtendWith(MockitoExtension.class)
class GitHubServiceImplTest {
  private static final String DUMMY_API_URL = "https://api.github.com";

  @Mock
  GitHub gitHub;

  @Mock
  GHRepository ghRepository;

  @InjectMocks
  private GithubServiceImpl githubService;

  @Test
  void testGetGithub() throws IOException {
    var result = githubService.getGithub();
    assertEquals(DUMMY_API_URL, result.getApiUrl());
  }

  @Test
  void testGetGithubContent() throws IOException {
    var mockGHContent = mock(GHContent.class);
    final String dummryURL = DUMMY_API_URL.concat("/dummry-content");
    when(mockGHContent.getUrl()).thenReturn(dummryURL);
    when(ghRepository.getFileContent(any())).thenReturn(mockGHContent);
    var result = githubService.getGHContent(ghRepository, "");
    assertEquals(dummryURL, result.getUrl());
  }

  @Test
  void testGetDirectoryContent() throws IOException {
    var result = githubService.getDirectoryContent(ghRepository, "");
    assertEquals(0, result.size());
  }

}
