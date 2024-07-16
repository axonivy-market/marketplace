package com.axonivy.market.github.service.impl;

import java.io.IOException;
import java.util.List;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.axonivy.market.github.model.GitHubProperty;
import com.axonivy.market.github.service.GitHubService;

@Service
public class GitHubServiceImpl implements GitHubService {

  private GitHubProperty gitHubProperty;

  public GitHubServiceImpl(GitHubProperty gitHubProperty) {
    this.gitHubProperty = gitHubProperty;
  }

  @Override
  public GitHub getGitHub() throws IOException {
    return new GitHubBuilder().withOAuthToken(gitHubProperty.getToken().trim().strip()).build();
  }

  @Override
  public GHOrganization getOrganization(String orgName) throws IOException {
    return getGitHub().getOrganization(orgName);
  }

  @Override
  public List<GHContent> getDirectoryContent(GHRepository ghRepository, String path, String ref) throws IOException {
    Assert.notNull(ghRepository, "Repository must not be null");
    return ghRepository.getDirectoryContent(path, ref);
  }

  @Override
  public GHRepository getRepository(String repositoryPath) throws IOException {
    return getGitHub().getRepository(repositoryPath);
  }

  @Override
  public GHContent getGHContent(GHRepository ghRepository, String path, String ref) throws IOException {
    Assert.notNull(ghRepository, "Repository must not be null");
    return ghRepository.getFileContent(path, ref);
  }
}