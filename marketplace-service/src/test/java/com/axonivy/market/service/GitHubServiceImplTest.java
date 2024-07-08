package com.axonivy.market.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import com.axonivy.market.github.service.impl.GitHubServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GitHubServiceImplTest {
  private static final String DUMMY_API_URL = "https://api.github.com";

  @Mock
  GitHub gitHub;

  @Mock
  GHRepository ghRepository;

  @InjectMocks
  private GitHubServiceImpl gitHubService;

  @Test
  void testGetGithub() throws IOException {
    var result = gitHubService.getGitHub();
    assertEquals(DUMMY_API_URL, result.getApiUrl());
  }

  @Test
  void testGetGithubContent() throws IOException {
    var mockGHContent = mock(GHContent.class);
    final String dummryURL = DUMMY_API_URL.concat("/dummry-content");
    when(mockGHContent.getUrl()).thenReturn(dummryURL);
    when(ghRepository.getFileContent(any(), any())).thenReturn(mockGHContent);
    var result = gitHubService.getGHContent(ghRepository, "", "");
    assertEquals(dummryURL, result.getUrl());
  }

  @Test
  void testGetDirectoryContent() throws IOException {
    var result = gitHubService.getDirectoryContent(ghRepository, "", "");
    assertEquals(0, result.size());
  }

}
