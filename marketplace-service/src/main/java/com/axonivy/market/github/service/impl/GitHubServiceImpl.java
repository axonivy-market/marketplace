package com.axonivy.market.github.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.User;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.repository.UserRepository;
import org.kohsuke.github.*;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class GitHubServiceImpl implements GitHubService {

  private final RestTemplate restTemplate;
  private final UserRepository userRepository;

  private static final String GITHUB_TOKEN_FILE = "classpath:github.token";

  public GitHubServiceImpl(RestTemplateBuilder restTemplateBuilder, UserRepository userRepository) {
    this.restTemplate = restTemplateBuilder.build();
    this.userRepository = userRepository;
  }

  @Override
  public GitHub getGitHub() throws IOException {
    File gitHubToken = ResourceUtils.getFile(GITHUB_TOKEN_FILE);
    var token = Files.readString(gitHubToken.toPath());
    return new GitHubBuilder().withOAuthToken(token.trim().strip()).build();
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
    public Map<String, Object> getAccessToken(String code, String clientId, String clientSecret) throws Oauth2ExchangeCodeException {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(GitHubConstants.Json.CLIENT_ID, clientId);
        params.add(GitHubConstants.Json.CLIENT_SECRET, clientSecret);
        params.add(GitHubConstants.Json.CODE, code);

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(GitHubConstants.GITHUB_GET_ACCESS_TOKEN_URL, request, Map.class);
        if (response.getBody().containsKey(GitHubConstants.Json.ERROR)) {
            throw new Oauth2ExchangeCodeException(response.getBody().get(GitHubConstants.Json.ERROR).toString(), response.getBody().get(GitHubConstants.Json.ERROR_DESCRIPTION).toString());
        }
        return response.getBody();
    }

    @Override
    public User getAndUpdateUser(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.github.com/user", HttpMethod.GET, entity, Map.class);

        Map<String, Object> userDetails = response.getBody();

        if (userDetails == null) {
            throw new RuntimeException("Failed to fetch user details from GitHub");
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