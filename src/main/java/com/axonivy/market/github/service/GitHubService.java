package com.axonivy.market.github.service;

import java.io.IOException;
import java.util.List;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

public interface GithubService {

  public GitHub getGithub() throws IOException;

  public GHOrganization getOrganization(String orgName) throws IOException;

  public GHRepository getRepository(String repositoryPath) throws IOException;

  public List<GHContent> getDirectoryContent(GHRepository ghRepository, String path) throws IOException;

  public GHContent getGHContent(GHRepository ghRepository, String path) throws IOException;
}
