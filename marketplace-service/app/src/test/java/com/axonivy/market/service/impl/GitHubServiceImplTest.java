package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.config.OkHttpClientBuilder;
import com.axonivy.market.config.RestClientBuilder;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.core.constants.CoreCommonConstants;
import com.axonivy.market.core.entity.Product;
import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.criteria.ProductSecurityCriteria;
import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.enums.AccessLevel;
import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.enums.PullRequestAction;
import com.axonivy.market.exceptions.model.Oauth2ExchangeCodeException;
import com.axonivy.market.exceptions.model.UnarchiveFailedException;
import com.axonivy.market.exceptions.model.UnauthorizedException;
import com.axonivy.market.github.model.CodeScanning;
import com.axonivy.market.github.model.Dependabot;
import com.axonivy.market.github.model.GitHubAccessTokenResponse;
import com.axonivy.market.entity.ProductSecurityInfo;
import com.axonivy.market.github.model.SecretScanning;
import com.axonivy.market.github.service.impl.GitHubServiceImpl;
import com.axonivy.market.model.AlternativeExtensionData;
import com.axonivy.market.model.GitHubReleaseModel;
import com.axonivy.market.repository.GithubUserRepository;
import com.axonivy.market.repository.ProductSecurityInfoRepository;
import com.axonivy.market.service.AppSettingService;
import com.axonivy.market.util.MultiTaskUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import com.axonivy.market.util.ProductContentUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.*;
import org.kohsuke.github.function.InputStreamFunction;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.MultiValueMap;
import okhttp3.OkHttpClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

import static com.axonivy.market.constants.GitHubConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GitHubServiceImplTest extends BaseSetup {

  private static final String BASE_BRANCH = "master";
  private static final String UNSUPPORTED_BRANCH_NAME_FIXTURE = "feature/update-deprecated-for-readme";
  private static final String UNSUPPORTED_NOTICE_FIXTURE = """
      > [!CAUTION]
      > ## Deprecated
      > These connectors are deprecated and will no longer be maintained or supported. It will be removed in Release 10.0.0.
      >
      > **Recommended alternative:** [successor-extension](https://market.axonivy.com/successor)""".stripIndent().trim();
  private static final AlternativeExtensionData EXTENSION_DATA_FIXTURE = AlternativeExtensionData.builder()
      .successorUrl("https://market.axonivy.com/successor")
      .alternativeExtension("successor-extension")
      .deprecatedVersionFrom("10.0.0")
      .build();

  @Mock
  private GitHub gitHub;

  @Mock
  private GitHubAccessTokenResponse gitHubAccessTokenResponse;

  @Mock
  private GHTeam ghTeam;

  @Mock
  private GithubUserRepository githubUserRepository;

  @Mock
  private ProductSecurityInfoRepository productSecurityInfoRepository;

  @Mock
  private OkHttpClientBuilder okHttpClientBuilder;

  @Mock
  private RestClientBuilder restClientBuilder;
  private MockRestServiceServer server;

  private RestClient restClient;

  @Mock
  private AppSettingService appSettingService;

  @Mock
  private MultiTaskUtils multiTaskUtils;

  @Mock
  private ThreadPoolTaskScheduler taskScheduler;

  @Spy
  @InjectMocks
  private GitHubServiceImpl gitHubService;

  @BeforeEach
  void setUpRestClientMocks() {
    RestClient.Builder builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
    restClient = builder.build();
    lenient().when(restClientBuilder.build()).thenReturn(restClient);
    lenient().when(okHttpClientBuilder.build()).thenReturn(new OkHttpClient());
    gitHubService = spy(new GitHubServiceImpl(restClientBuilder, githubUserRepository, appSettingService,
        productSecurityInfoRepository, okHttpClientBuilder, multiTaskUtils));
  }

  @AfterEach
  void verifyHttp() {
    if (server != null) {
      server.verify();
    }
  }

  private void expectGet(String url, String body) {
    server.expect(requestTo(url))
        .andExpect(method(org.springframework.http.HttpMethod.GET))
        .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
  }

  private void expectGet(String url, org.springframework.test.web.client.ResponseCreator responseCreator) {
    server.expect(requestTo(url))
        .andExpect(method(org.springframework.http.HttpMethod.GET))
        .andRespond(responseCreator);
  }

  private void expectPost(String url, org.springframework.test.web.client.ResponseCreator responseCreator) {
    server.expect(requestTo(url))
        .andExpect(method(org.springframework.http.HttpMethod.POST))
        .andRespond(responseCreator);
  }

  private void expectPatch(String url, org.springframework.test.web.client.ResponseCreator responseCreator) {
    server.expect(requestTo(url))
        .andExpect(method(org.springframework.http.HttpMethod.PATCH))
        .andRespond(responseCreator);
  }

  private void mockGitHubBuild(String accessToken) throws IOException {
    doReturn(gitHub).when(gitHubService).buildGitHub(accessToken);
  }

  @Test
  void testGetGitHubWithValidToken() throws IOException {
    when(appSettingService.getStringValueByKey(AppSettingKey.GITHUB_TOKEN)).thenReturn("validToken");
    mockGitHubBuild("validToken");
    assertNotNull(gitHubService.getGitHub(), "Expected GitHub object to be created with a valid token");
    verify(appSettingService).getStringValueByKey(AppSettingKey.GITHUB_TOKEN);
  }

  @Test
  void testGetGitHubWithExplicitAccessToken() throws IOException {
    String accessToken = "explicitToken";
    mockGitHubBuild(accessToken);
    GitHub result = gitHubService.getGitHub(accessToken);

    assertNotNull(result, "Expected GitHub object to be created with an explicit access token");
    verify(appSettingService, never()).getStringValueByKey(AppSettingKey.GITHUB_TOKEN);
  }

  @Test
  void testGetOrganizationWithValidOrgName() throws IOException {
    doReturn(gitHub).when(gitHubService).getGitHub();
    GHOrganization mockOrganization = mock(GHOrganization.class);
    when(gitHub.getOrganization("test-org")).thenReturn(mockOrganization);

    GHOrganization organization = gitHubService.getOrganization("test-org");

    assertNotNull(organization, "Expected organization to be returned for valid org name 'test-org'");

    verify(gitHubService).getGitHub();
    verify(gitHub).getOrganization("test-org");
  }

  @Test
  void testGetDirectoryContentValidInputs() throws IOException {
    GHRepository mockRepository = mock(GHRepository.class);
    String path = "src";
    String ref = "main";

    List<GHContent> mockContents = List.of(mock(GHContent.class), mock(GHContent.class));
    when(mockRepository.getDirectoryContent(path, ref)).thenReturn(mockContents);

    List<GHContent> contents = gitHubService.getDirectoryContent(mockRepository, path, ref);

    assertNotNull(contents, "Expected non-null directory content for valid path and ref");
    assertEquals(2, contents.size(), "Expected directory content list to contain exactly 2 items");

    verify(mockRepository).getDirectoryContent(path, ref);
  }

  @Test
  void testGetDirectoryContentNullRepository() {
    String path = "src";
    String ref = "main";

    assertThrows(IllegalArgumentException.class,
        () -> gitHubService.getDirectoryContent(null, path, ref),
        "Expected IllegalArgumentException when repository is null");
  }

  @Test
  void testGetRepositoryValidRepositoryPath() throws IOException {
    String repositoryPath = "my-org/my-repo";
    GHRepository mockRepository = mock(GHRepository.class);
    GitHub mockGitHub = mock(GitHub.class);
    doReturn(mockGitHub).when(gitHubService).getGitHub();
    when(mockGitHub.getRepository(repositoryPath)).thenReturn(mockRepository);

    GHRepository repository = gitHubService.getRepository(repositoryPath);

    assertNotNull(repository, "Expected repository to be returned for valid repository path 'my-org/my-repo'");
    verify(mockGitHub).getRepository(repositoryPath);
  }

  @Test
  void testGetGHContentValidInputs() throws IOException {
    GHRepository mockRepository = mock(GHRepository.class);
    String path = "README.md";
    String ref = "main";
    GHContent mockContent = mock(GHContent.class);

    when(mockRepository.getFileContent(path, ref)).thenReturn(mockContent);

    GHContent content = gitHubService.getGHContent(mockRepository, path, ref);

    assertNotNull(content, "Expected non-null content for valid path 'README.md' and ref 'main'");
    verify(mockRepository).getFileContent(path, ref);
  }

  @Test
  void testGetGHContentNullRepository() {
    String path = "README.md";
    String ref = "main";

    assertThrows(IllegalArgumentException.class,
        () -> gitHubService.getGHContent(null, path, ref),
        "Expected IllegalArgumentException when repository is null for getGHContent");
  }

  @Test
  void testGetAccessTokenValidCode() throws Exception {
    String code = "validCode";
    String clientId = "clientId";
    String clientSecret = "clientSecret";
    String accessToken = "accessToken";

    when(appSettingService.getStringValueByKey(AppSettingKey.GITHUB_OAUTH_CLIENT_ID)).thenReturn(clientId);
    when(appSettingService.getStringValueByKey(AppSettingKey.GITHUB_OAUTH_CLIENT_SECRET)).thenReturn(clientSecret);

    expectPost(GitHubConstants.GITHUB_GET_ACCESS_TOKEN_URL,
        withSuccess("{\"access_token\":\"" + accessToken + "\"}", MediaType.APPLICATION_JSON));

    GitHubAccessTokenResponse result = gitHubService.getAccessToken(code);

    assertNotNull(result, "Expected non-null GitHubAccessTokenResponse when valid code and properties are provided");
    assertEquals(accessToken, result.getAccessToken(),
        "Expected access token in response to match the mocked access token");

  }

  @Test
  void testGetAccessTokenGitHubErrorResponse() throws Oauth2ExchangeCodeException {
    String code = "validCode";
    String clientId = "clientId";
    String clientSecret = "clientSecret";
    String error = "invalid_grant";
    String errorDescription = "The authorization code is invalid";

    when(appSettingService.getStringValueByKey(AppSettingKey.GITHUB_OAUTH_CLIENT_ID)).thenReturn(clientId);
    when(appSettingService.getStringValueByKey(AppSettingKey.GITHUB_OAUTH_CLIENT_SECRET)).thenReturn(clientSecret);

    expectPost(GitHubConstants.GITHUB_GET_ACCESS_TOKEN_URL,
        withSuccess("""
            {"error":"invalid_grant","error_description":"The authorization code is invalid"}
            """.trim(), MediaType.APPLICATION_JSON));

    Oauth2ExchangeCodeException exception = assertThrows(
        Oauth2ExchangeCodeException.class,
        () -> gitHubService.getAccessToken(code),
        "Expected Oauth2ExchangeCodeException when GitHub returns an error response"
    );

    assertEquals(error, exception.getError(),
        "Expected exception error field to match the mocked GitHub error");
    assertEquals(errorDescription, exception.getErrorDescription(),
        "Expected exception errorDescription field to match the mocked GitHub error description");
  }


  @Test
  void testGetAccessTokenSuccessfulResponseWithError() throws Oauth2ExchangeCodeException {
    String code = "validCode";
    String clientId = "clientId";
    String clientSecret = "clientSecret";

    when(appSettingService.getStringValueByKey(AppSettingKey.GITHUB_OAUTH_CLIENT_ID)).thenReturn(clientId);
    when(appSettingService.getStringValueByKey(AppSettingKey.GITHUB_OAUTH_CLIENT_SECRET)).thenReturn(clientSecret);

    expectPost(GitHubConstants.GITHUB_GET_ACCESS_TOKEN_URL,
        withSuccess("{\"error\":\"error_code\",\"error_description\":\"Error description\"}",
            MediaType.APPLICATION_JSON));

    Oauth2ExchangeCodeException exception = assertThrows(
        Oauth2ExchangeCodeException.class,
        () -> gitHubService.getAccessToken(code),
        "Expected Oauth2ExchangeCodeException when GitHub responds with error fields despite a successful response"
    );

    assertEquals("error_code", exception.getError(),
        "Expected exception error field to match the GitHub response error");
    assertEquals("Error description", exception.getErrorDescription(),
        "Expected exception errorDescription field to match the GitHub response error description");
  }

  @Test
  void testValidateUserInOrganizationAndTeamValid() throws Exception {
    String accessToken = "validToken";
    String organization = "testOrg";
    String team = "devTeam";
    GHMyself fakeMyself = getFakeGHMyself();

    doReturn(gitHub).when(gitHubService).getGitHub(accessToken);
    doReturn(true).when(gitHubService).isUserInOrganizationAndTeam(gitHub, organization, team);
    when(gitHub.getMyself()).thenReturn(fakeMyself);

    GithubUser result = gitHubService.validateUserInOrganizationAndTeam(accessToken, organization, team);

    assertEquals(String.valueOf(123L), result.getGitHubId(), "GitHub ID should match the fake user");
    assertEquals("test-user", result.getName(), "Name should match the fake user");
    assertEquals("test-user", result.getUsername(), "Username should match the fake user");
    assertEquals("avatarUrl", result.getAvatarUrl(), "Avatar URL should match the fake user");
  }

  @Test
  void testIsUserInOrganizationAndTeamWhenNullGitHub() throws IOException {
    GitHub gitHubNullAble = null;
    String organization = "my-org";
    String teamName = "my-team";

    boolean result = gitHubService.isUserInOrganizationAndTeam(gitHubNullAble, organization, teamName);

    assertFalse(result, "Expected result to be false when GitHub instance is null");
  }

  @Test
  void testIsUserInOrganizationAndTeamWhenEmptyTeams() throws IOException {
    String organization = "my-org";
    String teamName = "my-team";
    Map<String, Set<GHTeam>> hashMapTeams = new HashMap<>();
    when(gitHub.getMyTeams()).thenReturn(hashMapTeams);

    boolean result = gitHubService.isUserInOrganizationAndTeam(gitHub, organization, teamName);

    assertFalse(result, "Expected result to be false when user has no teams in GitHub");
  }

  @Test
  void testIsUserInOrganizationAndTeamWhenTeamNotFound() throws IOException {
    String organization = "my-org";
    String teamName = "my-team";
    Set<GHTeam> teams = new HashSet<>();
    teams.add(ghTeam);
    Map<String, Set<GHTeam>> hashMapTeams = new HashMap<>();
    hashMapTeams.put(organization, teams);
    when(gitHub.getMyTeams()).thenReturn(hashMapTeams);

    boolean result = gitHubService.isUserInOrganizationAndTeam(gitHub, organization, teamName);

    assertFalse(result, "Expected result to be false when the specified team is not found in the organization");
  }

  @Test
  void testIsUserInOrganizationAndTeamWhenTeamFound() throws IOException {
    String organization = "my-org";
    String teamName = "my-team";
    Set<GHTeam> teams = new HashSet<>();
    when(ghTeam.getName()).thenReturn(teamName);
    teams.add(ghTeam);
    Map<String, Set<GHTeam>> hashMapTeams = new HashMap<>();
    hashMapTeams.put(organization, teams);
    when(gitHub.getMyTeams()).thenReturn(hashMapTeams);

    boolean result = gitHubService.isUserInOrganizationAndTeam(gitHub, organization, teamName);

    assertTrue(result, "Expected result to be true when the specified team is found in the organization");
  }

  @Test
  void testIsUserInOrganizationAndTeamWhenTeamListNull() throws IOException {
    String organization = "my-org";
    String teamName = "my-team";
    Map<String, Set<GHTeam>> hashMapTeams = new HashMap<>();
    hashMapTeams.put(organization, null);
    when(gitHub.getMyTeams()).thenReturn(hashMapTeams);

    boolean result = gitHubService.isUserInOrganizationAndTeam(gitHub, organization, teamName);

    assertFalse(result, "Expected result to be false when the organization exists but its team list is null");
  }

  @Test
  void testValidateUserInOrganizationAndTeamThrowsUnauthorizedException() throws IOException {
    String accessToken = "invalid-token";
    String organization = "orgName";
    String team = "teamName";

    GitHub gitHubMock = mock(GitHub.class);
    doReturn(gitHubMock).when(gitHubService).getGitHub(accessToken);
    doReturn(false).when(gitHubService).isUserInOrganizationAndTeam(gitHubMock, organization, team);

    UnauthorizedException exception = assertThrows(
        UnauthorizedException.class,
        () -> gitHubService.validateUserInOrganizationAndTeam(accessToken, organization, team),
        "Expected UnauthorizedException when user is not in the specified organization/team"
    );

    assertEquals(
        "GITHUB_USER_UNAUTHORIZED - User must be a member of team teamName and organization orgName",
        exception.getMessage(),
        "Expected exception message to clearly state unauthorized user and required team/organization"
    );
  }

  @Test
  void testSecurityInfo() throws IOException {
    String accessToken = "accessToken";
    String orgName = "orgName";
    String repoName = "repoName";
    String branchSHA1 = "branchSHA1";
    GHRepository ghRepository = mock(GHRepository.class);
    GHOrganization ghOrganization = mock(GHOrganization.class);
    when(ghOrganization.getLogin()).thenReturn(orgName);
    when(ghRepository.getName()).thenReturn(repoName);
    when(ghRepository.getVisibility()).thenReturn(GHRepository.Visibility.PUBLIC);
    GHBranch branch = mock(GHBranch.class);
    when(branch.isProtected()).thenReturn(true);
    when(branch.getSHA1()).thenReturn(branchSHA1);
    GHCommit commit = mock(GHCommit.class);
    when(commit.getCommitDate()).thenReturn(new Date());
    when(ghRepository.getCommit(branchSHA1)).thenReturn(commit);
    when(ghRepository.getBranch(any())).thenReturn(branch);
    String urlSecretScanning = String.format(GitHubConstants.Url.REPO_SECRET_SCANNING_ALERTS_OPEN, orgName, repoName);
    server.expect(requestTo(urlSecretScanning))
        .andExpect(method(org.springframework.http.HttpMethod.GET))
        .andRespond(withSuccess("[{\"number\":1}]", MediaType.APPLICATION_JSON));
    String urlCodeScanning = String.format(GitHubConstants.Url.REPO_CODE_SCANNING_ALERTS_OPEN, orgName, repoName);
    server.expect(requestTo(urlCodeScanning))
        .andExpect(method(org.springframework.http.HttpMethod.GET))
        .andRespond(withSuccess("[{\"number\":1,\"state\":\"open\",\"rule\":{\"security_severity_level\":\"high\"}}]",
            MediaType.APPLICATION_JSON));
    String urlDependabotScanning = String.format(GitHubConstants.Url.REPO_DEPENDABOT_ALERTS_OPEN, orgName, repoName);
    server.expect(requestTo(urlDependabotScanning))
        .andExpect(method(org.springframework.http.HttpMethod.GET))
        .andRespond(withSuccess("""
            [{"number":1,"state":"open","security_advisory":{"severity":"high"}}]
            """.trim(), MediaType.APPLICATION_JSON));

    ProductSecurityInfo result = gitHubService.fetchSecurityInfoSafe(ghRepository, ghOrganization, accessToken);
    assertNotNull(result, "Expected non-null ProductSecurityInfo result when security scanning responses are mocked");
  }

  @Test
  void testGetNumberOfSecretScanningAlertsSuccess() {
    String accessToken = "accessToken";
    String orgName = "orgName";
    String repoName = "repoName";
    String url = String.format(GitHubConstants.Url.REPO_SECRET_SCANNING_ALERTS_OPEN, orgName, repoName);
    GHRepository ghRepository = mock(GHRepository.class);
    GHOrganization ghOrganization = mock(GHOrganization.class);
    when(ghOrganization.getLogin()).thenReturn(orgName);
    when(ghRepository.getName()).thenReturn(repoName);
    server.expect(requestTo(url))
        .andExpect(method(org.springframework.http.HttpMethod.GET))
        .andRespond(withSuccess("[{\"number\":1}]", MediaType.APPLICATION_JSON));
    SecretScanning result = gitHubService.getNumberOfSecretScanningAlerts(ghRepository, ghOrganization, accessToken);
    assertEquals(
        AccessLevel.ENABLED,
        result.getStatus(),
        "Expected secret scanning status to be ENABLED when API returns alerts"
    );
  }

  @Test
  void testGetCodeScanningAlertsSuccess() {
    String accessToken = "accessToken";
    String orgName = "orgName";
    String repoName = "repoName";
    String url = String.format(GitHubConstants.Url.REPO_CODE_SCANNING_ALERTS_OPEN, orgName, repoName);
    GHRepository ghRepository = mock(GHRepository.class);
    GHOrganization ghOrganization = mock(GHOrganization.class);
    when(ghOrganization.getLogin()).thenReturn(orgName);
    when(ghRepository.getName()).thenReturn(repoName);
    server.expect(requestTo(url))
        .andExpect(method(org.springframework.http.HttpMethod.GET))
        .andRespond(withSuccess("[{\"number\":1,\"state\":\"open\",\"rule\":{\"security_severity_level\":\"high\"}}]",
            MediaType.APPLICATION_JSON));
    CodeScanning result = gitHubService.getCodeScanningAlerts(ghRepository, ghOrganization, accessToken);
    assertEquals(
        AccessLevel.ENABLED,
        result.getStatus(),
        "Expected code scanning status to be ENABLED when API returns open alerts with high severity"
    );
  }

  @Test
  void testGetDependabotAlertsSuccess() {
    String accessToken = "accessToken";
    String orgName = "orgName";
    String repoName = "repoName";
    String url = String.format(GitHubConstants.Url.REPO_DEPENDABOT_ALERTS_OPEN, orgName, repoName);
    GHRepository ghRepository = mock(GHRepository.class);
    GHOrganization ghOrganization = mock(GHOrganization.class);
    when(ghOrganization.getLogin()).thenReturn(orgName);
    when(ghRepository.getName()).thenReturn(repoName);
    server.expect(requestTo(url))
        .andExpect(method(org.springframework.http.HttpMethod.GET))
        .andRespond(withSuccess("[{\"number\":1,\"state\":\"open\",\"security_advisory\":{\"severity\":\"high\"}}]",
            MediaType.APPLICATION_JSON));
    Dependabot result = gitHubService.getDependabotAlerts(ghRepository, ghOrganization, accessToken);
    assertEquals(
        AccessLevel.ENABLED,
        result.getStatus(),
        "Expected Dependabot alerts status to be ENABLED when API returns open alerts with high severity"
    );
  }

  @Test
  void testGetDependabotAlertsNotFound() {
    String accessToken = "accessToken";
    String orgName = "orgName";
    String repoName = "repoName";
    String url = String.format(GitHubConstants.Url.REPO_DEPENDABOT_ALERTS_OPEN, orgName, repoName);
    GHRepository ghRepository = mock(GHRepository.class);
    GHOrganization ghOrganization = mock(GHOrganization.class);
    when(ghOrganization.getLogin()).thenReturn(orgName);
    when(ghRepository.getName()).thenReturn(repoName);
    server.expect(requestTo(url))
        .andExpect(method(org.springframework.http.HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));
    Dependabot result = gitHubService.getDependabotAlerts(ghRepository, ghOrganization, accessToken);
    assertEquals(
        AccessLevel.NO_PERMISSION,
        result.getStatus(),
        "Expected Dependabot alerts status to be NO_PERMISSION when the API returns 404 Not Found"
    );
  }

  @Test
  void testGetDependabotAlertsDisabled() {
    String accessToken = "accessToken";
    String orgName = "orgName";
    String repoName = "repoName";
    String url = String.format(GitHubConstants.Url.REPO_DEPENDABOT_ALERTS_OPEN, orgName, repoName);

    GHRepository ghRepository = mock(GHRepository.class);
    GHOrganization ghOrganization = mock(GHOrganization.class);
    when(ghOrganization.getLogin()).thenReturn(orgName);
    when(ghRepository.getName()).thenReturn(repoName);
    server.expect(requestTo(url))
        .andExpect(method(org.springframework.http.HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.FORBIDDEN)
            .body("Code Security must be enabled for this repository to use code scanning."));

    Dependabot result = gitHubService.getDependabotAlerts(ghRepository, ghOrganization, accessToken);

    assertEquals(
        AccessLevel.DISABLED,
        result.getStatus(),
        "Expected Dependabot alerts status to be DISABLED when the API returns 403 Forbidden"
    );
  }

  @Test
  void testGetGitHubReleaseModelsWithMEmptyReleases() throws IOException {
    Product mockProduct = mock(Product.class);
    Pageable mockPageable = mock(Pageable.class);


    Page<GitHubReleaseModel> result = gitHubService.getGitHubReleaseModels(Collections.emptyList(), mockPageable,
        mockProduct.getId(), mockProduct.getRepositoryName(), mockProduct.getSourceUrl());

    assertNotNull(result, "Expected non-null Page of GitHubReleaseModel even when there are no releases");
    assertTrue(result.isEmpty(), "Expected Page to be empty when there are no GitHub releases");
  }

  @Test
  void testGetExportTestArtifactSuccess() throws IOException {
    // Arrange
    GHWorkflowRun mockRun = mock(GHWorkflowRun.class);
    GHArtifact mockArtifact1 = mock(GHArtifact.class);
    GHArtifact mockArtifact2 = mock(GHArtifact.class);
    PagedIterable<GHArtifact> mockPagedIterable = mock(PagedIterable.class);

    when(mockRun.listArtifacts()).thenReturn(mockPagedIterable);
    when(mockPagedIterable.toList()).thenReturn(Arrays.asList(mockArtifact1, mockArtifact2));

    when(mockArtifact1.getName()).thenReturn("other-artifact");
    when(mockArtifact2.getName()).thenReturn(CommonConstants.TEST_REPORT_FILE);

    GHArtifact result = gitHubService.getExportTestArtifact(mockRun);

    assertNotNull(result, "Artifact should not be null");
    assertEquals(mockArtifact2, result, "Should return the artifact with the target name");
    verify(mockRun).listArtifacts();
  }

  @Test
  void testGetLatestWorkflowRunWorkflowNotFound() throws IOException {
    GHRepository mockRepo = mock(GHRepository.class);
    String workflowFileName = "non-existent-workflow.yml";

    when(mockRepo.getWorkflow(workflowFileName)).thenThrow(new GHFileNotFoundException("Workflow not found"));
    when(mockRepo.getFullName()).thenReturn("owner/repo");

    GHWorkflowRun result = gitHubService.getLatestWorkflowRun(mockRepo, workflowFileName);

    assertNull(result, "Should return null when workflow is not found");
    verify(mockRepo).getWorkflow(workflowFileName);
  }

  @Test
  void testGetLatestWorkflowRunNoSuchElementException() throws IOException {
    GHRepository mockRepo = mock(GHRepository.class);
    String workflowFileName = "test-workflow.yml";

    when(mockRepo.getWorkflow(workflowFileName)).thenThrow(new NoSuchElementException("No workflow found"));
    when(mockRepo.getFullName()).thenReturn("owner/repo");

    GHWorkflowRun result = gitHubService.getLatestWorkflowRun(mockRepo, workflowFileName);

    assertNull(result, "Should return null when no workflow run is found");
    verify(mockRepo).getWorkflow(workflowFileName);
  }

  @Test
  void testGetExportTestArtifactNotFound() throws IOException {
    GHWorkflowRun mockRun = mock(GHWorkflowRun.class);
    GHArtifact mockArtifact1 = mock(GHArtifact.class);
    GHArtifact mockArtifact2 = mock(GHArtifact.class);
    PagedIterable<GHArtifact> mockPagedIterable = mock(PagedIterable.class);

    when(mockRun.listArtifacts()).thenReturn(mockPagedIterable);
    when(mockPagedIterable.toList()).thenReturn(Arrays.asList(mockArtifact1, mockArtifact2));

    when(mockArtifact1.getName()).thenReturn("other-artifact-1");
    when(mockArtifact2.getName()).thenReturn("other-artifact-2");

    GHArtifact result = gitHubService.getExportTestArtifact(mockRun);

    assertNull(result, "Should return null when no artifact with the target name is found");
    verify(mockRun).listArtifacts();
  }

  @Test
  void testGetRepositoryRepositoryFound() throws IOException {
    String repositoryPath = "org/repo";
    GHRepository mockRepository = mock(GHRepository.class);
    GitHub mockGitHub = mock(GitHub.class);
    doReturn(mockGitHub).when(gitHubService).getGitHub();
    when(mockGitHub.getRepository(repositoryPath)).thenReturn(mockRepository);

    GHRepository result = gitHubService.getRepository(repositoryPath);

    assertNotNull(result, "Expected non-null repository");
    verify(mockGitHub).getRepository(repositoryPath);
  }

  @Test
  void testGetRepositoryGhFileNotFoundException() throws IOException {
    doReturn(gitHub).when(gitHubService).getGitHub();
    when(gitHub.getRepository("missing/repo")).thenThrow(new GHFileNotFoundException());

    GHRepository result = gitHubService.getRepository("missing/repo");

    assertNull(result, "Expected null result when GHFileNotFoundException is thrown");
  }

  @Test
  void testGetRepositoryIOException() throws IOException {
    doReturn(gitHub).when(gitHubService).getGitHub();
    when(gitHub.getRepository("error/repo")).thenThrow(new IOException("IO error"));

    GHRepository result = gitHubService.getRepository("error/repo");

    assertNull(result, "Expected null result when IOException is thrown");
  }

  @Test
  void testGetRepoOfficialReleasesWithEmptyReleases() throws IOException {
    String repoName = "test-org/empty-repo";
    String productId = "test-product-id";

    GHRepository mockRepository = mock(GHRepository.class);
    PagedIterable<GHRelease> mockPagedIterable = mock(PagedIterable.class);

    doReturn(mockRepository).when(gitHubService).getRepository(repoName);
    when(mockRepository.listReleases()).thenReturn(mockPagedIterable);
    doAnswer(invocation -> null).when(mockPagedIterable).forEach(any());

    List<GHRelease> result = gitHubService.getRepoOfficialReleases(repoName, productId);

    assertNotNull(result, "Result should not be null");
    assertTrue(result.isEmpty(), "Should return empty list when no releases exist");

    verify(gitHubService, atLeastOnce()).getRepository(repoName);
    verify(mockRepository).listReleases();
  }

  @Test
  void testGetRepoOfficialReleasesWithIOExceptionThrown() {
    String repoName = "test-org/error-repo";
    String productId = "test-product-id";

    doThrow(new IOException("Network error")).when(gitHubService).getRepository(repoName);

    assertThrows(IOException.class, () -> gitHubService.getRepoOfficialReleases(repoName, productId),
        "Should propagate IOException when repository access fails");

    verify(gitHubService, atLeastOnce()).getRepository(repoName);
  }

  @Test
  void testGetRepoOfficialReleasesWithMixedReleaseTypes() throws IOException {
    String repoName = "test-org/mixed-repo";
    String productId = "test-product-id";

    GHRepository mockRepository = mock(GHRepository.class);
    PagedIterable<GHRelease> mockPagedIterable = mock(PagedIterable.class);

    GHRelease officialRelease1 = mock(GHRelease.class);
    GHRelease draftRelease1 = mock(GHRelease.class);
    GHRelease officialRelease2 = mock(GHRelease.class);
    GHRelease draftRelease2 = mock(GHRelease.class);
    GHRelease officialRelease3 = mock(GHRelease.class);

    when(officialRelease1.isDraft()).thenReturn(false);
    when(draftRelease1.isDraft()).thenReturn(true);
    when(officialRelease2.isDraft()).thenReturn(false);
    when(draftRelease2.isDraft()).thenReturn(true);
    when(officialRelease3.isDraft()).thenReturn(false);

    List<GHRelease> allReleases = Arrays.asList(officialRelease1, draftRelease1, officialRelease2, draftRelease2,
        officialRelease3);

    doReturn(mockRepository).when(gitHubService).getRepository(repoName);
    when(mockRepository.listReleases()).thenReturn(mockPagedIterable);
    doAnswer(invocation -> {
      Consumer<GHRelease> consumer = invocation.getArgument(0);
      allReleases.forEach(consumer);
      return null;
    }).when(mockPagedIterable).forEach(any());

    List<GHRelease> result = gitHubService.getRepoOfficialReleases(repoName, productId);

    assertNotNull(result, "Result should not be null");
    assertEquals(3, result.size(), "Should return only the 3 official (non-draft) releases");
    assertTrue(result.contains(officialRelease1), "Should contain first official release");
    assertTrue(result.contains(officialRelease2), "Should contain second official release");
    assertTrue(result.contains(officialRelease3), "Should contain third official release");
    assertFalse(result.contains(draftRelease1), "Should not contain first draft release");
    assertFalse(result.contains(draftRelease2), "Should not contain second draft release");
    verify(gitHubService, atLeastOnce()).getRepository(repoName);
    verify(mockRepository).listReleases();
  }

  @Test
  void testGetGitHubReleaseModels() throws IOException {
    List<GHRelease> allReleases = Arrays.asList(
        mock(GHRelease.class),
        mock(GHRelease.class),
        mock(GHRelease.class)
    );
    List<GHRelease> pagedReleases = Arrays.asList(allReleases.get(0), allReleases.get(1));
    Pageable pageable = mock(Pageable.class);
    String productId = "prod-1";
    String productRepoName = "repo-1";
    String productSourceUrl = "http://source.url";
    GHRelease latestRelease = mock(GHRelease.class);
    when(latestRelease.getName()).thenReturn("latest-release");
    try (MockedStatic<ProductContentUtils> utilsMock = Mockito.mockStatic(ProductContentUtils.class)) {
      utilsMock.when(() -> ProductContentUtils.extractReleasesPage(allReleases, pageable)).thenReturn(pagedReleases);
      doReturn(latestRelease).when(gitHubService).getGitHubLatestReleaseByProductId(productRepoName);
      when(pagedReleases.get(0).getName()).thenReturn("release-1");
      when(pagedReleases.get(1).getName()).thenReturn("latest-release");
      GitHubReleaseModel model1 = new GitHubReleaseModel();
      model1.setName("release-1");
      GitHubReleaseModel model2 = new GitHubReleaseModel();
      model2.setName("latest-release");
      doReturn(model1).when(gitHubService).toGitHubReleaseModel(pagedReleases.get(0), productSourceUrl, productId,
          false);
      doReturn(model2).when(gitHubService).toGitHubReleaseModel(pagedReleases.get(1), productSourceUrl, productId,
          true);
      Page<GitHubReleaseModel> result = gitHubService.getGitHubReleaseModels(allReleases, pageable, productId,
          productRepoName, productSourceUrl);
      assertEquals(2, result.getContent().size(), "Should return two models for the paged releases");
      assertEquals("release-1", result.getContent().get(0).getName(), "First model name should match first release");
      assertEquals("latest-release", result.getContent().get(1).getName(),
          "Second model name should match latest release");
      verify(gitHubService).toGitHubReleaseModel(pagedReleases.get(0), productSourceUrl, productId, false);
      verify(gitHubService).toGitHubReleaseModel(pagedReleases.get(1), productSourceUrl, productId, true);
    }
  }

  @Test
  void testGetAndUpdateUserShouldUpdateAndReturnUserWhenUserExists() throws Exception {
    String accessToken = "token";
    GHMyself myself = getFakeGHMyself();

    when(gitHub.getMyself()).thenReturn(myself);
    doReturn(gitHub).when(gitHubService).getGitHub(accessToken);

    GithubUser existingUser = new GithubUser();
    existingUser.setGitHubId("123");
    when(githubUserRepository.searchByGitHubId("123")).thenReturn(existingUser);

    GithubUser result = gitHubService.getAndUpdateUser(accessToken);

    assertNotNull(result, "Returned GithubUser should not be null");
    assertEquals("123", result.getGitHubId(), "GitHubId should be set correctly");
    assertEquals("test-user", result.getName(), "Name should be updated from GHMyself");
    assertEquals("test-user", result.getUsername(), "Username should be updated from GHMyself");
    assertEquals("avatarUrl", result.getAvatarUrl(), "Avatar URL should be updated from GHMyself");
    assertEquals(GitHubConstants.GITHUB_PROVIDER_NAME, result.getProvider(), "Provider should be set to 'github'");

    ArgumentCaptor<GithubUser> captor = ArgumentCaptor.forClass(GithubUser.class);
    verify(githubUserRepository, times(1)).save(captor.capture());
    assertEquals(result, captor.getValue(), "Saved user should match returned user");
  }

  @Test
  void testGetAndUpdateUserThrowsNotFoundExceptionOnIOException() throws Exception {
    // given
    String accessToken = "token";
    doReturn(gitHub).when(gitHubService).getGitHub(accessToken);
    when(gitHub.getMyself()).thenThrow(new IOException("GitHub API down"));

    // when + then
    NotFoundException ex = assertThrows(NotFoundException.class,
        () -> gitHubService.getAndUpdateUser(accessToken),
        "IOException should be translated into NotFoundException");

    assertEquals(
        ErrorCode.GITHUB_USER_NOT_FOUND.getHelpText() + CoreCommonConstants.HYPHEN + "Failed to fetch " +
            "user details from GitHub", ex.getMessage(),
        "Error message should be meaningful");
  }

  @Test
  void testGetLatestWorkflowRunNoCompletedRunsReturnsNull() throws IOException {
    GHRepository repo = mock(GHRepository.class);
    GHWorkflow workflow = mock(GHWorkflow.class);
    PagedIterable<GHWorkflowRun> pagedRuns = mock(PagedIterable.class);
    PagedIterator<GHWorkflowRun> pagedIterator = mock(PagedIterator.class);

    GHWorkflowRun inProgressRun = mock(GHWorkflowRun.class);

    when(repo.getWorkflow("build.yml")).thenReturn(workflow);
    when(workflow.listRuns()).thenReturn(pagedRuns);
    when(pagedRuns.withPageSize(anyInt())).thenReturn(pagedRuns);

    when(pagedRuns.iterator()).thenReturn(pagedIterator);

    when(pagedIterator.hasNext()).thenReturn(true, false);
    when(pagedIterator.next()).thenReturn(inProgressRun);

    when(inProgressRun.getStatus())
        .thenReturn(GHWorkflowRun.Status.IN_PROGRESS);

    GHWorkflowRun result =
        gitHubService.getLatestWorkflowRun(repo, "build.yml");

    assertNull(result,
        "Expected null when no workflow runs have COMPLETED status, but a run was returned.");
  }

  @Test
  void testDownloadArtifactZipReturnsStreamWithDownloadedBytes() throws Exception {
    GHArtifact artifact = mock(GHArtifact.class);
    byte[] expected = new byte[]{1, 2, 3};

    doAnswer(inv -> {
      InputStreamFunction<Void> fn = inv.getArgument(0);
      fn.apply(new ByteArrayInputStream(expected));
      return null;
    }).when(artifact).download(ArgumentMatchers.any(InputStreamFunction.class));

    InputStream result = gitHubService.downloadArtifactZip(artifact);

    assertArrayEquals(expected, result.readAllBytes(),
        "Returned InputStream should contain exactly the bytes provided by the artifact download callback.");

    verify(artifact).download(ArgumentMatchers.any(InputStreamFunction.class));
  }

  @Test
  void testUpdateReadmeUnsupportedPullRequestReturnsNullWhenAddActionDoesNotChangeReadme() throws Exception {
    GHRepository repository = mock(GHRepository.class);
    GHContent readme = mock(GHContent.class);
    setupBaseRepositoryMocks(repository, readme, "# Title\n" + UNSUPPORTED_NOTICE_FIXTURE + "\nBody");

    GHPullRequest result = gitHubService.updateReadmeForSuccessorNotes("org/repo", PullRequestAction.ADD, EXTENSION_DATA_FIXTURE);

    assertNull(result, "Expected null when README already contains unsupported notice");
    verify(readme, never()).update(anyString(), anyString(), anyString());
    verify(repository, never()).createPullRequest(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testUpdateReadmeUnsupportedPullRequestReturnsNullWhenRemoveActionDoesNotChangeReadme() throws Exception {
    GHRepository repository = mock(GHRepository.class);
    GHContent readme = mock(GHContent.class);
    setupBaseRepositoryMocks(repository, readme, "# Title\nBody");
    when(repository.getRef(HEADS_PREFIX + UNSUPPORTED_BRANCH_NAME_FIXTURE)).thenThrow(new GHFileNotFoundException());

    GHPullRequest result = gitHubService.updateReadmeForSuccessorNotes("org/repo", PullRequestAction.REMOVE, EXTENSION_DATA_FIXTURE);

    assertNull(result, "Expected null when README has no unsupported notice to remove");
    verify(readme, never()).update(anyString(), anyString(), anyString());
    verify(repository, never()).createPullRequest(anyString(), anyString(), anyString(), anyString());
  }

  @Test
  void testRemoveBranchWhenActionIsRemoveAndBranchExists() throws Exception {
    GHRepository repository = mock(GHRepository.class);
    GHContent readme = mock(GHContent.class);
    GHRef branchRef = mock(GHRef.class);
    setupBaseRepositoryMocks(repository, readme, "# Title\nBody");
    when(repository.getRef(HEADS_PREFIX + UNSUPPORTED_BRANCH_NAME_FIXTURE)).thenReturn(branchRef);

    GHPullRequest result = gitHubService.updateReadmeForSuccessorNotes("org/repo", PullRequestAction.REMOVE, EXTENSION_DATA_FIXTURE);

    assertNull(result, "Expected null when README already has no notice and content is unchanged");
    verify(branchRef).delete();
  }

  @Test
  void testRemoveBranchWhenActionIsRemoveAndBranchDoesNotExist() throws Exception {
    GHRepository repository = mock(GHRepository.class);
    GHContent readme = mock(GHContent.class);
    setupBaseRepositoryMocks(repository, readme, "# Title\nBody");
    when(repository.getRef(HEADS_PREFIX + UNSUPPORTED_BRANCH_NAME_FIXTURE)).thenThrow(new GHFileNotFoundException());

    GHPullRequest result = gitHubService.updateReadmeForSuccessorNotes("org/repo", PullRequestAction.REMOVE, EXTENSION_DATA_FIXTURE);

    assertNull(result, "Expected null and no exception when branch does not exist");
    verify(repository, atLeastOnce()).getRef(HEADS_PREFIX + UNSUPPORTED_BRANCH_NAME_FIXTURE);
  }

  @Test
  void testBranchNotDeletedWhenActionIsAddAndContentIsAlreadyUpToDate() throws Exception {
    GHRepository repository = mock(GHRepository.class);
    GHContent readme = mock(GHContent.class);
    GHRef branchRef = mock(GHRef.class);
    setupBaseRepositoryMocks(repository, readme, "# Title\n" + UNSUPPORTED_NOTICE_FIXTURE + "\nBody");

    GHPullRequest result = gitHubService.updateReadmeForSuccessorNotes("org/repo", PullRequestAction.ADD, EXTENSION_DATA_FIXTURE);

    assertNull(result, "Expected null when README already contains the unsupported notice");
    verify(branchRef, never()).delete();
  }

  @Test
  void testUpdateReadmeForSuccessorNotesWhenAlreadyOpen() throws Exception {
    GHRepository repository = mock(GHRepository.class);
    GHContent readme = mock(GHContent.class);
    GHRef existingBranchRef = mock(GHRef.class);
    GHPullRequest existingPr = mock(GHPullRequest.class);
    when(existingPr.getHtmlUrl())
        .thenReturn(URI.create("https://example.com/pr/1").toURL());
    when(existingPr.getTitle()).thenReturn("Add unsupported notice to README");

    setupBaseRepositoryMocks(repository, readme, "# Title\nBody");
    when(repository.getRef(HEADS_PREFIX + UNSUPPORTED_BRANCH_NAME_FIXTURE)).thenReturn(existingBranchRef);
    mockOpenPullRequests(repository, List.of(existingPr));

    GHPullRequest result = gitHubService.updateReadmeForSuccessorNotes("org/repo", PullRequestAction.ADD, EXTENSION_DATA_FIXTURE);

    assertEquals(existingPr, result, "Expected already open pull request to be returned");
    verify(repository, never()).createPullRequest(anyString(), anyString(), anyString(), anyString());
    verify(readme, never()).update(anyString(), anyString(), anyString());
  }

  @Test
  void testUpdateReadmeForSuccessorNotesFromExistingBranchWhenNoOpenPr() throws Exception {
    GHRepository repository = mock(GHRepository.class);
    GHContent readme = mock(GHContent.class);
    GHRef existingBranchRef = mock(GHRef.class);
    GHCompare compare = mock(GHCompare.class);
    GHPullRequest createdPr = mock(GHPullRequest.class);

    setupBaseRepositoryMocks(repository, readme, "# Title\nBody");
    when(repository.getRef(HEADS_PREFIX + UNSUPPORTED_BRANCH_NAME_FIXTURE)).thenReturn(existingBranchRef);
    mockOpenPullRequests(repository, Collections.emptyList());
    when(repository.getCompare(BASE_BRANCH, UNSUPPORTED_BRANCH_NAME_FIXTURE)).thenReturn(compare);
    when(compare.getStatus()).thenReturn(GHCompare.Status.ahead);
    when(repository.createPullRequest(anyString(), eq(UNSUPPORTED_BRANCH_NAME_FIXTURE), eq(BASE_BRANCH), anyString()))
        .thenReturn(createdPr);

    GHPullRequest result = gitHubService.updateReadmeForSuccessorNotes("org/repo", PullRequestAction.ADD, EXTENSION_DATA_FIXTURE);

    assertEquals(createdPr, result, "Expected a new pull request to be created from existing branch");
    verify(readme, never()).update(anyString(), anyString(), anyString());
    verify(repository, never()).createRef(eq(REFS_HEADS_PREFIX + UNSUPPORTED_BRANCH_NAME_FIXTURE), anyString());
  }

  @Test
  void testUpdateReadmeUnsupportedPullRequestRecreatesMergedBranchAndUpdatesReadmeBeforeCreatingPr() throws Exception {
    GHRepository repository = mock(GHRepository.class);
    GHContent readme = mock(GHContent.class);
    GHRef existingBranchRef = mock(GHRef.class);
    GHCompare compare = mock(GHCompare.class);
    GHRef baseBranchRef = mock(GHRef.class, RETURNS_DEEP_STUBS);
    GHPullRequest createdPr = mock(GHPullRequest.class);

    setupBaseRepositoryMocks(repository, readme, "# Title\nBody");
    when(repository.getRef(HEADS_PREFIX + UNSUPPORTED_BRANCH_NAME_FIXTURE)).thenReturn(existingBranchRef);
    mockOpenPullRequests(repository, Collections.emptyList());
    when(repository.getCompare(BASE_BRANCH, UNSUPPORTED_BRANCH_NAME_FIXTURE)).thenReturn(compare);
    when(compare.getStatus()).thenReturn(GHCompare.Status.behind);
    when(repository.getRef(HEADS_PREFIX + BASE_BRANCH)).thenReturn(baseBranchRef);
    when(baseBranchRef.getObject().getSha()).thenReturn("base-sha");
    when(repository.createPullRequest(anyString(), eq(UNSUPPORTED_BRANCH_NAME_FIXTURE), eq(BASE_BRANCH), anyString()))
        .thenReturn(createdPr);

    GHPullRequest result = gitHubService.updateReadmeForSuccessorNotes("org/repo", PullRequestAction.ADD, EXTENSION_DATA_FIXTURE);

    assertEquals(createdPr, result, "Expected pull request to be created after branch recreation");
    verify(existingBranchRef).delete();
    verify(repository).createRef(REFS_HEADS_PREFIX + UNSUPPORTED_BRANCH_NAME_FIXTURE, "base-sha");
    verify(readme).update(anyString(), anyString(), eq(UNSUPPORTED_BRANCH_NAME_FIXTURE));
  }

  @Test
  void testUpdateReadmeUnsupportedPullRequestCreatesBranchAndPrWhenUnsupportedBranchDoesNotExist() throws Exception {
    GHRepository repository = mock(GHRepository.class);
    GHContent readme = mock(GHContent.class);
    GHRef baseBranchRef = mock(GHRef.class, RETURNS_DEEP_STUBS);
    GHPullRequest createdPr = mock(GHPullRequest.class);
    setupBaseRepositoryMocks(repository, readme, "# Title\nBody");
    when(repository.getRef(HEADS_PREFIX + UNSUPPORTED_BRANCH_NAME_FIXTURE)).thenThrow(new GHFileNotFoundException());
    when(repository.getRef(HEADS_PREFIX + BASE_BRANCH)).thenReturn(baseBranchRef);
    when(baseBranchRef.getObject().getSha()).thenReturn("base-sha");
    when(repository.createPullRequest(anyString(), eq(UNSUPPORTED_BRANCH_NAME_FIXTURE), eq(BASE_BRANCH), anyString()))
        .thenReturn(createdPr);

    GHPullRequest result = gitHubService.updateReadmeForSuccessorNotes("org/repo", PullRequestAction.ADD, EXTENSION_DATA_FIXTURE);

    assertEquals(createdPr, result, "Expected pull request to be created when unsupported branch is missing");
    verify(repository).createRef(REFS_HEADS_PREFIX + UNSUPPORTED_BRANCH_NAME_FIXTURE, "base-sha");
    verify(readme).update(anyString(), anyString(), eq(UNSUPPORTED_BRANCH_NAME_FIXTURE));
  }

  @Test
  void testUpdateReadmeForSuccessorNotesThrowsWhenActionIsNull() throws Exception {
    GHRepository repository = mock(GHRepository.class);
    GHContent readme = mock(GHContent.class);
    setupBaseRepositoryMocks(repository, readme, "# Title\nBody");

    assertThrows(NullPointerException.class,
        () -> gitHubService.updateReadmeForSuccessorNotes("org/repo", null, EXTENSION_DATA_FIXTURE),
        "Expected NullPointerException when pull request action is null");
  }

  @Test
  void testUpdateReadmeUnsupportedPullRequestThrowsWhenReadmeHasNoHeading() throws Exception {
    GHRepository repository = mock(GHRepository.class);
    GHContent readme = mock(GHContent.class);
    setupBaseRepositoryMocks(repository, readme, "Body without markdown heading");

    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
        () -> gitHubService.updateReadmeForSuccessorNotes("org/repo", PullRequestAction.ADD, EXTENSION_DATA_FIXTURE),
        "Expected IllegalArgumentException when README has no heading");

    assertEquals("README.md must contain a heading line starting with '#'", ex.getMessage(),
        "Exception message should clearly indicate missing markdown heading in README"
    );
  }

  @Test
  void testSearchSecurityDetailsDelegatesToRepository() {
    // Arrange
    ProductSecurityCriteria criteria = ProductSecurityCriteria.builder()
        .searchText("portal")
        .sortDirection("DESC")
        .build();
    Pageable pageable = mock(Pageable.class);

    ProductSecurityInfo infoA = buildMockProductSecurityInfo("portal-connector");
    ProductSecurityInfo infoB = buildMockProductSecurityInfo("portal-engine");
    Page<ProductSecurityInfo> expectedPage = new org.springframework.data.domain.PageImpl<>(
        List.of(infoA, infoB));

    when(productSecurityInfoRepository.searchProductSecurityAndSorting(criteria, pageable))
        .thenReturn(expectedPage);

    // Act
    Page<ProductSecurityInfo> result = gitHubService.searchSecurityDetails(criteria, pageable);

    // Assert
    assertNotNull(result, "Expected non-null page result from searchSecurityDetails");
    assertEquals(2, result.getContent().size(),
        "Expected two repositories matching search criteria");
    assertEquals("portal-connector", result.getContent().get(0).getRepoName(),
        "Expected first result to be 'portal-connector'");
    assertEquals("portal-engine", result.getContent().get(1).getRepoName(),
        "Expected second result to be 'portal-engine'");
    verify(productSecurityInfoRepository).searchProductSecurityAndSorting(criteria, pageable);
  }

  @Test
  void testSearchSecurityDetailsWithEmptyCriteriaReturnsAllResults() {
    // Arrange
    ProductSecurityCriteria criteria = ProductSecurityCriteria.builder().build();
    Pageable pageable = mock(Pageable.class);

    Page<ProductSecurityInfo> expectedPage = new org.springframework.data.domain.PageImpl<>(
        List.of(buildMockProductSecurityInfo("repo-a"), buildMockProductSecurityInfo("repo-b")));

    when(productSecurityInfoRepository.searchProductSecurityAndSorting(criteria, pageable))
        .thenReturn(expectedPage);

    // Act
    Page<ProductSecurityInfo> result = gitHubService.searchSecurityDetails(criteria, pageable);

    // Assert
    assertNotNull(result, "Expected non-null page result when criteria has no filters");
    assertEquals(2, result.getTotalElements(),
        "Expected two repositories returned when no search filter is applied");
    verify(productSecurityInfoRepository).searchProductSecurityAndSorting(criteria, pageable);
  }

  @Test
  void testSearchSecurityDetailsWithNoMatchReturnsEmptyPage() {
    // Arrange
    ProductSecurityCriteria criteria = ProductSecurityCriteria.builder()
        .searchText("nonexistent-repo")
        .build();
    Pageable pageable = mock(Pageable.class);

    when(productSecurityInfoRepository.searchProductSecurityAndSorting(criteria, pageable))
        .thenReturn(org.springframework.data.domain.Page.empty());

    // Act
    Page<ProductSecurityInfo> result = gitHubService.searchSecurityDetails(criteria, pageable);

    // Assert
    assertNotNull(result, "Expected non-null page result even when no repos match");
    assertTrue(result.isEmpty(), "Expected empty page when no repositories match the search text");
    verify(productSecurityInfoRepository).searchProductSecurityAndSorting(criteria, pageable);
  }

  @Test
  void testSyncSecurityDetailsForProductShouldReturnSavedList() throws IOException {
    // Arrange
    GHOrganization mockOrg = mock(GHOrganization.class);
    GHRepository mockRepo = mock(GHRepository.class);
    PagedIterable<GHRepository> pagedRepos = mock(PagedIterable.class);
    ProductSecurityInfo mockInfo = buildMockProductSecurityInfo("test-repo");

    when(appSettingService.getStringValueByKey(AppSettingKey.GITHUB_TOKEN)).thenReturn("token");
    doReturn(gitHub).when(gitHubService).getGitHub("token");
    when(gitHub.getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME)).thenReturn(mockOrg);
    when(mockOrg.listRepositories()).thenReturn(pagedRepos);
    when(pagedRepos.toList()).thenReturn(List.of(mockRepo));
    when(multiTaskUtils.parallelProcessWithLimit(anyCollection(), any(), anyInt())).thenReturn(List.of(mockInfo));

    doReturn(mockInfo).when(gitHubService).fetchSecurityInfoSafe(mockRepo, mockOrg, "token");
    when(productSecurityInfoRepository.saveAll(anyList())).thenReturn(List.of(mockInfo));

    // Act
    List<ProductSecurityInfo> result = gitHubService.syncSecurityDetailsForProduct();

    // Assert
    assertNotNull(result, "Expected non-null result list when one repository is processed");
    assertEquals(1, result.size(), "Expected exactly one ProductSecurityInfo in the result list");
    assertEquals("test-repo", result.get(0).getRepoName(), "Expected saved repo name to match mocked value");
    verify(productSecurityInfoRepository).saveAll(anyList());
  }

  @Test
  void testSyncSecurityDetailsForProductShouldHandleMultipleRepos() throws IOException {
    // Arrange
    GHOrganization mockOrg = mock(GHOrganization.class);
    GHRepository repoA = mock(GHRepository.class);
    GHRepository repoB = mock(GHRepository.class);
    GHRepository repoC = mock(GHRepository.class);
    PagedIterable<GHRepository> pagedRepos = mock(PagedIterable.class);
    ProductSecurityInfo infoA = buildMockProductSecurityInfo("repo-a");
    ProductSecurityInfo infoB = buildMockProductSecurityInfo("repo-b");
    ProductSecurityInfo infoC = buildMockProductSecurityInfo("repo-c");

    when(appSettingService.getStringValueByKey(AppSettingKey.GITHUB_TOKEN)).thenReturn("token");
    doReturn(gitHub).when(gitHubService).getGitHub("token");
    when(gitHub.getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME)).thenReturn(mockOrg);
    when(mockOrg.listRepositories()).thenReturn(pagedRepos);
    when(pagedRepos.toList()).thenReturn(List.of(repoA, repoB, repoC));
    when(multiTaskUtils.parallelProcessWithLimit(anyCollection(), any(), anyInt()))
        .thenReturn(List.of(infoA, infoB, infoC));

    doReturn(infoA).when(gitHubService).fetchSecurityInfoSafe(repoA, mockOrg, "token");
    doReturn(infoB).when(gitHubService).fetchSecurityInfoSafe(repoB, mockOrg, "token");
    doReturn(infoC).when(gitHubService).fetchSecurityInfoSafe(repoC, mockOrg, "token");

    when(productSecurityInfoRepository.saveAll(anyList())).thenReturn(List.of(infoA, infoB, infoC));

    // Act
    List<ProductSecurityInfo> result = gitHubService.syncSecurityDetailsForProduct();

    // Assert
    assertEquals(3, result.size(), "Expected all three repositories to be processed and returned");
    verify(productSecurityInfoRepository).saveAll(anyList());
  }

  @Test
  void testSyncSecurityDetailsForProductWhenEmptyOrgShouldReturnEmptyList() throws IOException {
    // Arrange
    GHOrganization mockOrg = mock(GHOrganization.class);
    PagedIterable<GHRepository> pagedRepos = mock(PagedIterable.class);

    when(appSettingService.getStringValueByKey(AppSettingKey.GITHUB_TOKEN)).thenReturn("token");
    doReturn(gitHub).when(gitHubService).getGitHub("token");
    when(gitHub.getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME)).thenReturn(mockOrg);
    when(mockOrg.listRepositories()).thenReturn(pagedRepos);
    when(pagedRepos.toList()).thenReturn(Collections.emptyList());
    when(multiTaskUtils.parallelProcessWithLimit(anyCollection(), any(), anyInt())).thenReturn(Collections.emptyList());
    // Act
    List<ProductSecurityInfo> result = gitHubService.syncSecurityDetailsForProduct();

    // Assert
    assertNotNull(result, "Expected non-null result list even when organization has no repositories");
    assertTrue(result.isEmpty(), "Expected empty result list when no repositories are available");
    verify(productSecurityInfoRepository).saveAll(Collections.emptyList());
  }

  @Test
  void testFetchSecurityInfoSafeWhenSuccessShouldReturnInfo() {
    // Arrange
    GHOrganization mockOrg = mock(GHOrganization.class);
    GHRepository mockRepo = mock(GHRepository.class);
    ProductSecurityInfo mockInfo = buildMockProductSecurityInfo("repo-x");

    doReturn(mockInfo).when(gitHubService).fetchSecurityInfoSafe(mockRepo, mockOrg, "token");

    // Act
    ProductSecurityInfo result = gitHubService.fetchSecurityInfoSafe(mockRepo, mockOrg, "token");

    // Assert
    assertNotNull(result, "Expected non-null ProductSecurityInfo when fetch succeeds");
    assertEquals("repo-x", result.getRepoName(), "Expected repo name to match mocked security info");
  }

  private ProductSecurityInfo buildMockProductSecurityInfo(String repoName) {
    ProductSecurityInfo info = new ProductSecurityInfo();
    info.setRepoName(repoName);
    Dependabot dependabot = new Dependabot();
    dependabot.setStatus(AccessLevel.ENABLED);
    info.setDependabot(dependabot);
    SecretScanning secretScanning = new SecretScanning();
    secretScanning.setNumberOfSecretScanningAlerts(0);
    info.setSecretScanning(secretScanning);
    CodeScanning codeScanning = new CodeScanning();
    codeScanning.setStatus(AccessLevel.ENABLED);
    info.setCodeScanning(codeScanning);
    return info;
  }

  @Test
  void testArchiveTheRepositoryWhenNotArchivedShouldArchive() throws IOException {
    GHRepository mockRepo = mock(GHRepository.class);
    doReturn(mockRepo).when(gitHubService).getRepository("org/repo");
    when(mockRepo.isArchived()).thenReturn(false);

    gitHubService.archiveTheRepository("org/repo");

    verify(mockRepo).archive();
  }

  @Test
  void testArchiveTheRepositoryWhenAlreadyArchivedShouldNotArchive() throws IOException {
    GHRepository mockRepo = mock(GHRepository.class);
    doReturn(mockRepo).when(gitHubService).getRepository("org/repo");
    when(mockRepo.isArchived()).thenReturn(true);

    gitHubService.archiveTheRepository("org/repo");

    verify(mockRepo, never()).archive();
  }

  @Test
  void testHasDeprecationWarningInReadmeWhenRepositoryIsNullShouldReturnFalse() throws IOException {
    doReturn(null).when(gitHubService).getRepository("org/repo");

    boolean result = gitHubService.hasDeprecationWarningInReadme("org/repo");

    assertFalse(result, "Expected false when repository is null");
  }

  @Test
  void testHasDeprecationWarningInReadmeWhenReadmeContainsNoticeShouldReturnTrue() throws IOException {
    String readmeWithNotice = """
        # My Project
        
        > [!CAUTION]
        > ## Deprecated
        > These connectors are deprecated and will no longer be maintained or supported. It will be removed in Release 10.0.0.
        
        Some content here.
        """;
    setupDeprecationReadmeMocks(readmeWithNotice);

    boolean result = gitHubService.hasDeprecationWarningInReadme("org/repo");

    assertTrue(result, "Expected true when README contains deprecation notice");
  }

  @Test
  void testHasDeprecationWarningInReadmeWhenReadmeDoesNotContainNoticeShouldReturnFalse() throws IOException {
    String readmeWithoutNotice = """
        # My Project
        
        This is a normal README without any deprecation notice.
        """;
    setupDeprecationReadmeMocks(readmeWithoutNotice);

    boolean result = gitHubService.hasDeprecationWarningInReadme("org/repo");

    assertFalse(result, "Expected false when README does not contain deprecation notice");
  }

  @Test
  void testUnArchivedTheRepositoryWhenSuccessful() throws IOException {
    when(appSettingService.getStringValueByKey(AppSettingKey.GITHUB_TOKEN)).thenReturn("test-token");
    String url = GitHubConstants.Url.REPOS_BASE_URL + "org/repo";
    server.expect(requestTo(url))
        .andExpect(method(org.springframework.http.HttpMethod.PATCH))
        .andRespond(withNoContent());

    gitHubService.unArchivedTheRepository("org/repo");

    verify(appSettingService).getStringValueByKey(AppSettingKey.GITHUB_TOKEN);
  }

  @Test
  void testUnArchivedTheRepositoryWhenResponseNotSuccessful() throws IOException {
    when(appSettingService.getStringValueByKey(AppSettingKey.GITHUB_TOKEN)).thenReturn("test-token");
    String url = GitHubConstants.Url.REPOS_BASE_URL + "org/repo";
    server.expect(requestTo(url))
        .andExpect(method(org.springframework.http.HttpMethod.PATCH))
        .andRespond(withStatus(HttpStatus.FORBIDDEN));

    assertThrows(UnarchiveFailedException.class, () -> gitHubService.unArchivedTheRepository("org/repo"),
        "Expected UnarchiveFailedException when GitHub API returns a non-successful response");
  }

  @Test
  void testUnArchivedTheRepositoryWhenIOExceptionThrown() throws IOException {
    when(appSettingService.getStringValueByKey(AppSettingKey.GITHUB_TOKEN)).thenReturn("test-token");
    String url = GitHubConstants.Url.REPOS_BASE_URL + "org/repo";
    server.expect(requestTo(url))
        .andExpect(method(org.springframework.http.HttpMethod.PATCH))
        .andRespond(withServerError());

    assertThrows(UnarchiveFailedException.class, () -> gitHubService.unArchivedTheRepository("org/repo"),
        "Expected UnarchiveFailedException when an IOException occurs during the unarchive request");
  }

  private void setupDeprecationReadmeMocks(String readmeContent) throws IOException {
    GHRepository mockRepo = mock(GHRepository.class);
    GHContent mockReadme = mock(GHContent.class);
    doReturn(mockRepo).when(gitHubService).getRepository("org/repo");
    when(mockRepo.getDefaultBranch()).thenReturn("main");
    when(mockRepo.getFileContent(README_FILE_PATH, "main")).thenReturn(mockReadme);
    when(mockReadme.read()).thenReturn(new ByteArrayInputStream(readmeContent.getBytes(StandardCharsets.UTF_8)));
  }

  private void setupBaseRepositoryMocks(GHRepository repository, GHContent readme, String readmeContent)
      throws Exception {
    when(appSettingService.getStringValueByKey(AppSettingKey.GITHUB_TOKEN)).thenReturn("token");
    doReturn(gitHub).when(gitHubService).getGitHub("token");
    when(gitHub.getRepository("org/repo")).thenReturn(repository);
    when(repository.getDefaultBranch()).thenReturn(BASE_BRANCH);
    when(repository.getFileContent(README_FILE_PATH, BASE_BRANCH)).thenReturn(readme);
    when(readme.read()).thenReturn(new ByteArrayInputStream(readmeContent.getBytes()));
  }

  private void mockOpenPullRequests(GHRepository repository, List<GHPullRequest> pullRequests) throws IOException {
    GHPullRequestQueryBuilder pullRequestQueryBuilder = mock(GHPullRequestQueryBuilder.class);
    PagedIterable<GHPullRequest> pagedPullRequests = mock(PagedIterable.class);
    when(repository.queryPullRequests()).thenReturn(pullRequestQueryBuilder);
    when(pullRequestQueryBuilder.base(anyString())).thenReturn(pullRequestQueryBuilder);
    when(pullRequestQueryBuilder.head(anyString())).thenReturn(pullRequestQueryBuilder);
    when(pullRequestQueryBuilder.state(GHIssueState.OPEN)).thenReturn(pullRequestQueryBuilder);
    when(pullRequestQueryBuilder.list()).thenReturn(pagedPullRequests);
    when(pagedPullRequests.toList()).thenReturn(pullRequests);
  }

}
