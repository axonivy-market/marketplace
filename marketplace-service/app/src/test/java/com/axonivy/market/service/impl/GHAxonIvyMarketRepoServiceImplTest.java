package com.axonivy.market.service.impl;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.service.impl.GHAxonIvyMarketRepoServiceImpl;
import com.axonivy.market.service.AppSettingService;
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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
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

  @Mock
  AppSettingService appSettingService;

  @InjectMocks
  GHAxonIvyMarketRepoServiceImpl axonIvyMarketRepoServiceImpl;

  @BeforeEach
  void setup() throws IOException {
    when(ghOrganization.getRepository(any())).thenReturn(ghRepository);
    when(gitHubService.getOrganization(anyString())).thenReturn(ghOrganization);
    lenient().when(appSettingService.getStringValueByKey(AppSettingKey.GITHUB_MARKET_BRANCH)).thenReturn("master");
  }

  @Test
  void testFetchAllMarketItems() throws IOException {
    // Empty due to missing token
    var ghContentMap = axonIvyMarketRepoServiceImpl.fetchAllMarketItems();
    assertEquals(0, ghContentMap.values().size(), "Expected no market items when token is missing");

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
    assertEquals(1, ghContentMap.values().size(), "Expected one market item when GitHub repo returns one record");
  }

  @Test
  void testFetchMarketItemsBySHA1Range() throws IOException {
    final String startSHA1 = "2f415c725b049655c6c100448b8aeed59514023b";
    final String endSHA1 = "c57259288e208feea7e18fdb2fd483081bb69fb4";
    final String fileName = "market/test-meta.json";
    final String fileName2 = "market-test/test-meta.json";
    final String logoFileName = "market/logo.png";
    final String logoDarkFileName = "market/logo-dark.png";
    var mockCommit = mock(Commit.class);
    var mockGHCompare = mock(GHCompare.class);
    when(mockGHCompare.listCommits()).thenReturn(pagedCommit);
    when(pagedCommit.toList()).thenReturn(List.of(mockCommit));
    when(ghRepository.getCompare(anyString(), anyString())).thenReturn(mockGHCompare);

    var gitHubFiles = axonIvyMarketRepoServiceImpl.fetchMarketItemsBySHA1Range(startSHA1, endSHA1);
    assertEquals(0, gitHubFiles.size(), "Expected no files when commit has no associated files");

    when(mockCommit.listFiles()).thenReturn(pagedFile);
    var mockFile = mock(File.class);
    when(mockFile.getFileName()).thenReturn(fileName);
    when(mockFile.getRawUrl()).thenReturn(URI.create("http://github/test-repo-url/test-meta.json").toURL());
    when(mockFile.getStatus()).thenReturn("added");
    when(mockFile.getPreviousFilename()).thenReturn("test-prev-meta.json");

    var mockFile2 = mock(File.class);
    when(mockFile2.getFileName()).thenReturn(fileName2);

    var logoFile = mock(File.class);
    when(logoFile.getFileName()).thenReturn(logoFileName);
    when(logoFile.getRawUrl()).thenReturn(URI.create("http://github/test-repo-url/logo.png").toURL());
    when(logoFile.getStatus()).thenReturn("modified");
    when(logoFile.getPreviousFilename()).thenReturn("logo.png");

    var logoDarkFile = mock(File.class);
    when(logoDarkFile.getFileName()).thenReturn(logoDarkFileName);
    when(logoDarkFile.getRawUrl()).thenReturn(URI.create("http://github/test-repo-url/logo-dark.png").toURL());
    when(logoDarkFile.getStatus()).thenReturn("added");
    when(logoDarkFile.getPreviousFilename()).thenReturn("logo-dark.png");

    when(pagedFile.toList()).thenReturn(List.of(mockFile, mockFile2, logoFile, logoDarkFile));

    gitHubFiles = axonIvyMarketRepoServiceImpl.fetchMarketItemsBySHA1Range(startSHA1, endSHA1);
    assertEquals(3, gitHubFiles.size(),
        "Expected exactly one meta.json file and two logo files matching the 'market/' directory");
  }

  @Test
  void testGetLastCommit() {
    var lastCommit = axonIvyMarketRepoServiceImpl.getLastCommit(0L);
    assertNull(lastCommit, "Expected lastCommit to be null for an ID of 0L");
  }

  @Test
  void testGetMarketItemByPath() throws IOException {
    var mockGHContent = mock(GHContent.class);
    List<GHContent> mockGhContents = new ArrayList<>();
    mockGhContents.add(mockGHContent);
    when(gitHubService.getDirectoryContent(any(), any(), any())).thenReturn(mockGhContents);
    var ghContents = axonIvyMarketRepoServiceImpl.getMarketItemByPath("market/connector/a-trust");
    assertEquals(1, ghContents.size(), "Expected exactly one GHContent item for the given path");
  }
}
