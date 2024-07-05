package com.axonivy.market.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.PagedIterable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.service.impl.GHAxonIvyProductRepoServiceImpl;

@ExtendWith(MockitoExtension.class)
class GHAxonIvyProductRepoServiceImplTest {

  private static final String DUMMY_TAG = "v1.0.0";

  @Mock
  PagedIterable<GHTag> listTags;

  @Mock
  GHRepository ghRepository;

  @Mock
  GitHubService gitHubService;

  @InjectMocks
  private GHAxonIvyProductRepoServiceImpl axonivyProductRepoServiceImpl;

  @BeforeEach
  void setup() throws IOException {
    var mockGHOrganization = mock(GHOrganization.class);
    when(mockGHOrganization.getRepository(any())).thenReturn(ghRepository);
    when(gitHubService.getOrganization(any())).thenReturn(mockGHOrganization);
  }

  @Test
  void testAllTagsFromRepoName() throws IOException {
    var mockTag = mock(GHTag.class);
    when(mockTag.getName()).thenReturn(DUMMY_TAG);
    when(listTags.toList()).thenReturn(List.of(mockTag));
    when(ghRepository.listTags()).thenReturn(listTags);
    var result = axonivyProductRepoServiceImpl.getAllTagsFromRepoName("");
    assertEquals(1, result.size());
    assertEquals(DUMMY_TAG, result.get(0).getName());
  }

  @Test
  void testContentFromGHRepoAndTag() {
    var result = axonivyProductRepoServiceImpl.getContentFromGHRepoAndTag("", null, null);
    assertEquals(null, result);
  }
}
