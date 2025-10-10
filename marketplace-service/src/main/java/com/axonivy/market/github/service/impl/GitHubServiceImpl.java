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
import com.axonivy.market.util.ProductContentUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.*;
import org.springframework.cache.annotation.Cacheable;
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
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.CacheNameConstants.REPO_RELEASES;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static com.axonivy.market.enums.AccessLevel.ENABLED;
import static com.axonivy.market.enums.AccessLevel.DISABLED;
import static com.axonivy.market.enums.AccessLevel.NO_PERMISSION;

@Log4j2
@Service
public class GitHubServiceImpl implements GitHubService {


  public static final int PAGE_SIZE_OF_WORKFLOW = 10;
  private final RestTemplate restTemplate;
  private final GithubUserRepository githubUserRepository;
  private final GitHubProperty gitHubProperty;
  private final ThreadPoolTaskScheduler taskScheduler;

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
    try {
      return getGitHub().getRepository(repositoryPath);
    } catch (GHFileNotFoundException e) {
      log.error("Repository not found: {}", repositoryPath, e);
    } catch (IOException e) {
      log.error("Error fetching repository: {}", repositoryPath, e);
    }
    return null;
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

    var headers = new HttpHeaders();
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
      var githubUser = Optional.ofNullable(githubUserRepository.searchByGitHubId(String.valueOf(myself.getId())))
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
      log.error(e);
    }

    throw new UnauthorizedException(ErrorCode.GITHUB_USER_UNAUTHORIZED.getCode(),
        String.format(ErrorMessageConstants.INVALID_USER_ERROR, ErrorCode.GITHUB_USER_UNAUTHORIZED.getHelpText(), team,
            organization));
  }

  @Override
  public List<ProductSecurityInfo> getSecurityDetailsForAllProducts(String accessToken, String orgName) {
    try {
      var gitHub = getGitHub(accessToken);
      GHOrganization organization = gitHub.getOrganization(orgName);

      List<CompletableFuture<ProductSecurityInfo>> futures = organization.listRepositories().toList().stream()
          .map(repo -> CompletableFuture.supplyAsync(() -> fetchSecurityInfoSafe(repo, organization, accessToken),
              taskScheduler.getScheduledExecutor())).toList();

      return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
          .thenApply(v -> futures.stream().map(CompletableFuture::join).sorted(
              Comparator.comparing(ProductSecurityInfo::getRepoName)).collect(Collectors.toList())).join();
    } catch (IOException e) {
      log.error(e);
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

    return hashSetTeam.stream().anyMatch((GHTeam team) -> teamName.equals(team.getName()));
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
    var productSecurityInfo = new ProductSecurityInfo();
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

  private static Map<String, Integer> countAlertsBySeverity(List<Map<String, Object>> alerts,
      String advisoryKey,
      String severityKey) {
    return alerts.stream()
        .map(alert -> alert.get(advisoryKey))
        .filter(Map.class::isInstance)
        .map(obj -> (String) ((Map<?, ?>) obj).get(severityKey))
        .filter(Objects::nonNull)
        .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(s -> 1)));
  }

  private static Dependabot mapToDependabot(List<Map<String, Object>> alerts) {
    var dependabot = new Dependabot();
    dependabot.setAlerts(countAlertsBySeverity(
        alerts,
        GitHubConstants.Json.SEVERITY_ADVISORY,
        GitHubConstants.Json.SEVERITY
    ));
    return dependabot;
  }

  private static CodeScanning mapToCodeScanning(List<Map<String, Object>> alerts) {
    var codeScanning = new CodeScanning();
    codeScanning.setAlerts(countAlertsBySeverity(
        alerts,
        GitHubConstants.Json.RULE,
        GitHubConstants.Json.SECURITY_SEVERITY_LEVEL
    ));
    return codeScanning;
  }

  public Dependabot getDependabotAlerts(GHRepository repo, GHPerson organization,
      String accessToken) {
    return fetchAlerts(
        accessToken,
        String.format(GitHubConstants.Url.REPO_DEPENDABOT_ALERTS_OPEN, organization.getLogin(), repo.getName()),
        GitHubServiceImpl::mapToDependabot,
        Dependabot::new
    );
  }

  public SecretScanning getNumberOfSecretScanningAlerts(GHRepository repo,
      GHPerson organization, String accessToken) {
    return fetchAlerts(
        accessToken,
        String.format(GitHubConstants.Url.REPO_SECRET_SCANNING_ALERTS_OPEN, organization.getLogin(), repo.getName()),
        (List<Map<String, Object>> alerts) -> {
          var secretScanning = new SecretScanning();
          secretScanning.setNumberOfAlerts(alerts.size());
          return secretScanning;
        },
        SecretScanning::new
    );
  }

  public CodeScanning getCodeScanningAlerts(GHRepository repo,
      GHPerson organization, String accessToken) {
    return fetchAlerts(
        accessToken,
        String.format(GitHubConstants.Url.REPO_CODE_SCANNING_ALERTS_OPEN, organization.getLogin(), repo.getName()),
        GitHubServiceImpl::mapToCodeScanning,
        CodeScanning::new
    );
  }

  private <T> T fetchAlerts(
      String accessToken,
      String url,
      Function<List<Map<String, Object>>, T> mapAlerts,
      Supplier<T> defaultInstanceSupplier
  ) {
    var instance = defaultInstanceSupplier.get();
    try {
      ResponseEntity<List<Map<String, Object>>> response = fetchApiResponseAsList(accessToken, url);
      if (response.getBody() != null) {
        instance = mapAlerts.apply(response.getBody());
      } else {
        instance = mapAlerts.apply(List.of());
      }
      setStatus(instance, ENABLED);
    } catch (HttpClientErrorException.Forbidden e) {
      log.error("Access forbidden: ", e);
      setStatus(instance, DISABLED);
    } catch (HttpClientErrorException.NotFound e) {
      log.error("Alerts not found: ", e);
      setStatus(instance, NO_PERMISSION);
    }
    return instance;
  }

  private static void setStatus(Object instance, com.axonivy.market.enums.AccessLevel status) {
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
    var headers = new HttpHeaders();
    headers.setBearerAuth(accessToken);
    HttpEntity<String> entity = new HttpEntity<>(headers);

    return restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {
    });
  }

  @Override
  public Page<GitHubReleaseModel> getGitHubReleaseModels(List<GHRelease> ghReleases,
      Pageable pageable, String productId, String productRepoName, String productSourceUrl) throws IOException {
    List<GitHubReleaseModel> gitHubReleaseModels = new ArrayList<>();
    if (ObjectUtils.isNotEmpty(ghReleases)) {
      String latestGitHubReleaseName = this.getGitHubLatestReleaseByProductId(productRepoName).getName();
      for (GHRelease ghRelease : ProductContentUtils.extractReleasesPage(ghReleases, pageable)) {
        gitHubReleaseModels.add(this.toGitHubReleaseModel(ghRelease, productSourceUrl, productId,
            StringUtils.equals(latestGitHubReleaseName, ghRelease.getName())));
      }
    }
    return new PageImpl<>(gitHubReleaseModels, pageable, ghReleases.size());
  }

  @Cacheable(value = REPO_RELEASES, key = "{#productId}")
  @Override
  public List<GHRelease> getRepoOfficialReleases(String repoName, String productId) throws IOException {
    List<GHRelease> ghReleases = new ArrayList<>();
    var ghRepo = getRepository(repoName);
    if (null != ghRepo) {
      ghRepo.listReleases().forEach((GHRelease release) -> {
        if (!release.isDraft()) {
          ghReleases.add(release);
        }
      });
    }
    return ghReleases;
  }

  public GitHubReleaseModel toGitHubReleaseModel(GHRelease ghRelease, String productSourceUrl, String productId,
      Boolean isLatestGitHubReleaseName) throws IOException {
    var gitHubReleaseModel = new GitHubReleaseModel();
    var modifiedBody = ProductContentUtils.transformGithubReleaseBody(ghRelease.getBody(), productSourceUrl);
    gitHubReleaseModel.setBody(modifiedBody);
    gitHubReleaseModel.setName(ghRelease.getName());
    gitHubReleaseModel.setPublishedAt(ghRelease.getPublished_at());
    gitHubReleaseModel.setHtmlUrl(ghRelease.getHtmlUrl().toString());
    gitHubReleaseModel.add(GitHubUtils.createSelfLinkForGithubReleaseModel(productId, ghRelease.getId()));
    gitHubReleaseModel.setLatestRelease(isLatestGitHubReleaseName);
    return gitHubReleaseModel;
  }

  @Override
  public GitHubReleaseModel getGitHubReleaseModelByProductIdAndReleaseId(Product product,
      long releaseId) throws IOException {
    var ghRelease = this.getRepository(product.getRepositoryName()).getRelease(releaseId);
    GHRelease githubLatestRelease = getGitHubLatestReleaseByProductId(product.getRepositoryName());
    return this.toGitHubReleaseModel(ghRelease, product.getSourceUrl(), product.getId(),
        StringUtils.equals(githubLatestRelease.getName(), ghRelease.getName()));
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
