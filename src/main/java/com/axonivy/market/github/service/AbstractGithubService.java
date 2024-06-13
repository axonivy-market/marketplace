package com.axonivy.market.github.service;

import java.io.IOException;
import java.util.List;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.springframework.util.Assert;

import com.axonivy.market.github.GitHubProvider;

public abstract class AbstractGithubService {

  public GHOrganization getOrganization(String orgName) throws IOException {
    return GitHubProvider.get().getOrganization(orgName);
  }

  public List<GHContent> getDirectoryContent(GHRepository ghRepository, String path) throws IOException {
    Assert.notNull(ghRepository, "Repository must not be null");
    return ghRepository.getDirectoryContent(path);
  }
}
