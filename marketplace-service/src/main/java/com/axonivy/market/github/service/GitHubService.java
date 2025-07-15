package com.axonivy.market.github.service;

import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.github.model.GitHubAccessTokenResponse;
import com.axonivy.market.github.model.GitHubProperty;
import com.axonivy.market.github.model.ProductSecurityInfo;
import com.axonivy.market.model.GitHubReleaseModel;
import org.kohsuke.github.GHArtifact;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.GHWorkflowRun;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface GitHubService {

  GitHub getGitHub() throws IOException;

  GitHub getGitHub(String accessToken) throws IOException;

  GHOrganization getOrganization(String orgName) throws IOException;

  GHRepository getRepository(String repositoryPath) throws IOException;

  List<GHTag> getRepositoryTags(String repositoryPath) throws IOException;

  List<GHContent> getDirectoryContent(GHRepository ghRepository, String path, String ref) throws IOException;

  GHContent getGHContent(GHRepository ghRepository, String path, String ref) throws IOException;

  GitHubAccessTokenResponse getAccessToken(String code, GitHubProperty gitHubProperty)
      throws Oauth2ExchangeCodeException, MissingHeaderException;

  GithubUser getAndUpdateUser(String accessToken);

  void validateUserInOrganizationAndTeam(String accessToken, String team, String org) throws UnauthorizedException;

  List<ProductSecurityInfo> getSecurityDetailsForAllProducts(String accessToken, String orgName);

  Page<GitHubReleaseModel> getGitHubReleaseModels(Product product, PagedIterable<GHRelease> ghReleasePagedIterable,
      Pageable pageable) throws IOException;

  GitHubReleaseModel getGitHubReleaseModelByProductIdAndReleaseId(Product product, Long releaseId) throws IOException;

  GHWorkflowRun getLatestWorkflowRun(GHRepository repo, String workflowFileName) throws IOException;

  GHArtifact getExportTestArtifact(GHWorkflowRun run) throws IOException;

  InputStream downloadArtifactZip(GHArtifact artifact) throws IOException;
  }
