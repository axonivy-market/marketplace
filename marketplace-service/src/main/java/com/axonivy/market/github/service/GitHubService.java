package com.axonivy.market.github.service;

import java.io.IOException;
import java.util.List;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import com.axonivy.market.entity.User;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.github.model.GitHubAccessTokenResponse;
import com.axonivy.market.github.model.GitHubProperty;

public interface GitHubService {

  GitHub getGitHub() throws IOException;

  GHOrganization getOrganization(String orgName) throws IOException;

  GHRepository getRepository(String repositoryPath) throws IOException;

  List<GHContent> getDirectoryContent(GHRepository ghRepository, String path, String ref) throws IOException;

  GHContent getGHContent(GHRepository ghRepository, String path, String ref) throws IOException;

  GitHubAccessTokenResponse getAccessToken(String code, GitHubProperty gitHubProperty)
      throws Oauth2ExchangeCodeException, MissingHeaderException;

  User getAndUpdateUser(String accessToken);

  void validateUserOrganization(String accessToken, String organization) throws UnauthorizedException;
}
