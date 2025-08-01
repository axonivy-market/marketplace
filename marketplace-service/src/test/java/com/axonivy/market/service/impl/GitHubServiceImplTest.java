package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.AccessLevel;
import com.axonivy.market.exceptions.model.MissingHeaderException;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.*;
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

  @Spy
  @InjectMocks
  private GitHubServiceImpl gitHubService;

  @Test
  void testGetGitHub_WithValidToken() throws IOException {
    when(gitHubProperty.getToken()).thenReturn("validToken");
    assertNotNull(gitHubService.getGitHub());
    verify(gitHubProperty).getToken();
  }

  @Test
  void testGetGitHub_WithNullToken() throws IOException {
    when(gitHubProperty.getToken()).thenReturn(null);
    assertNotNull(gitHubService.getGitHub());
  }

  @Test
  void testGetOrganization_WithValidOrgName() throws IOException {
    when(gitHubService.getGitHub()).thenReturn(gitHub);
    GHOrganization mockOrganization = mock(GHOrganization.class);
    when(gitHub.getOrganization("test-org")).thenReturn(mockOrganization);
    GHOrganization organization = gitHubService.getOrganization("test-org");
    assertNotNull(organization);
    verify(gitHubProperty).getToken();
    verify(gitHubService).getGitHub();
    verify(gitHub).getOrganization("test-org");
  }

  @Test
  void testGetDirectoryContent_ValidInputs() throws IOException {
    GHRepository mockRepository = mock(GHRepository.class);
    String path = "src";
    String ref = "main";

    List<GHContent> mockContents = List.of(mock(GHContent.class), mock(GHContent.class));
    when(mockRepository.getDirectoryContent(path, ref)).thenReturn(mockContents);

    List<GHContent> contents = gitHubService.getDirectoryContent(mockRepository, path, ref);

    assertNotNull(contents);
    assertEquals(2, contents.size());
    verify(mockRepository).getDirectoryContent(path, ref);
  }

  @Test
  void testGetDirectoryContent_NullRepository() {
    String path = "src";
    String ref = "main";

    assertThrows(IllegalArgumentException.class,
        () -> gitHubService.getDirectoryContent(null, path, ref));
  }

  @Test
  void testGetRepository_ValidRepositoryPath() throws IOException {
    String repositoryPath = "my-org/my-repo";
    GHRepository mockRepository = mock(GHRepository.class);
    when(gitHubService.getGitHub()).thenReturn(mock(GitHub.class));
    when(gitHubService.getGitHub().getRepository(repositoryPath)).thenReturn(mockRepository);

    GHRepository repository = gitHubService.getRepository(repositoryPath);

    assertNotNull(repository);
    verify(gitHubService.getGitHub()).getRepository(repositoryPath);
  }

  @Test
  void testGetGHContent_ValidInputs() throws IOException {
    GHRepository mockRepository = mock(GHRepository.class);
    String path = "README.md";
    String ref = "main";
    GHContent mockContent = mock(GHContent.class);

    when(mockRepository.getFileContent(path, ref)).thenReturn(mockContent);

    GHContent content = gitHubService.getGHContent(mockRepository, path, ref);

    assertNotNull(content);
    verify(mockRepository).getFileContent(path, ref);
  }

  @Test
  void testGetGHContent_NullRepository() {
    String path = "README.md";
    String ref = "main";

    assertThrows(IllegalArgumentException.class,
        () -> gitHubService.getGHContent(null, path, ref));
  }

  @Test
  void testGetAccessToken_ValidCodeAndGitHubProperty() throws Exception {
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

    assertNotNull(result);
    assertEquals(accessToken, result.getAccessToken());
    verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(GitHubAccessTokenResponse.class));
  }

  @Test
  void testGetAccessToken_NullGitHubProperty() {
    MissingHeaderException exception = assertThrows(MissingHeaderException.class, () ->
        gitHubService.getAccessToken("validCode", null)
    );
    assertEquals("Invalid or missing header", exception.getMessage());
  }

  @Test
  void testGetAccessToken_GitHubErrorResponse() throws Oauth2ExchangeCodeException {
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

    Oauth2ExchangeCodeException exception = assertThrows(Oauth2ExchangeCodeException.class, () ->
        gitHubService.getAccessToken(code, gitHubProperty)
    );
    assertEquals(error, exception.getError());
    assertEquals(errorDescription, exception.getErrorDescription());
  }

  @Test
  void testGetAccessToken_SuccessfulResponseWithError() throws Oauth2ExchangeCodeException {
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

    Oauth2ExchangeCodeException exception = assertThrows(Oauth2ExchangeCodeException.class, () ->
        gitHubService.getAccessToken(code, gitHubProperty)
    );
    assertEquals("error_code", exception.getError());
    assertEquals("Error description", exception.getErrorDescription());
  }

  @Test
  void testValidateUserInOrganizationAndTeam_Valid() throws Exception {
    String accessToken = "validToken";
    String organization = "testOrg";
    String team = "devTeam";

    when(gitHubService.getGitHub(accessToken)).thenReturn(gitHub);

    when(gitHubService.isUserInOrganizationAndTeam(gitHub, organization, team)).thenReturn(true);

    gitHubService.validateUserInOrganizationAndTeam(accessToken, organization, team);
  }

  @Test
  void testIsUserInOrganizationAndTeam_NullGitHub() throws IOException {
    GitHub gitHubNullAble = null;
    String organization = "my-org";
    String teamName = "my-team";

    boolean result = gitHubService.isUserInOrganizationAndTeam(gitHubNullAble, organization, teamName);

    assertFalse(result);
  }

  @Test
  void testIsUserInOrganizationAndTeam_EmptyTeams() throws IOException {
    String organization = "my-org";
    String teamName = "my-team";
    Map<String, Set<GHTeam>> hashMapTeams = new HashMap<>();
    when(gitHub.getMyTeams()).thenReturn(hashMapTeams);

    boolean result = gitHubService.isUserInOrganizationAndTeam(gitHub, organization, teamName);

    assertFalse(result);
  }

  @Test
  void testIsUserInOrganizationAndTeam_TeamNotFound() throws IOException {
    String organization = "my-org";
    String teamName = "my-team";
    Set<GHTeam> teams = new HashSet<>();
    teams.add(ghTeam);
    Map<String, Set<GHTeam>> hashMapTeams = new HashMap<>();
    hashMapTeams.put(organization, teams);
    when(gitHub.getMyTeams()).thenReturn(hashMapTeams);

    boolean result = gitHubService.isUserInOrganizationAndTeam(gitHub, organization, teamName);

    assertFalse(result);
  }

  @Test
  void testIsUserInOrganizationAndTeam_TeamFound() throws IOException {
    String organization = "my-org";
    String teamName = "my-team";
    Set<GHTeam> teams = new HashSet<>();
    when(ghTeam.getName()).thenReturn(teamName);
    teams.add(ghTeam);
    Map<String, Set<GHTeam>> hashMapTeams = new HashMap<>();
    hashMapTeams.put(organization, teams);
    when(gitHub.getMyTeams()).thenReturn(hashMapTeams);

    boolean result = gitHubService.isUserInOrganizationAndTeam(gitHub, organization, teamName);

    assertTrue(result);
  }

  @Test
  void testIsUserInOrganizationAndTeam_TeamListNull() throws IOException {
    String organization = "my-org";
    String teamName = "my-team";
    Map<String, Set<GHTeam>> hashMapTeams = new HashMap<>();
    hashMapTeams.put(organization, null);
    when(gitHub.getMyTeams()).thenReturn(hashMapTeams);

    boolean result = gitHubService.isUserInOrganizationAndTeam(gitHub, organization, teamName);

    assertFalse(result);
  }

  @Test
  void testValidateUserInOrganizationAndTeam_throwsUnauthorizedException() throws IOException {
    String accessToken = "invalid-token";
    String organization = "orgName";
    String team = "teamName";

    GitHub gitHubMock = mock(GitHub.class);
    when(gitHubService.getGitHub(accessToken)).thenReturn(gitHubMock);
    when(gitHubService.isUserInOrganizationAndTeam(gitHubMock, organization, team)).thenReturn(false);
    UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> {
      gitHubService.validateUserInOrganizationAndTeam(accessToken, organization, team);
    });
    assertEquals("GITHUB_USER_UNAUTHORIZED - User must be a member of team teamName and organization orgName", exception.getMessage());
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
    assertNotNull(result);
  }

  @Test
  void testGetNumberOfSecretScanningAlerts_Success() {
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
    assertEquals(AccessLevel.ENABLED, result.getStatus());
  }

  @Test
  void testGetCodeScanningAlerts_Success() {
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
    assertEquals(AccessLevel.ENABLED, result.getStatus());
  }

  @Test
  void testGetDependabotAlerts_Success() {
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
    assertEquals(AccessLevel.ENABLED, result.getStatus());
  }

  @Test
  void testGetDependabotAlerts_NotFound() {
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
    assertEquals(AccessLevel.NO_PERMISSION, result.getStatus());
  }

  @Test
  void testGetDependabotAlerts_Disabled() {
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
    assertEquals(AccessLevel.DISABLED, result.getStatus());
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
    assertEquals(0, result.size());
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

      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
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

      assertNotNull(result);
      verify(gitHubService, atLeastOnce()).getRepository(mockRepositoryName);
      verify(mockRepository).getRelease(1L);
    }
  }

  @Test
  void testTransformGithubReleaseBody() {
    String githubReleaseBody = "This is a release body with PR #123 and user @johndoe";
    String productSourceUrl = "http://example.com";

    String result = gitHubService.transformGithubReleaseBody(githubReleaseBody, productSourceUrl);

    assertNotNull(result);
    assertTrue(result.contains("http://example.com/pull/123"));
    assertTrue(result.contains("https://github.com/johndoe"));
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

      assertNotNull(result);
      assertEquals("v1.0.0", result.getName());
      assertTrue(result.getBody().contains("http://example.com"));
    }
  }

  @Test
  void testGetGitHubReleaseModelsWithMEmptyReleases() throws IOException {
    Product mockProduct = mock(Product.class);
    PagedIterable<GHRelease> mockPagedIterable = mock(PagedIterable.class);
    Pageable mockPageable = mock(Pageable.class);

    when(mockPagedIterable.toList()).thenReturn(Collections.emptyList());

    Page<GitHubReleaseModel> result = gitHubService.getGitHubReleaseModels(mockProduct, mockPagedIterable, mockPageable);

    assertNotNull(result);
    assertTrue(result.isEmpty());
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

}
