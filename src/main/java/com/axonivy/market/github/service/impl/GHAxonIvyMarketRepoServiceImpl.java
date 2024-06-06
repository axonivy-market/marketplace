package com.axonivy.market.github.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.github.service.AbstractGithubService;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.repository.GithubRepoMetaRepository;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.github.service.AbstractGithubService;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.repository.GithubRepoMetaRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class GHAxonIvyMarketRepoServiceImpl extends AbstractGithubService implements GHAxonIvyMarketRepoService {

    private final GithubRepoMetaRepository repoMetaRepository;
    private GHOrganization organization;
    private GHRepository repository;

  public GHAxonIvyMarketRepoServiceImpl(GithubRepoMetaRepository repoMetaRepository) {
    this.repoMetaRepository = repoMetaRepository;
  }

  @Override
  public Map<String, List<GHContent>> fetchAllMarketItems() {
    // TODO
    Map<String, List<GHContent>> ghContentMap = new HashMap<String, List<GHContent>>();
    try {
      var directoryContent = getDirectoryContent(getRepository(), GitHubConstants.AXONIVY_MARKETPLACE_PATH);
      for (var content : directoryContent) {
        extractFileOfContent(content, ghContentMap);
      }
    } catch (Exception e) {
      log.error("Cannot fetch GH Content", e);
    }

    @Override
    public Map<String, List<GHContent>> fetchAllMarketItems() {
        Map<String, List<GHContent>> ghContentMap = new HashMap<String, List<GHContent>>();
        try {
            var directoryContent = getDirectoryContent(getRepository(), GitHubConstants.AXON_IVY_MARKET_PLACE_PATH);
            for (var content : directoryContent) {
                if (content.getName().equals("portal")) {
                    log.warn(content.getName());
                    extractFileOfContent(content, ghContentMap);
                }
            }
        } catch (Exception e) {
            log.error("Cannot fetch GH Content", e);
        }
      }
    }
  }

  @Override
  public GHCommit getLastCommit() {
    GHCommit lastCommit = null;
    long lastChange = 0l;

    var marketRepoMetaData = repoMetaRepository.findByRepoName(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    if (marketRepoMetaData == null || marketRepoMetaData.getLastChange() == 0l) {
      // Initial commit
      LocalDateTime now = LocalDateTime.of(2020, 10, 30, 0, 0);
      lastChange = now.atZone(ZoneId.systemDefault()).toEpochSecond();
    } else {
      lastChange = marketRepoMetaData.getLastChange();
    }

    try {
      var lastCommits = getRepository().queryCommits().since(lastChange).from("master").list().toList();
      // Pick top-one
      lastCommit = CollectionUtils.firstElement(lastCommits);
      log.warn("Last Commits {}", lastCommit.getCommitDate());
    } catch (Exception e) {
      e.printStackTrace();
    }

    @Override
    public GHCommit getLastCommit() {
        GHCommit lastCommit = null;
        long lastChange = 0L;

        var marketRepoMetaData = repoMetaRepository.findByRepoName(GitHubConstants.AXON_IVY_MARKET_PLACE_REPO_NAME);
        if (marketRepoMetaData == null || marketRepoMetaData.getLastChange() == 0L) {
            // Initial commit
            LocalDateTime now = LocalDateTime.of(2020, 10, 30, 0, 0);
            lastChange = now.atZone(ZoneId.systemDefault()).toEpochSecond();
        } else {
            lastChange = marketRepoMetaData.getLastChange();
        }

        try {
            var lastCommits = getRepository().queryCommits().since(lastChange).list().toList();
            // Pick top-one
            lastCommit = CollectionUtils.firstElement(lastCommits);
            log.warn("Last Commits {}", lastCommit.getCommitDate());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lastCommit;
    }

    public GHOrganization getOrganization() throws IOException {
        if (organization == null) {
            organization = getOrganization(GitHubConstants.AXON_IVY_MARKET_ORGANIZATION_NAME);
        }
        return organization;
    }

    public GHRepository getRepository() throws IOException {
        if (repository == null) {
            repository = getOrganization().getRepository(GitHubConstants.AXON_IVY_MARKET_PLACE_REPO_NAME);
        }
        return repository;
    }

    @Override
    public GHContent getContentFromGHRepoAndTag(String repoName, String filePath, String tagVersion) {
        try {
            return getOrganization().getRepository(repoName).getFileContent(filePath, tagVersion);
        } catch (IOException e) {
            log.error("Cannot Get Content From File Directory", e);
            return null;
        }
    }

    @Override
    public GHContent getContentFromGHRepo(String repoName, String filePath) {
        try {
            return getOrganization().getRepository(repoName).getFileContent(filePath);
        } catch (IOException e) {
            log.error("Cannot Get Content From File Directory", e);
            return null;
        }
    }

    @Override
    public List<GHTag> getTagsFromRepoName(String repoName) {
        try {
            return getOrganization().getRepository(repoName).listTags().toList();
        } catch (IOException e) {
            log.error("Cannot Get Tag From Current Repo", e);
            return Collections.emptyList();
        }
    }
  private GHOrganization getOrganization() throws IOException {
    if (organization == null) {
      organization = getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
    }
    return organization;
  }

  public GHRepository getRepository() throws IOException {
    if (repository == null) {
      repository = getOrganization().getRepository(GitHubConstants.AXONIVY_MARKETPLACE_REPO_NAME);
    }
    return repository;
  }

}
