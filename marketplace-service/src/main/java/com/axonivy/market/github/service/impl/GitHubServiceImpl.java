package com.axonivy.market.github.service.impl;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.User;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.github.model.GitHubProperty;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.GitHubAccessTokenResponse;
import com.axonivy.market.repository.UserRepository;

@Service
public class GitHubServiceImpl implements GitHubService {

  private final RestTemplate restTemplate;
  private final UserRepository userRepository;
  private final GitHubProperty gitHubProperty;

  public GitHubServiceImpl(RestTemplateBuilder restTemplateBuilder, UserRepository userRepository,
      GitHubProperty gitHubProperty) {
    this.restTemplate = restTemplateBuilder.build();
    this.userRepository = userRepository;
    this.gitHubProperty = gitHubProperty;
  }

  @Override
  public GitHub getGitHub() throws IOException {
    return new GitHubBuilder()
        .withOAuthToken(Optional.ofNullable(gitHubProperty).map(GitHubProperty::getToken).orElse(EMPTY).trim())
        .build();
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

  @Override
  public GitHubAccessTokenResponse getAccessToken(String code, String clientId, String clientSecret)
      throws Oauth2ExchangeCodeException {
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add(GitHubConstants.Json.CLIENT_ID, clientId);
    params.add(GitHubConstants.Json.CLIENT_SECRET, clientSecret);
    params.add(GitHubConstants.Json.CODE, code);

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    ResponseEntity<GitHubAccessTokenResponse> responseEntity = restTemplate.postForEntity(
        GitHubConstants.GITHUB_GET_ACCESS_TOKEN_URL, request, GitHubAccessTokenResponse.class);
    GitHubAccessTokenResponse response = responseEntity.getBody();

    if (response != null && response.getError() != null && !response.getError().isBlank()) {
      throw new Oauth2ExchangeCodeException(response.getError(), response.getErrorDescription());
    }

    return response;
  }

  @Override
  public User getAndUpdateUser(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<Map<String, Object>> response = restTemplate.exchange("https://api.github.com/user", HttpMethod.GET,
        entity, new ParameterizedTypeReference<>() {
        });

    Map<String, Object> userDetails = response.getBody();

    if (userDetails == null) {
      throw new NotFoundException(ErrorCode.GITHUB_USER_NOT_FOUND, "Failed to fetch user details from GitHub");
    }

    String gitHubId = userDetails.get(GitHubConstants.Json.USER_ID).toString();
    String name = (String) userDetails.get(GitHubConstants.Json.USER_NAME);
    String avatarUrl = (String) userDetails.get(GitHubConstants.Json.USER_AVATAR_URL);
    String username = (String) userDetails.get(GitHubConstants.Json.USER_LOGIN_NAME);

    User user = userRepository.searchByGitHubId(gitHubId);
    if (user == null) {
      user = new User();
    }
    user.setGitHubId(gitHubId);
    user.setName(name);
    user.setUsername(username);
    user.setAvatarUrl(avatarUrl);
    user.setProvider(GitHubConstants.GITHUB_PROVIDER_NAME);

    userRepository.save(user);

    return user;
  }
}