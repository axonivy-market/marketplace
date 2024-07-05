package com.axonivy.market.github.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommitQueryBuilder;
import org.kohsuke.github.GHCompare;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.springframework.stereotype.Service;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.enums.FileStatus;
import com.axonivy.market.enums.FileType;
import com.axonivy.market.github.model.GitHubFile;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.util.GitHubUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class GHAxonIvyMarketRepoServiceImpl implements GHAxonIvyMarketRepoService {
  private static final LocalDateTime INITIAL_COMMIT_DATE = LocalDateTime.of(2020, 10, 30, 0, 0);
  private GHOrganization organization;
  private GHRepository repository;

  private final GitHubService gitHubService;

  public GHAxonIvyMarketRepoServiceImpl(GitHubService gitHubService) {
    this.gitHubService = gitHubService;
  }

  @Override
  public Map<String, List<GHContent>> fetchAllMarketItems() {
    Map<String, List<GHContent>> ghContentMap = new HashMap<>();
    try {
      List<GHContent> directoryContent = gitHubService.getDirectoryContent(getRepository(),
          GitHubConstants.AXONIVY_MARKETPLACE_PATH, GitHubConstants.DEFAULT_BRANCH);
      for (var content : directoryContent) {
        extractFileInDirectoryContent(content, ghContentMap);
      }
    } catch (Exception e) {
      log.error("Cannot fetch GHContent: ", e);
    }
    return ghContentMap;
  }

  private void extractFileInDirectoryContent(GHContent content, Map<String, List<GHContent>> ghContentMap)
      throws IOException {
    if (content != null && content.isDirectory()) {
      for (var childContent : GitHubUtils.mapPagedIteratorToList(content.listDirectoryContent())) {
        if (childContent.isFile()) {
          var contents = ghContentMap.getOrDefault(content.getPath(), new ArrayList<>());
          contents.add(childContent);
          ghContentMap.putIfAbsent(content.getPath(), contents);
        } else {
          extractFileInDirectoryContent(childContent, ghContentMap);
        }
      }
    }
  }

  @Override
  public GHCommit getLastCommit(long lastCommitTime) {
    if (lastCommitTime == 0l) {
      lastCommitTime = INITIAL_COMMIT_DATE.atZone(ZoneId.systemDefault()).toEpochSecond();
    }
    try {
      GHCommitQueryBuilder commitBuilder = createQueryCommitsBuilder(lastCommitTime);
      return GitHubUtils.mapPagedIteratorToList(commitBuilder.list()).stream().findFirst().orElse(null);
    } catch (Exception e) {
      log.error("Cannot query GHCommit: ", e);
    }
    return null;
  }

  private GHCommitQueryBuilder createQueryCommitsBuilder(long lastCommitTime) {
    return getRepository().queryCommits().since(lastCommitTime).from(GitHubConstants.DEFAULT_BRANCH);
  }

  @Override
  public List<GitHubFile> fetchMarketItemsBySHA1Range(String fromSHA1, String toSHA1) {
    Map<String, GitHubFile> gitHubFileMap = new HashMap<>();
    try {
      GHCompare compareResult = getRepository().getCompare(fromSHA1, toSHA1);
      for (var commit : GitHubUtils.mapPagedIteratorToList(compareResult.listCommits())) {
        var listFiles = commit.listFiles();
        if (listFiles == null) {
          continue;
        }
        GitHubUtils.mapPagedIteratorToList(listFiles).forEach(file -> {
          String fullPathName = file.getFileName();
          if (FileType.of(fullPathName) != null) {
            var gitHubFile = new GitHubFile();
            gitHubFile.setFileName(fullPathName);
            gitHubFile.setPath(file.getRawUrl().getPath());
            gitHubFile.setStatus(FileStatus.of(file.getStatus()));
            gitHubFile.setType(FileType.of(fullPathName));
            gitHubFile.setPreviousFilename(file.getPreviousFilename());
            gitHubFileMap.put(fullPathName, gitHubFile);
          }
        });
      }
    } catch (Exception e) {
      log.error("Cannot get GH compare: ", e);
    }
    return new ArrayList<>(gitHubFileMap.values());
  }

  private GHOrganization getOrganization() throws IOException {
    if (organization == null) {
      organization = gitHubService.getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
    }
    return organization;
  }

  @Override
  public GHRepository getRepository() {
    if (repository == null) {
      try {
        repository = getOrganization().getRepository(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
      } catch (IOException e) {
        log.error("Get AxonIvy Market repo failed: ", e);
      }
    }
    return repository;
  }

}
