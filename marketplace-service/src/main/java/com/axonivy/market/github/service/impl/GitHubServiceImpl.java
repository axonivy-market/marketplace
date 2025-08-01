package com.axonivy.market.github.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ErrorMessageConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.ErrorCode;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.github.model.CodeScanning;
import com.axonivy.market.github.model.Dependabot;
import com.axonivy.market.github.model.GitHubAccessTokenResponse;
import com.axonivy.market.github.model.GitHubProperty;
import com.axonivy.market.github.model.ProductSecurityInfo;
import com.axonivy.market.github.model.SecretScanning;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.GitHubReleaseModel;
import com.axonivy.market.repository.GithubUserRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.kohsuke.github.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;

@Log4j2
@Service
public class GitHubServiceImpl implements GitHubService {

  public static final int PAGE_SIZE_OF_WORKFLOW = 10;
  private final RestTemplate restTemplate;
  private final GithubUserRepository githubUserRepository;
  private final GitHubProperty gitHubProperty;
  private final ThreadPoolTaskScheduler taskScheduler;
  private static final String GITHUB_PULL_REQUEST_NUMBER_REGEX = "#(\\d+)";
  private static final String GITHUB_PULL_REQUEST_LINK = "/pull/";
  private static final String GITHUB_USERNAME_REGEX = "@([a-zA-Z0-9\\-]+)";
  private static final String GITHUB_MAIN_LINK = "https://github.com/";
  private static final String FIRST_REGEX_CAPTURING_GROUP="$1";

  public GitHubServiceImpl(RestTemplate restTemplate, GithubUserRepository githubUserRepository,
      GitHubProperty gitHubProperty, ThreadPoolTaskScheduler taskScheduler) {
    this.restTemplate = restTemplate;
    this.githubUserRepository = githubUserRepository;
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
  public GithubUser getAndUpdateUser(String accessToken) {
    try {
      GHMyself myself = getGitHub(accessToken).getMyself();
      GithubUser githubUser = Optional.ofNullable(githubUserRepository.searchByGitHubId(String.valueOf(myself.getId())))
          .orElse(new GithubUser());
      githubUser.setGitHubId(String.valueOf(myself.getId()));
      githubUser.setName(myself.getName());
      githubUser.setUsername(myself.getLogin());
      githubUser.setAvatarUrl(myself.getAvatarUrl());
      githubUser.setProvider(GitHubConstants.GITHUB_PROVIDER_NAME);
      githubUserRepository.save(githubUser);
      return githubUser;
    } catch (IOException e) {
      log.error("GitHub user fetch failed", e);
      throw new NotFoundException(ErrorCode.GITHUB_USER_NOT_FOUND, "Failed to fetch user details from GitHub");
    }
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
    try {
      GitHub gitHub = getGitHub(accessToken);
      GHOrganization organization = gitHub.getOrganization(orgName);

      List<CompletableFuture<ProductSecurityInfo>> futures = organization.listRepositories().toList().stream()
          .map(repo -> CompletableFuture.supplyAsync(() -> fetchSecurityInfoSafe(repo, organization, accessToken),
              taskScheduler.getScheduledExecutor())).toList();

      return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
          .thenApply(v -> futures.stream().map(CompletableFuture::join).sorted(
              Comparator.comparing(ProductSecurityInfo::getRepoName)).collect(Collectors.toList())).join();
    } catch (IOException e) {
      log.error(e.getStackTrace());
      return Collections.emptyList();
    }
  }

  public boolean isUserInOrganizationAndTeam(GitHub gitHub, String organization, String teamName) throws IOException {
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

  public ProductSecurityInfo fetchSecurityInfoSafe(GHRepository repo, GHOrganization organization,
      String accessToken) {
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
    String latestCommitSHA = repo.getBranch(defaultBranch).getSHA1();
    GHCommit latestCommit = repo.getCommit(latestCommitSHA);
    productSecurityInfo.setLatestCommitSHA(latestCommitSHA);
    productSecurityInfo.setLastCommitDate(latestCommit.getCommitDate());
    productSecurityInfo.setDependabot(getDependabotAlerts(repo, organization, accessToken));
    productSecurityInfo.setSecretScanning(getNumberOfSecretScanningAlerts(repo, organization,
        accessToken));
    productSecurityInfo.setCodeScanning(getCodeScanningAlerts(repo, organization,
        accessToken));
    return productSecurityInfo;
  }

  public Dependabot getDependabotAlerts(GHRepository repo, GHOrganization organization,
      String accessToken) {
    return fetchAlerts(
        accessToken,
        String.format(GitHubConstants.Url.REPO_DEPENDABOT_ALERTS_OPEN, organization.getLogin(), repo.getName()),
        alerts -> {
          Dependabot dependabot = new Dependabot();
          Map<String, Integer> severityMap = new HashMap<>();
          for (Map<String, Object> alert : alerts) {
            Object advisoryObj = alert.get(GitHubConstants.Json.SEVERITY_ADVISORY);
            if (advisoryObj instanceof Map<?, ?> securityAdvisory) {
              String severity = (String) securityAdvisory.get(GitHubConstants.Json.SEVERITY);
              if (severity != null) {
                severityMap.put(severity, severityMap.getOrDefault(severity, 0) + 1);
              }
            }
          }
          dependabot.setAlerts(severityMap);
          return dependabot;
        },
        Dependabot::new
    );
  }

  public SecretScanning getNumberOfSecretScanningAlerts(GHRepository repo,
      GHOrganization organization, String accessToken) {
    return fetchAlerts(
        accessToken,
        String.format(GitHubConstants.Url.REPO_SECRET_SCANNING_ALERTS_OPEN, organization.getLogin(), repo.getName()),
        alerts -> {
          SecretScanning secretScanning = new SecretScanning();
          secretScanning.setNumberOfAlerts(alerts.size());
          return secretScanning;
        },
        SecretScanning::new
    );
  }

  public CodeScanning getCodeScanningAlerts(GHRepository repo,
      GHOrganization organization, String accessToken) {
    return fetchAlerts(
        accessToken,
        String.format(GitHubConstants.Url.REPO_CODE_SCANNING_ALERTS_OPEN, organization.getLogin(), repo.getName()),
        alerts -> {
          CodeScanning codeScanning = new CodeScanning();
          Map<String, Integer> codeScanningMap = new HashMap<>();
          for (Map<String, Object> alert : alerts) {
            Object ruleObj = alert.get(GitHubConstants.Json.RULE);
            if (ruleObj instanceof Map<?, ?> rule) {
              String severity = (String) rule.get(GitHubConstants.Json.SECURITY_SEVERITY_LEVEL);
              if (severity != null) {
                codeScanningMap.put(severity, codeScanningMap.getOrDefault(severity, 0) + 1);
              }
            }
          }
          codeScanning.setAlerts(codeScanningMap);
          return codeScanning;
        },
        CodeScanning::new
    );
  }

  private <T> T fetchAlerts(
      String accessToken,
      String url,
      Function<List<Map<String, Object>>, T> mapAlerts,
      Supplier<T> defaultInstanceSupplier
  ) {
    T instance = defaultInstanceSupplier.get();
    try {
      ResponseEntity<List<Map<String, Object>>> response = fetchApiResponseAsList(accessToken, url);
      instance = mapAlerts.apply(response.getBody() != null ? response.getBody() : List.of());
      setStatus(instance, com.axonivy.market.enums.AccessLevel.ENABLED);
    } catch (HttpClientErrorException.Forbidden e) {
      setStatus(instance, com.axonivy.market.enums.AccessLevel.DISABLED);
    } catch (HttpClientErrorException.NotFound e) {
      setStatus(instance, com.axonivy.market.enums.AccessLevel.NO_PERMISSION);
    }
    return instance;
  }

  private void setStatus(Object instance, com.axonivy.market.enums.AccessLevel status) {
    if (instance instanceof Dependabot dependabot) {
      dependabot.setStatus(status);
    } else if (instance instanceof SecretScanning secretScanning) {
      secretScanning.setStatus(status);
    } else if (instance instanceof CodeScanning codeScanning) {
      codeScanning.setStatus(status);
    }
  }

  public ResponseEntity<List<Map<String, Object>>> fetchApiResponseAsList(
      String accessToken,
      String url) throws RestClientException {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    return restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
    });
  }

  @Override
  public Page<GitHubReleaseModel> getGitHubReleaseModels(Product product,
      PagedIterable<GHRelease> ghReleasePagedIterable,
      Pageable pageable) throws IOException {
    List<GitHubReleaseModel> gitHubReleaseModels = new ArrayList<>();
    List<GHRelease> ghReleases = ghReleasePagedIterable.toList().stream().filter(ghRelease -> !ghRelease.isDraft()).toList();
    if (ObjectUtils.isNotEmpty(ghReleases)) {
      String latestGitHubReleaseName = this.getGitHubLatestReleaseByProductId(product.getRepositoryName()).getName();
      for (GHRelease ghRelease : ghReleases) {
        gitHubReleaseModels.add(this.toGitHubReleaseModel(ghRelease, product, latestGitHubReleaseName));
      }
    }

    return new PageImpl<>(gitHubReleaseModels, pageable, gitHubReleaseModels.size());
  }

  public GitHubReleaseModel toGitHubReleaseModel(GHRelease ghRelease, Product product, String latestGitHubReleaseName) throws IOException {
    GitHubReleaseModel gitHubReleaseModel = new GitHubReleaseModel();
    String modifiedBody = transformGithubReleaseBody(ghRelease.getBody(), product.getSourceUrl());
    gitHubReleaseModel.setBody(modifiedBody);
    gitHubReleaseModel.setName(ghRelease.getName());
    gitHubReleaseModel.setPublishedAt(ghRelease.getPublished_at());
    gitHubReleaseModel.setHtmlUrl(ghRelease.getHtmlUrl().toString());
    gitHubReleaseModel.add(GitHubUtils.createSelfLinkForGithubReleaseModel(product, ghRelease));
    gitHubReleaseModel.setLatestRelease(ghRelease.getName().equals(latestGitHubReleaseName));

    return gitHubReleaseModel;
  }

  @Override
  public GitHubReleaseModel getGitHubReleaseModelByProductIdAndReleaseId(Product product, Long releaseId) throws IOException {
    GHRelease ghRelease = this.getRepository(product.getRepositoryName()).getRelease(releaseId);
    GHRelease githubLatestRelease = getGitHubLatestReleaseByProductId(product.getRepositoryName());
    return this.toGitHubReleaseModel(ghRelease, product, githubLatestRelease.getName());
  }

  public String transformGithubReleaseBody(String githubReleaseBody, String productSourceUrl) {
    return githubReleaseBody.replaceAll(GITHUB_PULL_REQUEST_NUMBER_REGEX,
        productSourceUrl + GITHUB_PULL_REQUEST_LINK + FIRST_REGEX_CAPTURING_GROUP).replaceAll(GITHUB_USERNAME_REGEX, GITHUB_MAIN_LINK + FIRST_REGEX_CAPTURING_GROUP);
  }

  public GHRelease getGitHubLatestReleaseByProductId(String repositoryName) throws IOException {
    return this.getRepository(repositoryName).getLatestRelease();
  }

  @Override
  public GHWorkflowRun getLatestWorkflowRun(GHRepository repo, String workflowFileName) throws IOException {
    try {
      PagedIterable<GHWorkflowRun> runs = repo.getWorkflow(workflowFileName).listRuns().withPageSize(
          PAGE_SIZE_OF_WORKFLOW);
      for (GHWorkflowRun run : runs) {
        if (GHWorkflowRun.Status.COMPLETED == run.getStatus()) {
          return run;
        }
      }
      log.warn("No completed workflow runs found for '{}'", workflowFileName);
      return null;
    } catch (GHFileNotFoundException | NoSuchElementException e) {
      log.warn("Workflow file '{}' not found in repository '{}'", workflowFileName, repo.getFullName(), e);
      return null;
    }
  }

  @Override
  public GHArtifact getExportTestArtifact(GHWorkflowRun run) throws IOException {
    return run.listArtifacts().toList().stream()
        .filter(artifact -> CommonConstants.TEST_REPORT_FILE.equals(artifact.getName()))
        .findFirst()
        .orElse(null);
  }

  @Override
  public InputStream downloadArtifactZip(GHArtifact artifact) throws IOException {
    var outputStream = new ByteArrayOutputStream();
    artifact.download((InputStream inputStream) -> {
      try (inputStream; outputStream) {
        inputStream.transferTo(outputStream);
      } catch (IOException e) {
        log.error("Failed to download artifact zip: {}", artifact.getName(), e);
      }
      return null;
    });
    return new ByteArrayInputStream(outputStream.toByteArray());
  }
}
