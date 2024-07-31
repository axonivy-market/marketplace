package com.axonivy.market.service;

import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.service.impl.GHAxonIvyMarketRepoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHCommit.File;
import org.kohsuke.github.GHCompare;
import org.kohsuke.github.GHCompare.Commit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.PagedIterable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GHAxonIvyMarketRepoServiceImplTest {

  @Mock
  GHOrganization ghOrganization;

  @Mock
  GHRepository ghRepository;

  @Mock
  PagedIterable<GHContent> pagedGHContent;

  @Mock
  PagedIterable<Commit> pagedCommit;

  @Mock
  PagedIterable<File> pagedFile;

  @Mock
  GitHubService gitHubService;

  @InjectMocks
  GHAxonIvyMarketRepoServiceImpl axonIvyMarketRepoServiceImpl;

  @BeforeEach
  void setup() throws IOException {
    when(ghOrganization.getRepository(any())).thenReturn(ghRepository);
    when(gitHubService.getOrganization(anyString())).thenReturn(ghOrganization);
  }

  @Test
  void testFetchAllMarketItems() throws IOException {
    // Empty due to missing token
    var ghContentMap = axonIvyMarketRepoServiceImpl.fetchAllMarketItems();
    assertEquals(0, ghContentMap.values().size());

    // Has one record from Github-repo
    var mockGHFileContent = mock(GHContent.class);
    var mockGHContent = mock(GHContent.class);
    when(mockGHContent.isDirectory()).thenReturn(true);
    when(mockGHContent.listDirectoryContent()).thenReturn(pagedGHContent);
    List<GHContent> mockGhContents = new ArrayList<>();
    mockGhContents.add(mockGHContent);
    when(mockGHFileContent.isFile()).thenReturn(true);
    when(pagedGHContent.toList()).thenReturn(List.of(mockGHFileContent));
    when(gitHubService.getDirectoryContent(any(), any(), any())).thenReturn(mockGhContents);

    ghContentMap = axonIvyMarketRepoServiceImpl.fetchAllMarketItems();
    assertEquals(1, ghContentMap.values().size());
  }

  @Test
  void testFetchMarketItemsBySHA1Range() throws IOException {
    final String startSHA1 = "2f415c725b049655c6c100448b8aeed59514023b";
    final String endSHA1 = "c57259288e208feea7e18fdb2fd483081bb69fb4";
    final String fileName = "test-meta.json";

    var mockCommit = mock(Commit.class);
    var mockGHCompare = mock(GHCompare.class);
    when(mockGHCompare.listCommits()).thenReturn(pagedCommit);
    when(pagedCommit.toList()).thenReturn(List.of(mockCommit));
    when(ghRepository.getCompare(anyString(), anyString())).thenReturn(mockGHCompare);

    var gitHubFiles = axonIvyMarketRepoServiceImpl.fetchMarketItemsBySHA1Range(startSHA1, endSHA1);
    assertEquals(0, gitHubFiles.size());

    when(mockCommit.listFiles()).thenReturn(pagedFile);
    var mockFile = mock(File.class);
    when(mockFile.getFileName()).thenReturn(fileName);
    when(mockFile.getRawUrl()).thenReturn(new URL("http://github/test-repo-url/test-meta.json"));
    when(mockFile.getStatus()).thenReturn("added");
    when(mockFile.getPreviousFilename()).thenReturn("test-prev-meta.json");
    when(pagedFile.toList()).thenReturn(List.of(mockFile));

    gitHubFiles = axonIvyMarketRepoServiceImpl.fetchMarketItemsBySHA1Range(startSHA1, endSHA1);
    assertEquals(1, gitHubFiles.size());
    assertEquals(fileName, gitHubFiles.get(0).getFileName());
  }

  @Test
  void testGetLastCommit() throws IOException {
    var lastCommit = axonIvyMarketRepoServiceImpl.getLastCommit(0L);
    assertNull(lastCommit);
  }
}
