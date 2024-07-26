package com.axonivy.market.service;

import com.axonivy.market.github.service.impl.GitHubServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


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

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    lenient().when(restTemplateBuilder.build()).thenReturn(restTemplate);
  }

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
