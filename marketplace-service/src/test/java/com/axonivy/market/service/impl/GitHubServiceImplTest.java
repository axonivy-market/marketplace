package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GithubUser;
import com.axonivy.market.entity.Product;
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
import com.axonivy.market.github.model.ProductSecurityInfo;
import com.axonivy.market.github.model.SecretScanning;
import com.axonivy.market.github.service.impl.GitHubServiceImpl;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.GitHubReleaseModel;
import com.axonivy.market.repository.GithubUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.*;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GitHubServiceImplTest {

  @Mock
  private GitHubProperty gitHubProperty;

  @Mock
  RestTemplate restTemplate;

  @Mock
  private GitHub gitHub;

  @Mock
  private ResponseEntity<GitHubAccessTokenResponse> responseEntity;

  @Mock
  private GitHubAccessTokenResponse gitHubAccessTokenResponse;

  @Mock
  private GHTeam ghTeam;

  @Mock
  private GithubUserRepository githubUserRepository;

  @Spy
  @InjectMocks
  private GitHubServiceImpl gitHubService;

  @Test
  void testGetGitHubWithValidToken() throws IOException {
    when(gitHubProperty.getToken()).thenReturn("validToken");
    assertNotNull(gitHubService.getGitHub(), "Expected GitHub object to be created with a valid token");
    verify(gitHubProperty).getToken();
  }

  @Test
  void testGetGitHubWithNullToken() throws IOException {
    when(gitHubProperty.getToken()).thenReturn(null);
    assertNotNull(gitHubService.getGitHub(), "Expected GitHub object to be created even when token is null");
  }

  @Test
  void testGetOrganizationWithValidOrgName() throws IOException {
    when(gitHubService.getGitHub()).thenReturn(gitHub);
    GHOrganization mockOrganization = mock(GHOrganization.class);
    when(gitHub.getOrganization("test-org")).thenReturn(mockOrganization);

    GHOrganization organization = gitHubService.getOrganization("test-org");

    assertNotNull(organization, "Expected organization to be returned for valid org name 'test-org'");

    verify(gitHubProperty).getToken();
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
    when(gitHubService.getGitHub()).thenReturn(mock(GitHub.class));
    when(gitHubService.getGitHub().getRepository(repositoryPath)).thenReturn(mockRepository);

    GHRepository repository = gitHubService.getRepository(repositoryPath);

    assertNotNull(repository, "Expected repository to be returned for valid repository path 'my-org/my-repo'");
    verify(gitHubService.getGitHub()).getRepository(repositoryPath);
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
  void testGetAccessTokenValidCodeAndGitHubProperty() throws Exception {
    String code = "validCode";
    String clientId = "clientId";
    String clientSecret = "clientSecret";
    String accessToken = "accessToken";

    when(gitHubProperty.getOauth2ClientId()).thenReturn(clientId);
    when(gitHubProperty.getOauth2ClientSecret()).thenReturn(clientSecret);

    when(responseEntity.getBody()).thenReturn(gitHubAccessTokenResponse);
    when(gitHubAccessTokenResponse.getError()).thenReturn(null);
    when(gitHubAccessTokenResponse.getAccessToken()).thenReturn(accessToken);

    when(restTemplate.postForEntity(
        eq(GitHubConstants.GITHUB_GET_ACCESS_TOKEN_URL),
        any(HttpEntity.class),
        eq(GitHubAccessTokenResponse.class)
    )).thenReturn(responseEntity);

    GitHubAccessTokenResponse result = gitHubService.getAccessToken(code, gitHubProperty);

    assertNotNull(result, "Expected non-null GitHubAccessTokenResponse when valid code and properties are provided");
    assertEquals(accessToken, result.getAccessToken(),
        "Expected access token in response to match the mocked access token");

    verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(GitHubAccessTokenResponse.class));
  }


  @Test
  void testGetAccessTokenNullGitHubProperty() {
    MissingHeaderException exception = assertThrows(
        MissingHeaderException.class,
        () -> gitHubService.getAccessToken("validCode", null),
        "Expected MissingHeaderException when GitHubProperty is null"
    );

    assertEquals("Invalid or missing header", exception.getMessage(),
        "Expected exception message to be 'Invalid or missing header'");
  }


  @Test
  void testGetAccessTokenGitHubErrorResponse() throws Oauth2ExchangeCodeException {
    String code = "validCode";
    String clientId = "clientId";
    String clientSecret = "clientSecret";
    String error = "invalid_grant";
    String errorDescription = "The authorization code is invalid";

    when(gitHubProperty.getOauth2ClientId()).thenReturn(clientId);
    when(gitHubProperty.getOauth2ClientSecret()).thenReturn(clientSecret);

    when(responseEntity.getBody()).thenReturn(gitHubAccessTokenResponse);
    when(gitHubAccessTokenResponse.getError()).thenReturn(error);
    when(gitHubAccessTokenResponse.getErrorDescription()).thenReturn(errorDescription);

    when(restTemplate.postForEntity(
        eq(GitHubConstants.GITHUB_GET_ACCESS_TOKEN_URL),
        any(HttpEntity.class),
        eq(GitHubAccessTokenResponse.class)
    )).thenReturn(responseEntity);

    Oauth2ExchangeCodeException exception = assertThrows(
        Oauth2ExchangeCodeException.class,
        () -> gitHubService.getAccessToken(code, gitHubProperty),
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

    when(gitHubProperty.getOauth2ClientId()).thenReturn(clientId);
    when(gitHubProperty.getOauth2ClientSecret()).thenReturn(clientSecret);

    when(responseEntity.getBody()).thenReturn(gitHubAccessTokenResponse);
    when(gitHubAccessTokenResponse.getError()).thenReturn("error_code");
    when(gitHubAccessTokenResponse.getErrorDescription()).thenReturn("Error description");

    when(restTemplate.postForEntity(
        eq(GitHubConstants.GITHUB_GET_ACCESS_TOKEN_URL),
        any(HttpEntity.class),
        eq(GitHubAccessTokenResponse.class)
    )).thenReturn(responseEntity);

    Oauth2ExchangeCodeException exception = assertThrows(
        Oauth2ExchangeCodeException.class,
        () -> gitHubService.getAccessToken(code, gitHubProperty),
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

    when(gitHubService.getGitHub(accessToken)).thenReturn(gitHub);

    when(gitHubService.isUserInOrganizationAndTeam(gitHub, organization, team)).thenReturn(true);

    gitHubService.validateUserInOrganizationAndTeam(accessToken, organization, team);
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
    when(gitHubService.getGitHub(accessToken)).thenReturn(gitHubMock);
    when(gitHubService.isUserInOrganizationAndTeam(gitHubMock, organization, team)).thenReturn(false);

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
    List<Map<String, Object>> responseBodySecretScanning = new ArrayList<>();
    responseBodySecretScanning.add(Map.of(
        "number", 1
    ));
    when(restTemplate.exchange(
        eq(urlSecretScanning),
        eq(HttpMethod.GET),
        any(HttpEntity.class),
        any(ParameterizedTypeReference.class))
    ).thenReturn(new ResponseEntity<>(responseBodySecretScanning, HttpStatus.OK));
    String urlCodeScanning = String.format(GitHubConstants.Url.REPO_CODE_SCANNING_ALERTS_OPEN, orgName, repoName);
    List<Map<String, Object>> responseBodyCodeScanning = new ArrayList<>();
    responseBodyCodeScanning.add(Map.of(
        "number", 1,
        "state", "open",
        "rule", Map.of("security_severity_level", "high")
    ));
    when(restTemplate.exchange(
        eq(urlCodeScanning),
        eq(HttpMethod.GET),
        any(HttpEntity.class),
        any(ParameterizedTypeReference.class))
    ).thenReturn(new ResponseEntity<>(responseBodyCodeScanning, HttpStatus.OK));

    String urlDependabotScanning = String.format(GitHubConstants.Url.REPO_DEPENDABOT_ALERTS_OPEN, orgName, repoName);
    List<Map<String, Object>> responseBodyDependabotScanning = new ArrayList<>();
    responseBodyDependabotScanning.add(Map.of(
        "number", 1,
        "state", "open",
        "security_advisory", Map.of("severity", "high")
    ));
    when(restTemplate.exchange(
        eq(urlDependabotScanning),
        eq(HttpMethod.GET),
        any(HttpEntity.class),
        any(ParameterizedTypeReference.class))
    ).thenReturn(new ResponseEntity<>(responseBodyDependabotScanning, HttpStatus.OK));

    ProductSecurityInfo result = gitHubService.fetchSecurityInfoSafe(ghRepository, ghOrganization, accessToken);
    assertNotNull(result, "Expected non-null ProductSecurityInfo result when security scanning responses are mocked");
  }

  @Test
  void testGetNumberOfSecretScanningAlertsSuccess() {
    String accessToken = "accessToken";
    String orgName = "orgName";
    String repoName = "repoName";
    String url = String.format(GitHubConstants.Url.REPO_SECRET_SCANNING_ALERTS_OPEN, orgName, repoName);
    List<Map<String, Object>> responseBody = new ArrayList<>();
    responseBody.add(Map.of(
        "number", 1
    ));
    GHRepository ghRepository = mock(GHRepository.class);
    GHOrganization ghOrganization = mock(GHOrganization.class);
    when(ghOrganization.getLogin()).thenReturn(orgName);
    when(ghRepository.getName()).thenReturn(repoName);
    when(restTemplate.exchange(
        eq(url),
        eq(HttpMethod.GET),
        any(HttpEntity.class),
        any(ParameterizedTypeReference.class))
    ).thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
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
    List<Map<String, Object>> responseBody = new ArrayList<>();
    responseBody.add(Map.of(
        "number", 1,
        "state", "open",
        "rule", Map.of("security_severity_level", "high")
    ));
    GHRepository ghRepository = mock(GHRepository.class);
    GHOrganization ghOrganization = mock(GHOrganization.class);
    when(ghOrganization.getLogin()).thenReturn(orgName);
    when(ghRepository.getName()).thenReturn(repoName);
    when(restTemplate.exchange(
        eq(url),
        eq(HttpMethod.GET),
        any(HttpEntity.class),
        any(ParameterizedTypeReference.class))
    ).thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
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
    List<Map<String, Object>> responseBody = new ArrayList<>();
    responseBody.add(Map.of(
        "number", 1,
        "state", "open",
        "security_advisory", Map.of("severity", "high")
    ));
    GHRepository ghRepository = mock(GHRepository.class);
    GHOrganization ghOrganization = mock(GHOrganization.class);
    when(ghOrganization.getLogin()).thenReturn(orgName);
    when(ghRepository.getName()).thenReturn(repoName);
    when(restTemplate.exchange(
        eq(url),
        eq(HttpMethod.GET),
        any(HttpEntity.class),
        any(ParameterizedTypeReference.class))
    ).thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
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
    when(restTemplate.exchange(
        eq(url),
        eq(HttpMethod.GET),
        any(HttpEntity.class),
        any(ParameterizedTypeReference.class))
    ).thenThrow(HttpClientErrorException.NotFound.class);
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

    when(restTemplate.exchange(
        eq(url),
        eq(HttpMethod.GET),
        any(HttpEntity.class),
        any(ParameterizedTypeReference.class))
    ).thenThrow(HttpClientErrorException.Forbidden.class);

    Dependabot result = gitHubService.getDependabotAlerts(ghRepository, ghOrganization, accessToken);

    assertEquals(
        AccessLevel.DISABLED,
        result.getStatus(),
        "Expected Dependabot alerts status to be DISABLED when the API returns 403 Forbidden"
    );
  }

  @Test
  void testGetSecurityDetailsForAllProducts() throws Exception {
    String accessToken = "mockAccessToken";
    String orgName = "mockOrganization";
    GHOrganization ghOrganization = mock(GHOrganization.class);
    when(gitHubService.getGitHub(accessToken)).thenReturn(gitHub);
    when(gitHub.getOrganization(orgName)).thenReturn(ghOrganization);
    PagedIterable<GHRepository> mockPagedIterable = mock(PagedIterable.class);
    when(ghOrganization.listRepositories()).thenReturn(mockPagedIterable);
    List<ProductSecurityInfo> result = gitHubService.getSecurityDetailsForAllProducts(accessToken, orgName);
    assertEquals(
        0,
        result.size(),
        "Expected no security details when the organization has no repositories"
    );
  }

  @Test
  void testGetGitHubReleaseModelsWithMixedReleases() throws IOException, URISyntaxException {
    try(MockedStatic<GitHubUtils> gitHubUtilsMockedStatic = Mockito.mockStatic(GitHubUtils.class)) {
      Link mockLink = mock(Link.class);
      gitHubUtilsMockedStatic.when(() -> GitHubUtils.createSelfLinkForGithubReleaseModel(any(), any())).thenReturn(mockLink);

      Product mockProduct = mock(Product.class);
      GHRepository mockRepository = mock(GHRepository.class);
      PagedIterable<GHRelease> mockPagedIterable = mock(PagedIterable.class);
      GHRelease mockLatestRelease = mock(GHRelease.class);

      String mockGithubReleaseBody = "This is a release body with PR #123 and user @johndoe";
      String mockProductSourceUrl = "http://example.com";
      String mockVersion = "v1.0.0";
      String mockRepositoryName = "axonivy-market/portal";
      URL mockHtmlUrl1 = new URI("http://example.com/portal/releases/tag/next-12.0").toURL();

      when(mockProduct.getRepositoryName()).thenReturn(mockRepositoryName);
      when(gitHubService.getGitHub()).thenReturn(mock(GitHub.class));
      when(gitHubService.getGitHub().getRepository(mockRepositoryName)).thenReturn(mockRepository);
      when(gitHubService.getGitHubLatestReleaseByProductId(mockProduct.getRepositoryName())).thenReturn(mockLatestRelease);

      Pageable mockPageable = mock(Pageable.class);
      GHRelease mockRelease1 = mock(GHRelease.class);
      GHRelease mockRelease2 = mock(GHRelease.class);

      when(mockRelease1.isDraft()).thenReturn(false);
      when(mockRelease2.isDraft()).thenReturn(true);
      when(mockRelease1.getBody()).thenReturn(mockGithubReleaseBody);
      when(mockRelease1.getName()).thenReturn(mockVersion);
      when(mockRelease1.getPublished_at()).thenReturn(new Date());
      when(mockRelease1.getHtmlUrl()).thenReturn(mockHtmlUrl1);
      when(mockProduct.getSourceUrl()).thenReturn(mockProductSourceUrl);
      when(mockPagedIterable.toList()).thenReturn(List.of(mockRelease1, mockRelease2));
      when(mockLatestRelease.getName()).thenReturn(mockVersion);
      when(gitHubService.toGitHubReleaseModel(mockRelease1, mockProduct, mockLatestRelease.getName())).thenReturn(new GitHubReleaseModel());

      Page<GitHubReleaseModel> result = gitHubService.getGitHubReleaseModels(mockProduct, mockPagedIterable, mockPageable);

      assertNotNull(result, "Expected non-null Page of GitHubReleaseModel");
      assertEquals(
          1,
          result.getTotalElements(),
          "Expected only 1 release element because drafts should be excluded"
      );
    }
  }

  @Test
  void testGetGitHubReleaseModelByProductIdAndReleaseId() throws IOException, URISyntaxException {
    try (MockedStatic<GitHubUtils> gitHubUtilsMockedStatic = Mockito.mockStatic(GitHubUtils.class)) {
      Link mockLink = mock(Link.class);
      gitHubUtilsMockedStatic.when(() -> GitHubUtils.createSelfLinkForGithubReleaseModel(any(), any())).thenReturn(mockLink);

      String mockGithubReleaseBody = "This is a release body with PR #123 and user @johndoe";
      String mockProductSourceUrl = "http://example.com";
      String mockRepositoryName = "axonivy-market/portal";
      URL mockHtmlUrl = new URI("http://example.com/portal/releases/tag/next-12.0").toURL();
      String mockVersion = "v1.0.0";

      Product mockProduct = mock(Product.class);
      GHRepository mockRepository = mock(GHRepository.class);
      GHRelease mockRelease = mock(GHRelease.class);
      GHRelease mockLatestRelease = mock(GHRelease.class);

      when(mockProduct.getRepositoryName()).thenReturn(mockRepositoryName);
      when(gitHubService.getGitHub()).thenReturn(mock(GitHub.class));
      when(gitHubService.getGitHub().getRepository(mockRepositoryName)).thenReturn(mockRepository);
      when(gitHubService.getGitHubLatestReleaseByProductId(mockProduct.getRepositoryName())).thenReturn(mockLatestRelease);
      when(mockRepository.getRelease(1L)).thenReturn(mockRelease);
      when(mockRelease.getBody()).thenReturn(mockGithubReleaseBody);
      when(mockRelease.getName()).thenReturn(mockVersion);
      when(mockRelease.getPublished_at()).thenReturn(new Date());
      when(mockRelease.getHtmlUrl()).thenReturn(mockHtmlUrl);
      when(mockProduct.getSourceUrl()).thenReturn(mockProductSourceUrl);
      when(gitHubService.toGitHubReleaseModel(mockRelease, mockProduct, mockLatestRelease.getName())).thenReturn(new GitHubReleaseModel());

      GitHubReleaseModel result = gitHubService.getGitHubReleaseModelByProductIdAndReleaseId(mockProduct, 1L);

      assertNotNull(result, "Expected non-null GitHubReleaseModel when fetching by product ID and release ID");
      verify(gitHubService, atLeastOnce()).getRepository(mockRepositoryName);
      verify(mockRepository).getRelease(1L);
    }
  }

  @Test
  void testTransformGithubReleaseBody() {
    String githubReleaseBody = "This is a release body with PR #123 and user @johndoe";
    String productSourceUrl = "http://example.com";

    String result = gitHubService.transformGithubReleaseBody(githubReleaseBody, productSourceUrl);

    assertNotNull(result, "Expected transformed release body to be non-null");
    assertTrue(result.contains("http://example.com/pull/123"),
        "Expected transformed release body to contain the correct pull request link");
    assertTrue(result.contains("https://github.com/johndoe"),
        "Expected transformed release body to contain the correct GitHub user link");
  }

  @Test
  void testToGitHubReleaseModel() throws IOException, URISyntaxException {
    try(MockedStatic<GitHubUtils> gitHubUtilsMockedStatic = Mockito.mockStatic(GitHubUtils.class)) {
      Link mockLink = mock(Link.class);
      gitHubUtilsMockedStatic.when(() -> GitHubUtils.createSelfLinkForGithubReleaseModel(any(), any())).thenReturn(mockLink);

      GHRelease mockGhRelease = mock(GHRelease.class);
      GHRelease mockLatestRelease = mock(GHRelease.class);
      Product mockProduct = mock(Product.class);
      URL mockHtmlUrl = new URI("http://example.com/portal/releases/tag/next-12.0").toURL();

      when(mockGhRelease.getBody()).thenReturn("This is a release body with PR #123 and user @johndoe");
      when(mockGhRelease.getName()).thenReturn("v1.0.0");
      when(mockGhRelease.getPublished_at()).thenReturn(new Date());
      when(mockGhRelease.getHtmlUrl()).thenReturn(mockHtmlUrl);
      when(mockProduct.getSourceUrl()).thenReturn("http://example.com");

      GitHubReleaseModel result = gitHubService.toGitHubReleaseModel(mockGhRelease, mockProduct, mockLatestRelease.getName());

      assertNotNull(result, "Expected non-null GitHubReleaseModel after transformation");
      assertEquals("v1.0.0", result.getName(), "Expected GitHubReleaseModel name to match the release name");
      assertTrue(result.getBody().contains("http://example.com"),
          "Expected transformed release body to include the product source URL");
    }
  }

  @Test
  void testGetGitHubReleaseModelsWithMEmptyReleases() throws IOException {
    Product mockProduct = mock(Product.class);
    PagedIterable<GHRelease> mockPagedIterable = mock(PagedIterable.class);
    Pageable mockPageable = mock(Pageable.class);

    when(mockPagedIterable.toList()).thenReturn(Collections.emptyList());

    Page<GitHubReleaseModel> result = gitHubService.getGitHubReleaseModels(mockProduct, mockPagedIterable, mockPageable);

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
    when(gitHubService.getGitHub()).thenReturn(mockGitHub);
    when(mockGitHub.getRepository(repositoryPath)).thenReturn(mockRepository);

    GHRepository result = gitHubService.getRepository(repositoryPath);

    assertNotNull(result, "Expected non-null repository");
    verify(mockGitHub).getRepository(repositoryPath);
  }

  @Test
  void testGetRepositoryGhFileNotFoundException() throws IOException {
    when(gitHubService.getGitHub()).thenReturn(gitHub);
    when(gitHub.getRepository("missing/repo")).thenThrow(new GHFileNotFoundException());

    GHRepository result = gitHubService.getRepository("missing/repo");

    assertNull(result, "Expected null result when GHFileNotFoundException is thrown");
  }

  @Test
  void testGetRepositoryIOException() throws IOException {
    when(gitHubService.getGitHub()).thenReturn(gitHub);
    when(gitHub.getRepository("error/repo")).thenThrow(new IOException("IO error"));

    GHRepository result = gitHubService.getRepository("error/repo");

    assertNull(result, "Expected null result when IOException is thrown");
  }

  @Test
  void getAndUpdateUserShouldUpdateAndReturnUserWhenUserExists() throws Exception {
    String accessToken = "token";
    // Use anonymous class as a fake
    GHMyself myself = new GHMyself() {
      @Override public long getId() { return 123L; }
      @Override public String getName() { return "Test User"; }
      @Override public String getLogin() { return "testuser"; }
      @Override public String getAvatarUrl() { return "avatar_url"; }
    };
    when(gitHub.getMyself()).thenReturn(myself);
    doReturn(gitHub).when(gitHubService).getGitHub(accessToken);

    GithubUser existingUser = new GithubUser();
    existingUser.setGitHubId("123");
    when(githubUserRepository.searchByGitHubId("123")).thenReturn(existingUser);

    GithubUser result = gitHubService.getAndUpdateUser(accessToken);

    assertNotNull(result, "Returned GithubUser should not be null");
    assertEquals("123", result.getGitHubId(), "GitHubId should be set correctly");
    assertEquals("Test User", result.getName(), "Name should be updated from GHMyself");
    assertEquals("testuser", result.getUsername(), "Username should be updated from GHMyself");
    assertEquals("avatar_url", result.getAvatarUrl(), "Avatar URL should be updated from GHMyself");
    assertEquals(GitHubConstants.GITHUB_PROVIDER_NAME, result.getProvider(), "Provider should be set to 'github'");

    ArgumentCaptor<GithubUser> captor = ArgumentCaptor.forClass(GithubUser.class);
    verify(githubUserRepository, times(1)).save(captor.capture());
    assertEquals(result, captor.getValue(), "Saved user should match returned user");
  }

  @Test
  void testGetAndUpdateUserThrowsNotFoundExceptionOnIOException() throws Exception {
    // given
    String accessToken = "token";
    when(gitHubService.getGitHub(accessToken)).thenReturn(gitHub);
    when(gitHub.getMyself()).thenThrow(new IOException("GitHub API down"));

    // when + then
    NotFoundException ex = assertThrows(NotFoundException.class,
        () -> gitHubService.getAndUpdateUser(accessToken),
        "IOException should be translated into NotFoundException");

    assertEquals(ErrorCode.GITHUB_USER_NOT_FOUND.getHelpText() + CommonConstants.DASH_SEPARATOR + "Failed to fetch " +
            "user details from GitHub", ex.getMessage(),
        "Error message should be meaningful");
  }
}
