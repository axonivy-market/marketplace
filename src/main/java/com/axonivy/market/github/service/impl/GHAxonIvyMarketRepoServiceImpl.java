package com.axonivy.market.github.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
  public static final String META_FILE = "meta.json";
  public static final String LOGO_FILE = "logo.png";
  private GHOrganization organization;
  private GHRepository repository;
  private final GithubRepoMetaRepository repoMetaRepository;

  public GHAxonIvyMarketRepoServiceImpl(GithubRepoMetaRepository repoMetaRepository) {
    this.repoMetaRepository = repoMetaRepository;
  }

  @Override
  public Map<String, List<GHContent>> fetchAllMarketItems() {
    // TODO
    Map<String, List<GHContent>> ghContentMap = new HashMap<String, List<GHContent>>();
    try {
      var directoryContent = getDirectoryContent(getRepository(), "market");
      for (var content : directoryContent) {
        extractFileOfContent(content, ghContentMap);
      }
    } catch (Exception e) {
      log.error("Cannot fetch GH Content", e);
    }
    return ghContentMap;
  }

  private void extractFileOfContent(GHContent content, Map<String, List<GHContent>> ghContentMap) throws IOException {
    if (content.isDirectory()) {
      var listOfContent = content.listDirectoryContent();
      for (var childContent : listOfContent.toList()) {
        if (childContent.isFile() && isMetaOrLogoFile(childContent)) {
          var contents = ghContentMap.getOrDefault(content.getPath(), new ArrayList<GHContent>());
          contents.add(childContent);
          ghContentMap.putIfAbsent(content.getPath(), contents);
        } else {
          extractFileOfContent(childContent, ghContentMap);
        }
      }
    }
  }

  private boolean isMetaOrLogoFile(GHContent content) {
    var filePath = content.getPath();
    return StringUtils.endsWithAny(filePath, META_FILE, LOGO_FILE);
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
      var lastCommits = getRepository().queryCommits().since(lastChange).from("master").list().toList();
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
