package com.axonivy.market.github.service.impl;

import java.io.IOException;
import java.util.List;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTag;
import org.springframework.stereotype.Service;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.github.service.GitHubService;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class GHAxonIvyProductRepoServiceImpl implements GHAxonIvyProductRepoService {
  private GHOrganization organization;

  private final GitHubService gitHubService;

  public GHAxonIvyProductRepoServiceImpl(GitHubService gitHubService) {
    this.gitHubService = gitHubService;
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

  private GHOrganization getOrganization() throws IOException {
    if (organization == null) {
      organization = gitHubService.getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
    }
    return organization;
  }

  @Override
  public List<GHTag> getAllTagsFromRepoName(String repoName) throws IOException {
    return getOrganization().getRepository(repoName).listTags().toList();
  }
}
