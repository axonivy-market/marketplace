package com.axonivy.market.service.impl;

import com.axonivy.market.github.service.impl.GitHubServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class GitHubServiceImplTest {
  private static final String DUMMY_API_URL = "https://api.github.com";

  @Mock
  GHRepository ghRepository;

  @Mock
  private RestTemplateBuilder restTemplateBuilder;

  @Mock
  private RestTemplate restTemplate;

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
    final String dummyURL = DUMMY_API_URL.concat("/dummy-content");
    when(mockGHContent.getUrl()).thenReturn(dummyURL);
    when(ghRepository.getFileContent(any(), any())).thenReturn(mockGHContent);
    var result = gitHubService.getGHContent(ghRepository, "", "");
    assertEquals(dummyURL, result.getUrl());
  }

  @Test
  void testGetDirectoryContent() throws IOException {
    var result = gitHubService.getDirectoryContent(ghRepository, "", "");
    assertEquals(0, result.size());
  }

  @Test
  void testGithubWithToken() throws IOException {
    var result = gitHubService.getGitHub("accessToken");
    assertEquals(DUMMY_API_URL, result.getApiUrl());
  }
}
