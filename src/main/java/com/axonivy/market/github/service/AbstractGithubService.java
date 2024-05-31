package com.axonivy.market.github.service;

import java.io.IOException;
import java.util.List;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;

import com.axonivy.market.github.GitHubProvider;

public abstract class AbstractGithubService {

  protected GHOrganization getOrganization(String orgName) throws IOException {
    var github = GitHubProvider.get();
    return github.getOrganization(orgName);
  }

  protected List<GHContent> getDirectoryContent(GHRepository ghRepository, String path) throws IOException {
    if (ghRepository != null) {
      return ghRepository.getDirectoryContent(path);
    }
    return null;
  }
}
