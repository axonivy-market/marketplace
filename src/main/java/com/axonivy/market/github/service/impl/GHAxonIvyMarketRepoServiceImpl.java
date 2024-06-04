package com.axonivy.market.github.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

import com.axonivy.market.github.service.AbstractGithubService;
import com.axonivy.market.github.service.GHAxonIvyMarketRepoService;
import com.axonivy.market.repository.GithubRepoMetaRepository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class GHAxonIvyMarketRepoServiceImpl extends AbstractGithubService implements GHAxonIvyMarketRepoService {

  private GHOrganization organization;
  private GHRepository repository;
  private final GithubRepoMetaRepository repoMetaRepository;

  public GHAxonIvyMarketRepoServiceImpl(GithubRepoMetaRepository repoMetaRepository) {
    this.repoMetaRepository = repoMetaRepository;
  }

  @Override
  public Map<String, List<GHContent>> fetchAllMarketItems() {
    Map<String, List<GHContent>> ghContentMap = new HashMap<String, List<GHContent>>();
    try {
      var directoryContent = getDirectoryContent(getRepository(), "market");
      for (var content : directoryContent) {
        if (content.getName().equals("portal")) {
          log.warn(content.getName());
          extractFileOfContent(content, ghContentMap);
        }
      }
    } catch (Exception e) {
      log.error("Cannot fetch GH Content", e);
    }
    return ghContentMap;
  }

  private void extractFileOfContent(GHContent content, Map<String, List<GHContent>> ghContentMap) throws IOException {
    if (content.isDirectory()) {
      var listOfContent = content.listDirectoryContent();
      log.warn("found nha");
      for (var childContent : listOfContent.toList()) {
        if (childContent.isFile()) {
          var contents = ghContentMap.getOrDefault(content.getPath(), new ArrayList<GHContent>());
          contents.add(childContent);
          ghContentMap.putIfAbsent(content.getPath(), contents);
        } else {
          extractFileOfContent(childContent, ghContentMap);
        }
      }
    }
  }

  @Override
  public GHCommit getLastCommit() {
    GHCommit lastCommit = null;
    long lastChange = 0l;

    var marketRepoMetaData = repoMetaRepository.findByRepoName("market");
    if (marketRepoMetaData == null || marketRepoMetaData.getLastChange() == 0l) {
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
      organization = getOrganization("axonivy-market");
    }
    return organization;
  }

  public GHRepository getRepository() throws IOException {
    if (repository == null) {
      repository = getOrganization().getRepository("market");
    }
    return repository;
  }
}
