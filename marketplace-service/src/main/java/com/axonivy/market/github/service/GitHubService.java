package com.axonivy.market.github.service;

import com.axonivy.market.entity.User;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface GitHubService {

  GitHub getGitHub() throws IOException;

  GHOrganization getOrganization(String orgName) throws IOException;

  GHRepository getRepository(String repositoryPath) throws IOException;

  List<GHContent> getDirectoryContent(GHRepository ghRepository, String path, String ref) throws IOException;

  GHContent getGHContent(GHRepository ghRepository, String path, String ref) throws IOException;

  Map<String, Object> getAccessToken(String code, String clientId, String clientSecret);

  User getAndUpdateUser(String accessToken);
}
