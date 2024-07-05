package com.axonivy.market.github.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.axonivy.market.entity.User;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

public interface GitHubService {

  public GitHub getGitHub() throws IOException;

  public GHOrganization getOrganization(String orgName) throws IOException;

  public GHRepository getRepository(String repositoryPath) throws IOException;

  public List<GHContent> getDirectoryContent(GHRepository ghRepository, String path) throws IOException;

  public GHContent getGHContent(GHRepository ghRepository, String path) throws IOException;

  Map<String, Object> getAccessToken(String code, String clientId, String clientSecret);

  User getAndUpdateUser(String accessToken);
}
