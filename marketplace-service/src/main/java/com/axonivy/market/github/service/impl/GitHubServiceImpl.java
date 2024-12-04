package com.axonivy.market.github.service.impl;

import com.axonivy.market.constants.ErrorMessageConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.User;
import com.axonivy.market.enums.AccessLevel;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.github.model.CodeScanning;
import com.axonivy.market.github.model.Dependabot;
import com.axonivy.market.github.model.GitHubAccessTokenResponse;
import com.axonivy.market.github.model.GitHubProperty;
import com.axonivy.market.github.model.SecretScanning;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.model.ProductSecurityInfo;
import com.axonivy.market.repository.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Log4j2
@Service
public class GitHubServiceImpl implements GitHubService {

  private final RestTemplate restTemplate;
  private final UserRepository userRepository;
  private final GitHubProperty gitHubProperty;
  private ThreadPoolTaskScheduler taskScheduler;

  public GitHubServiceImpl(RestTemplateBuilder restTemplateBuilder, UserRepository userRepository,
      GitHubProperty gitHubProperty, ThreadPoolTaskScheduler taskScheduler) {
    this.restTemplate = restTemplateBuilder.build();
    this.userRepository = userRepository;
    this.gitHubProperty = gitHubProperty;
    this.taskScheduler = taskScheduler;
  }

  @Override
  public GitHub getGitHub() throws IOException {
    return new GitHubBuilder().withOAuthToken(
        Optional.ofNullable(gitHubProperty).map(GitHubProperty::getToken).orElse(EMPTY).trim()).build();
  }

  @Override
  public GitHub getGitHub(String accessToken) throws IOException {
    return new GitHubBuilder().withOAuthToken(accessToken).build();
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
  public List<GHTag> getRepositoryTags(String repositoryPath) throws IOException {
    return getRepository(repositoryPath).listTags().toList();
  }

  @Override
  public GHContent getGHContent(GHRepository ghRepository, String path, String ref) throws IOException {
    Assert.notNull(ghRepository, "Repository must not be null");
    return ghRepository.getFileContent(path, ref);
  }

  @Override
  public GitHubAccessTokenResponse getAccessToken(String code,
      GitHubProperty gitHubProperty) throws Oauth2ExchangeCodeException, MissingHeaderException {
    if (gitHubProperty == null) {
      throw new MissingHeaderException();
    }
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add(GitHubConstants.Json.CLIENT_ID, gitHubProperty.getOauth2ClientId());
    params.add(GitHubConstants.Json.CLIENT_SECRET, gitHubProperty.getOauth2ClientSecret());
    params.add(GitHubConstants.Json.CODE, code);

    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    ResponseEntity<GitHubAccessTokenResponse> responseEntity = restTemplate.postForEntity(
        GitHubConstants.GITHUB_GET_ACCESS_TOKEN_URL, request, GitHubAccessTokenResponse.class);
    GitHubAccessTokenResponse response = responseEntity.getBody();

    if (response != null && response.getError() != null && !response.getError().isBlank()) {
      log.error(String.format(ErrorMessageConstants.CURRENT_CLIENT_ID_MISMATCH_MESSAGE, code,
          gitHubProperty.getOauth2ClientId()));
      throw new Oauth2ExchangeCodeException(response.getError(), response.getErrorDescription());
    }

    return response;
  }

  @Override
  public User getAndUpdateUser(String accessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    ResponseEntity<Map<String, Object>> response = restTemplate.exchange(GitHubConstants.Url.USER, HttpMethod.GET,
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

  @Override
  public void validateUserInOrganizationAndTeam(String accessToken, String organization,
      String team) throws UnauthorizedException {
    try {
      var gitHub = getGitHub(accessToken);
      if (isUserInOrganizationAndTeam(gitHub, organization, team)) {
        return;
      }
    } catch (IOException e) {
      log.error(e.getStackTrace());
    }

    throw new UnauthorizedException(ErrorCode.GITHUB_USER_UNAUTHORIZED.getCode(),
        String.format(ErrorMessageConstants.INVALID_USER_ERROR, ErrorCode.GITHUB_USER_UNAUTHORIZED.getHelpText(), team,
            organization));
  }

  @Override
  public List<ProductSecurityInfo> getSecurityDetailsForAllProducts(String accessToken, String orgName) {
    List<ProductSecurityInfo> securityInfoList;
    ExecutorService executor = taskScheduler.getScheduledExecutor();
    try {
      GitHub gitHub = getGitHub(accessToken);
      GHOrganization organization = gitHub.getOrganization(orgName);

      List<CompletableFuture<ProductSecurityInfo>> futures = organization.listRepositories().toList().stream()
          .map(repo -> CompletableFuture.supplyAsync(() -> fetchSecurityInfoSafe(repo, organization, accessToken), executor))
          .toList();

      securityInfoList = futures.stream()
          .map(CompletableFuture::join)
          .collect(Collectors.toList());

      securityInfoList.sort(Comparator.comparing(ProductSecurityInfo::getRepoName));
    } catch (IOException e) {
      throw new RuntimeException("Error fetching repository data", e);
    }

    return securityInfoList;
  }

  private boolean isUserInOrganizationAndTeam(GitHub gitHub, String organization, String teamName) throws IOException {
    if (gitHub == null) {
      return false;
    }

    var hashMapTeams = gitHub.getMyTeams();
    var hashSetTeam = hashMapTeams.get(organization);
    if (CollectionUtils.isEmpty(hashSetTeam)) {
      return false;
    }

    for (GHTeam team : hashSetTeam) {
      if (teamName.equals(team.getName())) {
        return true;
      }
    }

    return false;
  }

  private ProductSecurityInfo fetchSecurityInfoSafe(GHRepository repo, GHOrganization organization, String accessToken) {
    try {
      return fetchSecurityInfo(repo, organization, accessToken);
    } catch (IOException e) {
      log.error("Error fetching security info for repo: " + repo.getName(), e);
      return new ProductSecurityInfo();
    }
  }

  private ProductSecurityInfo fetchSecurityInfo(GHRepository repo, GHOrganization organization,
      String accessToken) throws IOException {
    ProductSecurityInfo productSecurityInfo = new ProductSecurityInfo();
    productSecurityInfo.setRepoName(repo.getName());
    productSecurityInfo.setVisibility(repo.getVisibility().toString());
    productSecurityInfo.setArchived(repo.isArchived());
    String defaultBranch = repo.getDefaultBranch();
    productSecurityInfo.setBranchProtectionEnabled(repo.getBranch(defaultBranch).isProtected());
    String latestCommitSHA = repo.getBranches().get(defaultBranch).getSHA1();
    GHCommit latestCommit = repo.getCommit(latestCommitSHA);
    productSecurityInfo.setLatestCommitSHA(latestCommitSHA);
    productSecurityInfo.setLastCommitDate(latestCommit.getCommitDate());

    productSecurityInfo.setVulnerabilities(getVulnerabilitiesMap(repo, organization, accessToken));
    productSecurityInfo.setDependabot(getDependabotAlerts(repo, organization, accessToken));
    productSecurityInfo.setSecretsScanning(getNumberOfSecretScanningAlerts(repo, organization, accessToken));
    productSecurityInfo.setCodeScanning(getCodeScanningAlerts(repo, organization, accessToken));
    return productSecurityInfo;
  }

  private Map<String, Integer> getVulnerabilitiesMap(GHRepository repo, GHOrganization organization,
      String accessToken) {
    Map<String, Integer> severityCountMap = new HashMap<>();
    try {
      List<Map<String, Object>> combinedAdvisories = new ArrayList<>();

      ResponseEntity<List<Map<String, Object>>> responseDraft = fetchApiResponseAsList(accessToken,
          String.format(GitHubConstants.Url.REPO_SECURITY_ADVISORIES, organization.getLogin(), repo.getName(), "draft"));
      ResponseEntity<List<Map<String, Object>>> responseTriage = fetchApiResponseAsList(accessToken,
          String.format(GitHubConstants.Url.REPO_SECURITY_ADVISORIES, organization.getLogin(), repo.getName(), "triage"));

      if (responseDraft.getBody() != null) {
        combinedAdvisories.addAll(responseDraft.getBody());
      }
      if (responseTriage.getBody() != null) {
        combinedAdvisories.addAll(responseTriage.getBody());
      }

      for (Map<String, Object> advisory : combinedAdvisories) {
        if (advisory != null) {
          String severity = (String) advisory.get(GitHubConstants.Json.SEVERITY);
          if (severity != null) {
            severityCountMap.put(severity, severityCountMap.getOrDefault(severity, 0) + 1);
          }
        }
      }
    }
    catch (RestClientException e) {
      log.warn(e);
    }
    return severityCountMap;
  }

  private Dependabot getDependabotAlerts(GHRepository repo, GHOrganization organization, String accessToken) {
    Dependabot dependabot = new Dependabot();
    try {
      ResponseEntity<List<Map<String, Object>>> response = fetchApiResponseAsList(accessToken,
          String.format(GitHubConstants.Url.REPO_DEPENDABOT_ALERTS_OPEN, organization.getLogin(), repo.getName()));
      dependabot.setStatus(AccessLevel.ENABLED);
      Map<String, Integer> severityMap = new HashMap<>();
      if (response.getBody() != null) {
        List<Map<String, Object>> alerts = response.getBody();
        for (Map<String, Object> alert : alerts) {
          Object advisoryObj = alert.get(GitHubConstants.Json.SEVERITY_ADVISORY);
          if (advisoryObj instanceof Map<?, ?> securityAdvisory) {
            String severity = (String) securityAdvisory.get(GitHubConstants.Json.SEVERITY);
            if (severity != null) {
              severityMap.put(severity, severityMap.getOrDefault(severity, 0) + 1);
            }
          }
        }
      }
      dependabot.setAlerts(severityMap);
    }
    catch (HttpClientErrorException.Forbidden e) {
      log.warn(e);
      dependabot.setStatus(AccessLevel.DISABLED);
    }
    catch (HttpClientErrorException.NotFound e) {
      log.warn(e);
      dependabot.setStatus(AccessLevel.NO_PERMISSION);
    }
    return dependabot;
  }

  private SecretScanning getNumberOfSecretScanningAlerts(GHRepository repo, GHOrganization organization, String accessToken) {
    SecretScanning secretScanning = new SecretScanning();
    try {
      ResponseEntity<List<Map<String, Object>>> response = fetchApiResponseAsList(accessToken,
          String.format(GitHubConstants.Url.REPO_SECRET_SCANNING_ALERTS_OPEN, organization.getLogin(), repo.getName()));
      secretScanning.setStatus(AccessLevel.ENABLED);
      if (response.getBody() != null) {
        secretScanning.setNumberOfAlerts(response.getBody().size());
      }
    }
    catch (HttpClientErrorException.Forbidden e) {
      log.warn(e);
      secretScanning.setStatus(AccessLevel.DISABLED);
    }
    catch (HttpClientErrorException.NotFound e) {
      log.warn(e);
      secretScanning.setStatus(AccessLevel.NO_PERMISSION);
    }
    return secretScanning;
  }

  private CodeScanning getCodeScanningAlerts(GHRepository repo, GHOrganization organization, String accessToken) {
    CodeScanning codeScanning = new CodeScanning();
    try {
      ResponseEntity<List<Map<String, Object>>> response = fetchApiResponseAsList(accessToken,
          String.format(GitHubConstants.Url.REPO_CODE_SCANNING_ALERTS_OPEN, organization.getLogin(), repo.getName()));
      codeScanning.setStatus(AccessLevel.ENABLED);
      Map<String, Integer> codeScanningMap = new HashMap<>();
      if (response.getBody() != null) {
        List<Map<String, Object>> alerts = response.getBody();
        for (Map<String, Object> alert : alerts) {
          Object ruleObj = alert.get(GitHubConstants.Json.RULE);
          if (ruleObj instanceof Map<?, ?> rule) {
            String severity = (String) rule.get(GitHubConstants.Json.SEVERITY);
            if (severity != null) {
              codeScanningMap.put(severity, codeScanningMap.getOrDefault(severity, 0) + 1);
            }
          }
        }
      }
      codeScanning.setAlerts(codeScanningMap);
    }
    catch (HttpClientErrorException.Forbidden e) {
      log.warn(e);
      codeScanning.setStatus(AccessLevel.DISABLED);
    }
    catch (HttpClientErrorException.NotFound e) {
      log.warn(e);
      codeScanning.setStatus(AccessLevel.NO_PERMISSION);
    }
    return codeScanning;
  }

  private ResponseEntity<List<Map<String, Object>>> fetchApiResponseAsList(String accessToken, String url) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    return restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
    });
  }
}
