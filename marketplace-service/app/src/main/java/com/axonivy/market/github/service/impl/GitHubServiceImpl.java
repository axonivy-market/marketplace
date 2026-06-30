package com.axonivy.market.github.service.impl;

import com.axonivy.market.aop.annotation.TrackSyncTaskExecution;
import com.axonivy.market.config.OkHttpClientBuilder;
import com.axonivy.market.config.RestClientBuilder;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.ErrorMessageConstants;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.criteria.ProductSecurityCriteria;
import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.entity.ProductSecurityInfo;
import com.axonivy.market.enums.AccessLevel;
import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.enums.PullRequestAction;
import com.axonivy.market.enums.SyncTaskType;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.exceptions.model.UnarchiveFailedException;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.github.model.CodeScanning;
import com.axonivy.market.github.model.Dependabot;
import com.axonivy.market.github.model.GitHubAccessTokenResponse;
import com.axonivy.market.github.model.SecretScanning;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.AlternativeExtensionData;
import com.axonivy.market.model.GitHubReleaseModel;
import com.axonivy.market.model.UserInfo;
import com.axonivy.market.repository.GithubUserRepository;
import com.axonivy.market.repository.ProductSecurityInfoRepository;
import com.axonivy.market.service.AppSettingService;
import com.axonivy.market.util.MdcContextUtils;
import com.axonivy.market.util.MultiTaskUtils;
import com.axonivy.market.util.ProductContentUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.kohsuke.github.*;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.axonivy.market.constants.CacheNameConstants.REPO_RELEASES;
import static com.axonivy.market.constants.GitHubConstants.*;
import static com.axonivy.market.enums.AccessLevel.*;
import static com.axonivy.market.enums.PullRequestAction.*;
import static org.apache.commons.lang3.StringUtils.*;


@Log4j2
@Service
@RequiredArgsConstructor
public class GitHubServiceImpl implements GitHubService {
  public static final int PAGE_SIZE_OF_WORKFLOW = 10;
  private static final String CRLF = CR + LF;
  private static final int MAX_CONCURRENCY = 50;
  private static final String ALTERNATIVE_EXTENSION_FORMAT = """
   > **Recommended alternative:** [%s](%s)
   """;

  private static final String NO_ANALYSIS_FOUND = "no analysis found";
  private static final String MUST_BE_ENABLED = "must be enabled";
  private static final String MULTILINE_START = "(?m)^";
  /**
   * Regex suffix that matches the remainder of a blockquote block:
   * captures the rest of the first line, then continues matching consecutive lines starting with ">".
   */
  private static final String BLOCKQUOTE_LINES_PATTERN = "[^\\n]*\\n(>[^\\n]*\\n)*";
  /**
   * Same as {@link #BLOCKQUOTE_LINES_PATTERN} but also consumes any trailing whitespace/blank lines
   * after the block, useful for clean removal without leaving extra empty lines.
   */
  private static final String BLOCKQUOTE_LINES_WITH_TRAILING_WHITESPACE_PATTERN = "[^\\n]*\\n(>[^\\n]*\\n)*\\s*";
  private static final Pattern FORMAT_SPECIFIER_PATTERN = Pattern.compile("%s");

  private final RestClientBuilder restClientBuilder;
  private final GithubUserRepository githubUserRepository;
  private final AppSettingService appSettingService;
  private final ProductSecurityInfoRepository productSecurityInfoRepository;
  private final OkHttpClientBuilder okHttpClientBuilder;
  private final MultiTaskUtils multiTaskUtils;

  @Override
  public GitHub getGitHub() throws IOException {
    return buildGitHub(getConfiguredToken());
  }

  @Override
  public GitHub getGitHub(String accessToken) throws IOException {
    return buildGitHub(accessToken);
  }

  public GitHub buildGitHub(String accessToken) throws IOException {
    var client = okHttpClientBuilder.build();
    var gitHubConnector = new OkHttpGitHubConnector(client);
    return new GitHubBuilder().withOAuthToken(accessToken).withConnector(gitHubConnector).build();
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
  public GHRepository getRepository(String repositoryPath) {
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
  public GitHubAccessTokenResponse getAccessToken(
      String code) throws Oauth2ExchangeCodeException, MissingHeaderException {
    // Read OAuth client id/secret from DB-backed AppSetting; throw if missing
    var clientId = appSettingService.getStringValueByKey(AppSettingKey.GITHUB_OAUTH_CLIENT_ID);
    var clientSecret = appSettingService.getStringValueByKey(AppSettingKey.GITHUB_OAUTH_CLIENT_SECRET);

    if (StringUtils.isAnyBlank(clientId, clientSecret)) {
      throw new MissingHeaderException();
    }

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add(Json.CLIENT_ID, clientId);
    params.add(Json.CLIENT_SECRET, clientSecret);
    params.add(Json.CODE, code);

    GitHubAccessTokenResponse response = restClientBuilder.build().post()
        .uri(GITHUB_GET_ACCESS_TOKEN_URL)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .accept(MediaType.APPLICATION_JSON)
        .body(params)
        .retrieve()
        .body(GitHubAccessTokenResponse.class);

    if (response != null && response.getError() != null && !response.getError().isBlank()) {
      log.error(String.format(ErrorMessageConstants.CURRENT_CLIENT_ID_MISMATCH_MESSAGE, code, clientId));
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
      githubUser.setProvider(GITHUB_PROVIDER_NAME);
      githubUserRepository.save(githubUser);
      return githubUser;
    } catch (IOException e) {
      log.error("GitHub user fetch failed", e);
      throw new NotFoundException(ErrorCode.GITHUB_USER_NOT_FOUND, "Failed to fetch user details from GitHub");
    }
  }

  @Override
  public UserInfo validateUserInOrganizationAndTeam(String accessToken, String organization,
      String team) throws UnauthorizedException {
    try {
      var gitHub = getGitHub(accessToken);
      GHMyself myself = gitHub.getMyself();
      if (isUserInOrganizationAndTeam(gitHub, organization, team)) {
        var userInfo = new UserInfo();
        userInfo.setGitHubId(String.valueOf(myself.getId()));
        userInfo.setName(myself.getName());
        userInfo.setUsername(myself.getLogin());
        userInfo.setAvatarUrl(myself.getAvatarUrl());
        userInfo.setProvider(GITHUB_PROVIDER_NAME);
        userInfo.setUrl(String.valueOf(myself.getHtmlUrl()));

        return userInfo;
      }
    } catch (IOException e) {
      log.error(e);
    }

    throw new UnauthorizedException(ErrorCode.GITHUB_USER_UNAUTHORIZED.getCode(),
        String.format(ErrorMessageConstants.INVALID_USER_ERROR, ErrorCode.GITHUB_USER_UNAUTHORIZED.getHelpText(), team,
            organization));
  }

  @Override
  public Page<ProductSecurityInfo> searchSecurityDetails(ProductSecurityCriteria criteria, Pageable pageable) {
    return productSecurityInfoRepository.searchProductSecurityAndSorting(criteria, pageable);
  }

  @Override
  @TrackSyncTaskExecution(SyncTaskType.SYNC_GITHUB_SECURITY_MONITOR)
  public List<ProductSecurityInfo> syncSecurityDetailsForProduct() throws IOException {
    var gitHub = getGitHub(getConfiguredToken());
    GHOrganization organization = gitHub.getOrganization(AXONIVY_MARKET_ORGANIZATION_NAME);

    String token = getConfiguredToken();
    Function<GHRepository, ProductSecurityInfo> fetchInfoWithContext =
        repo -> fetchSecurityInfoSafe(repo, organization, token);

    List<ProductSecurityInfo> productSecurityInfos = multiTaskUtils.parallelProcessWithLimit(
        organization.listRepositories().toList().stream().filter(repo -> !repo.isArchived()).toList(),
        MdcContextUtils.wrapMdcContext(fetchInfoWithContext),
        MAX_CONCURRENCY);

    List<ProductSecurityInfo> validSecurityInfos = productSecurityInfos.stream()
        .filter(info -> isNotBlank(info.getRepoName()))
        .toList();

    List<ProductSecurityInfo> syncedSecurityRepos = productSecurityInfoRepository.saveAll(validSecurityInfos);
    log.info("Synced security details for {} repositories", syncedSecurityRepos.size());

    List<String> syncedRepoNames = validSecurityInfos.stream()
        .map(ProductSecurityInfo::getRepoName)
        .toList();
    if (!CollectionUtils.isEmpty(syncedRepoNames)) {
      productSecurityInfoRepository.deleteByRepoNameNotIn(syncedRepoNames);
    }
    return syncedSecurityRepos;
  }

  public boolean isUserInOrganizationAndTeam(GitHub gitHub, String organization, String teamName) throws IOException {
    if (gitHub == null) {
      return false;
    }
    GHMyself myself = gitHub.getMyself();
    if (myself == null || StringUtils.isBlank(myself.getLogin())) {
      return false;
    }
    return isUserInOrganizationAndTeam(myself.getLogin(), organization, teamName);
  }

  private boolean isUserInOrganizationAndTeam(String username, String organization, String teamIdentifier)
      throws IOException {
    GitHub serviceGitHub = getGitHub();
    GHOrganization ghOrganization = serviceGitHub.getOrganization(organization);
    GHUser user = serviceGitHub.getUser(username);
    if (ghOrganization == null || user == null || !ghOrganization.hasMember(user)) {
      return false;
    }

    GHTeam team = findTeam(ghOrganization, teamIdentifier);
    return team != null && team.hasMember(user);
  }

  private GHTeam findTeam(GHOrganization organization, String teamIdentifier) throws IOException {
    GHTeam team = organization.getTeamBySlug(teamIdentifier);
    if (team != null) {
      return team;
    }
    return organization.getTeamByName(teamIdentifier);
  }

  public ProductSecurityInfo fetchSecurityInfoSafe(GHRepository repo, GHOrganization organization,
      String accessToken) {
    try {
      log.info("fetching security info for repo: {}", repo.getName());
      return fetchSecurityInfo(repo, organization, accessToken);
    } catch (IOException e) {
      log.error("Error fetching security info for repo: {}", repo.getName(), e);
      return new ProductSecurityInfo();
    }
  }

  private ProductSecurityInfo fetchSecurityInfo(GHRepository repo, GHOrganization organization,
      String accessToken) throws IOException {
    String defaultBranch = repo.getDefaultBranch();
    var branch = repo.getBranch(defaultBranch);
    String latestCommitSHA = branch.getSHA1();
    GHCommit latestCommit = repo.getCommit(latestCommitSHA);

    return ProductSecurityInfo.builder()
        .repoName(repo.getName())
        .visibility(repo.getVisibility().toString())
        .branchProtectionEnabled(branch.isProtected())
        .latestCommitSHA(latestCommitSHA)
        .lastCommitDate(latestCommit.getCommitDate())
        .dependabot(getDependabotAlerts(repo, organization, accessToken))
        .secretScanning(getNumberOfSecretScanningAlerts(repo, organization, accessToken))
        .codeScanning(getCodeScanningAlerts(repo, organization, accessToken))
        .build();
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
        Json.SEVERITY_ADVISORY,
        Json.SEVERITY
    ));
    return dependabot;
  }

  private static CodeScanning mapToCodeScanning(List<Map<String, Object>> alerts) {
    var codeScanning = new CodeScanning();
    codeScanning.setAlerts(countAlertsBySeverity(
        alerts,
        Json.RULE,
        Json.SECURITY_SEVERITY_LEVEL
    ));
    return codeScanning;
  }

  public Dependabot getDependabotAlerts(GHRepository repo, GHPerson organization,
      String accessToken) {
    return fetchAlerts(
        accessToken,
        String.format(Url.REPO_DEPENDABOT_ALERTS_OPEN, organization.getLogin(), repo.getName()),
        GitHubServiceImpl::mapToDependabot,
        Dependabot::new
    );
  }

  public SecretScanning getNumberOfSecretScanningAlerts(GHRepository repo,
      GHPerson organization, String accessToken) {
    return fetchAlerts(
        accessToken,
        String.format(Url.REPO_SECRET_SCANNING_ALERTS_OPEN, organization.getLogin(), repo.getName()),
        (List<Map<String, Object>> alerts) -> {
          var secretScanning = new SecretScanning();
          secretScanning.setNumberOfSecretScanningAlerts(alerts.size());
          return secretScanning;
        },
        SecretScanning::new
    );
  }

  public CodeScanning getCodeScanningAlerts(GHRepository repo,
      GHPerson organization, String accessToken) {
    return fetchAlerts(
        accessToken,
        String.format(Url.REPO_CODE_SCANNING_ALERTS_OPEN, organization.getLogin(), repo.getName()),
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
      instance = mapAlerts.apply(response.getBody() != null ? response.getBody() : List.of());
      setStatus(instance, ENABLED);
    } catch (HttpStatusCodeException e) {
      String body = e.getResponseBodyAsString().toLowerCase(Locale.ROOT);
      AccessLevel status = resolveErrorStatus(e, body);
      log.error("Failed to fetch alerts URL: {} with the error: {}", url, e.getMessage());
      setStatus(instance, status);
    }
    return instance;
  }

  private AccessLevel resolveErrorStatus(HttpStatusCodeException e, String body) {
    HttpStatusCode statusCode = e.getStatusCode();
    switch (statusCode) {
      case HttpStatus.NOT_FOUND -> {
        if (body.contains(NO_ANALYSIS_FOUND)) return NOT_SUPPORTED;
        return NO_PERMISSION;
      }
      case HttpStatus.FORBIDDEN -> {
        return body.contains(MUST_BE_ENABLED) ? DISABLED : NO_PERMISSION;
      }
      case HttpStatus.SERVICE_UNAVAILABLE -> {
        return DISABLED;
      }
      default -> {return NO_PERMISSION;}
    }
  }

  private static void setStatus(Object instance, AccessLevel status) {
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
    return restClientBuilder.build().get()
        .uri(url)
        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + accessToken)
        .retrieve()
        .toEntity(new ParameterizedTypeReference<>() {
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
            Strings.CS.equals(latestGitHubReleaseName, ghRelease.getName())));
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
        Strings.CS.equals(githubLatestRelease.getName(), ghRelease.getName()));
  }

  public GHRelease getGitHubLatestReleaseByProductId(String repositoryName) throws IOException {
    return this.getRepository(repositoryName).getLatestRelease();
  }

  @Override
  public GHWorkflowRun getLatestWorkflowRun(GHRepository repo, String workflowFileName) throws IOException {
    try {
      GHWorkflow workflow = repo.getWorkflow(workflowFileName);
      var runs = Optional.ofNullable(repo.queryWorkflowRuns())
          .map(query -> query.branch(DEFAULT_BRANCH).status(GHWorkflowRun.Status.COMPLETED).list())
          .orElseGet(workflow::listRuns)
          .withPageSize(PAGE_SIZE_OF_WORKFLOW)
          .toList();
      for (GHWorkflowRun run : runs) {
        if (GHWorkflowRun.Status.COMPLETED == run.getStatus() && workflow.getId() == run.getWorkflowId()) {
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

  @Override
  public GHPullRequest updateReadmeForSuccessorNotes(
      String repoPath, PullRequestAction action, AlternativeExtensionData extensionData) throws IOException {
    String accessToken = getConfiguredToken();
    GitHub gitHub = getGitHub(accessToken);
    GHRepository repository = gitHub.getRepository(repoPath);

    if (repository.isArchived()) {
      unArchivedTheRepository(repoPath);
    }

    String baseBranch = repository.getDefaultBranch();
    GitHubUnsupportedText config = getGithubUnsupportedTextConfig();
    GHContent readme = repository.getFileContent(README_FILE_PATH, baseBranch);
    String currentReadmeContent = getReadmeContent(readme);
    PullRequestData pullRequestData = buildPullRequestData(action, currentReadmeContent, config, extensionData);

    boolean isSameContent = Objects.equals(currentReadmeContent, pullRequestData.updatedReadmeContent);
    if (isSameContent) {
      log.error("No Need to update readme content for deprecation because the content is the same");
      removeBranchIfExistsWhenRemoveDeprecation(repository, HEADS_PREFIX + config.unsupportedBranchName, action);
      return null;
    }

    try {
      return getPullRequestFromExistingBranch(repository, baseBranch, pullRequestData, readme);
    } catch (GHFileNotFoundException e) {
      log.info("There is no duplicated branch existing, create new branch");
      String branchSha = repository.getRef(HEADS_PREFIX + baseBranch).getObject().getSha();
      repository.createRef(REFS_HEADS_PREFIX + config.unsupportedBranchName(), branchSha);
    }
    readme.update(pullRequestData.updatedReadmeContent, config.deprecatedMessage(), config.unsupportedBranchName());
    return generatePullRequest(repository, baseBranch, pullRequestData);
  }

  @Override
  public void archiveTheRepository(String repoPath) throws IOException {
    GHRepository ghRepository = getRepository(repoPath);
    if (ghRepository != null && !ghRepository.isArchived()) {
      ghRepository.archive();
      log.info("Repository '{}' has been archived.", repoPath);
    }
  }

  @Override
  public boolean hasDeprecationWarningInReadme(String repoPath) throws IOException {
    GHRepository repository = getRepository(repoPath);
    if (repository == null) {
      return false;
    }
    GHContent readme = repository.getFileContent(README_FILE_PATH, repository.getDefaultBranch());
    String readmeContent = getReadmeContent(readme);
    GitHubUnsupportedText config = getGithubUnsupportedTextConfig();
    // Check if the README contains the deprecation notice prefix (without the version placeholder)
    String noticePrefix = FORMAT_SPECIFIER_PATTERN.split(config.unsupportedNotice())[0];
    return readmeContent.contains(noticePrefix.trim());
  }

  @Override
  public void unArchivedTheRepository(String repoPath) {
    String url = Url.REPOS_BASE_URL + repoPath;
    try {
      ResponseEntity<Void> response = restClientBuilder.build().patch()
          .uri(url)
          .header(HttpHeaders.AUTHORIZATION,
              BEARER_PREFIX + appSettingService.getStringValueByKey(AppSettingKey.GITHUB_TOKEN))
          .body("{\"archived\": false}")
          .retrieve()
          .toBodilessEntity();

      if (!response.getStatusCode().is2xxSuccessful()) {
        throw new UnarchiveFailedException(ErrorCode.UNARCHIVE_FAILED,
            String.format("Failed to unarchive repository '%s': %s", repoPath, response.getStatusCode()));
      }
      log.info("Repository '{}' has been unarchived successfully.", repoPath);
    } catch (RestClientException e) {
      throw new UnarchiveFailedException(ErrorCode.UNARCHIVE_FAILED,
          String.format("Error unarchiving repository '%s': %s", repoPath, e.getMessage()));
    }
  }

  private GHPullRequest getPullRequestFromExistingBranch(GHRepository repository, String baseBranch,
      PullRequestData pullRequestData, GHContent readme) throws IOException {
    String unsupportedBranchName = pullRequestData.unsupportedBranchName;
    repository.getRef(HEADS_PREFIX + unsupportedBranchName);
    log.info("Branch exists, reusing: {}", unsupportedBranchName);

    GHPullRequest existingPR = repository.queryPullRequests()
        .base(baseBranch).head(unsupportedBranchName).state(GHIssueState.OPEN)
        .list().toList()
        .stream().findFirst().orElse(null);

    if (existingPR != null) {
      // If the existing PR has a different title (e.g., REMOVE vs ADD), update it with new content
      if (!existingPR.getTitle().equals(pullRequestData.title)) {
        log.info("Existing PR '{}' has different action, updating with new content", existingPR.getHtmlUrl());
        // Fetch README from the unsupported branch (not base) to get correct SHA for update
        GHContent branchReadme = repository.getFileContent(README_FILE_PATH, unsupportedBranchName);
        branchReadme.update(pullRequestData.updatedReadmeContent, pullRequestData.title, unsupportedBranchName);
        existingPR.setTitle(pullRequestData.title);
        existingPR.setBody(pullRequestData.body);
      } else {
        log.info("There was existing pull request '{}'", existingPR.getHtmlUrl().toString());
      }
      return existingPR;
    }
    GHCompare compare = repository.getCompare(baseBranch, unsupportedBranchName);
    List<GHCompare.Status> githubStatus = List.of(GHCompare.Status.identical, GHCompare.Status.behind);
    if (githubStatus.stream().anyMatch(status -> status == compare.getStatus())) {
      repository.getRef(HEADS_PREFIX + unsupportedBranchName).delete();
      String branchSha = repository.getRef(HEADS_PREFIX + baseBranch).getObject().getSha();
      repository.createRef(REFS_HEADS_PREFIX + unsupportedBranchName, branchSha);
      readme.update(pullRequestData.updatedReadmeContent, pullRequestData.body, unsupportedBranchName);
    }
    return generatePullRequest(repository, baseBranch, pullRequestData);
  }

  private void removeBranchIfExistsWhenRemoveDeprecation(GHRepository repository, String headBranchName,
      PullRequestAction action) throws IOException {
    if (action != REMOVE || repository.isArchived()) {
      return;
    }

    try {
      repository.getRef(headBranchName).delete();
    } catch (GHFileNotFoundException e) {
      log.info("Branch '{}' does not exist, skipping deletion", headBranchName);
    }
  }

  private String getReadmeContent(GHContent readme) throws IOException {
    try (InputStream inputStream = readme.read()) {
      return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private GHPullRequest generatePullRequest(GHRepository repository, String baseBranch,
      PullRequestData pullRequestData) throws IOException {
    return repository.createPullRequest(
        pullRequestData.title,
        pullRequestData.unsupportedBranchName,
        baseBranch,
        pullRequestData.body);
  }

  /**
   * Applies the requested README unsupported-notice action.
   * The method is intentionally idempotent: if the target content is already in the desired state,
   * helper methods return the original text unchanged.
   */
  private String updateUnsupportedNotice(String readmeContent, PullRequestAction action, String notice) {
    return switch (action) {
      case ADD -> addUnsupportedNotice(readmeContent, notice);
      case REMOVE -> removeUnsupportedNotice(readmeContent, notice);
    };
  }

  /**
   * Inserts the unsupported notice right below the first Markdown heading.
   * - Keeps original newline style (CRLF/LF).
   * - Returns original content when the notice already exists.
   * - Fails fast when README has no heading line.
   */
  private String addUnsupportedNotice(String readmeContent, String notice) {
    String noticePrefix = FORMAT_SPECIFIER_PATTERN.split(
        getGithubUnsupportedTextConfig().unsupportedNotice())[0].trim();

    // If there's already a deprecation block, check if it's identical to the new notice
    if (readmeContent.contains(noticePrefix)) {
      // Case: When we deprecate a repository at 2nd times, but not yet to remove deprecated items from PR
      String existingBlock = extractExistingDeprecationBlock(readmeContent, noticePrefix);
      if (existingBlock != null && existingBlock.trim().equals(notice.trim())) {
        return readmeContent;
      }
      // Different notice exists — remove the old one first
      readmeContent = removeExistingDeprecationBlock(readmeContent, noticePrefix);
    }

    String lineSeparator = readmeContent.contains(CRLF) ? CRLF : LF;
    Matcher matcher = Pattern.compile("^#[^\\r\\n]*", Pattern.MULTILINE).matcher(readmeContent);
    if (matcher.find()) {
      String heading = matcher.group();
      String replacement = heading + lineSeparator + lineSeparator + notice.trim();
      return matcher.replaceFirst(Matcher.quoteReplacement(replacement));
    }
    throw new IllegalArgumentException("README.md must contain a heading line starting with '#'");
  }

  /**
   * Extracts the existing deprecation block from the README content.
   * Returns the matched block text, or null if not found.
   */
  private String extractExistingDeprecationBlock(String readmeContent, String noticePrefix) {
    String regex = MULTILINE_START + Pattern.quote(noticePrefix) + BLOCKQUOTE_LINES_PATTERN;
    Matcher matcher = Pattern.compile(regex).matcher(readmeContent);
    return matcher.find() ? matcher.group() : null;
  }

  /**
   * Removes an existing deprecation block from the README content using regex.
   * Matches from the notice prefix through all consecutive blockquote lines.
   */
  private String removeExistingDeprecationBlock(String readmeContent, String noticePrefix) {
    String regex = MULTILINE_START + Pattern.quote(noticePrefix) + BLOCKQUOTE_LINES_WITH_TRAILING_WHITESPACE_PATTERN;
    return readmeContent.replaceFirst(regex, EMPTY);
  }

  /**
   * Removes the unsupported notice when present.
   * Uses pattern matching to handle cases where the URL in the README
   * may differ from the one constructed at removal time.
   * Returns original content when the notice does not exist.
   */
  private String removeUnsupportedNotice(String readmeContent, String notice) {
    // If exact match fails, use regex to match the deprecation block structure
    // Match from "> [!CAUTION]" through all consecutive blockquote lines (starting with ">")
    // including the optional "Recommended alternative" line with any URL
    String noticePrefix = FORMAT_SPECIFIER_PATTERN.split(
        getGithubUnsupportedTextConfig().unsupportedNotice())[0].trim();
    if (!readmeContent.contains(noticePrefix)) {
      return readmeContent;
    }

    // Build a regex that matches the full deprecation block:
    String regex = MULTILINE_START + Pattern.quote(noticePrefix) + BLOCKQUOTE_LINES_WITH_TRAILING_WHITESPACE_PATTERN;
    return readmeContent.replaceFirst(regex, EMPTY);
  }

  /**
   * Read the GitHub token from DB-backed AppSetting.
   */
  private String getConfiguredToken() {
    return appSettingService.getStringValueByKey(AppSettingKey.GITHUB_TOKEN);
  }

  /**
   * Loads all GitHub unsupported-notice related texts from the JSON resource file.
   * The returned object provides branch name, PR titles, bodies, and the notice text itself.
   */
  private GitHubUnsupportedText getGithubUnsupportedTextConfig() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    try (InputStream inputStream = classLoader.getResourceAsStream(GITHUB_TEXTS_RESOURCE_PATH)) {
      if (inputStream == null) {
        throw new IllegalStateException("Missing resource: " + GITHUB_TEXTS_RESOURCE_PATH);
      }
      return new ObjectMapper().readValue(inputStream, GitHubUnsupportedText.class);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load unsupported notice text configuration", e);
    }
  }

  private record PullRequestData(String body, String title, String updatedReadmeContent, String unsupportedBranchName) {
  }

  private PullRequestData buildPullRequestData(PullRequestAction action, String currentReadmeContent,
      GitHubUnsupportedText config, AlternativeExtensionData extensionData) {

    String unsupportedNotices = String.format(config.unsupportedNotice(), extensionData.getDeprecatedVersionFrom());
    if (StringUtils.isNoneBlank(extensionData.getSuccessorUrl(), extensionData.getAlternativeExtension())) {
      unsupportedNotices += String.format(ALTERNATIVE_EXTENSION_FORMAT,
          extensionData.getAlternativeExtension(), extensionData.getSuccessorUrl());
    }

    String updatedContent = updateUnsupportedNotice(currentReadmeContent, action, unsupportedNotices);
    return switch (action) {
      case ADD -> new PullRequestData(
          config.addUnsupportedNoticePrBody(),
          config.deprecatedMessage(),
          updatedContent,
          config.unsupportedBranchName());
      case REMOVE -> new PullRequestData(
              config.removeUnsupportedNoticePrBody(),
              config.removeUnsupportedNoticeMessage(),
              updatedContent,
              config.unsupportedBranchName());
    };
  }

  private record GitHubUnsupportedText(
      String deprecatedMessage,
      String removeUnsupportedNoticeMessage,
      String unsupportedBranchName,
      String removeUnsupportedNoticePrBody,
      String addUnsupportedNoticePrBody,
      String unsupportedNotice
  ) {
  }

}
