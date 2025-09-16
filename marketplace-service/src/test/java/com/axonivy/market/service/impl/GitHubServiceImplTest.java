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
import com.axonivy.market.util.ProductContentUtils;
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
import java.util.function.Consumer;

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
  void testToGitHubReleaseModel() throws IOException, URISyntaxException {
    // Arrange
    GHRelease mockGhRelease = mock(GHRelease.class);
    String productSourceUrl = "https://github.com/axonivy-market/test-product";
    String productId = "test-product-id";
    Boolean isLatestRelease = true;

    // Mock release data
    String releaseName = "v1.2.0";
    String releaseBody = "## What's Changed\n- Fixed critical bug #123\n- Added new feature XYZ";
    Date publishedDate = new Date();
    URL htmlUrl = new URI("https://github.com/axonivy-market/test-product/releases/tag/v1.2.0").toURL();
    long releaseId = 12345L;

    when(mockGhRelease.getName()).thenReturn(releaseName);
    when(mockGhRelease.getBody()).thenReturn(releaseBody);
    when(mockGhRelease.getPublished_at()).thenReturn(publishedDate);
    when(mockGhRelease.getHtmlUrl()).thenReturn(htmlUrl);
    // Fix: Use lenient() to avoid the internal Callable issue
    lenient().when(mockGhRelease.getId()).thenReturn(releaseId);

    // Mock static utilities
    String transformedBody = "## What's Changed\n- Fixed critical bug #123\n- Added new feature XYZ (transformed)";
    Link selfLink = Link.of("http://localhost/api/products/test-product-id/releases/12345").withSelfRel();

    try (MockedStatic<com.axonivy.market.util.ProductContentUtils> productContentUtilsMock =
         mockStatic(com.axonivy.market.util.ProductContentUtils.class);
         MockedStatic<GitHubUtils> gitHubUtilsMock = mockStatic(GitHubUtils.class)) {

      productContentUtilsMock.when(() ->
          com.axonivy.market.util.ProductContentUtils.transformGithubReleaseBody(releaseBody, productSourceUrl))
          .thenReturn(transformedBody);

      gitHubUtilsMock.when(() ->
          GitHubUtils.createSelfLinkForGithubReleaseModel(productId, releaseId))
          .thenReturn(selfLink);

      // Act
      GitHubReleaseModel result = gitHubService.toGitHubReleaseModel(
          mockGhRelease, productSourceUrl, productId, isLatestRelease);

      // Assert
      assertNotNull(result, "Result should not be null");
      assertEquals(releaseName, result.getName(), "Release name should match");
      assertEquals(transformedBody, result.getBody(), "Release body should be transformed");
      assertEquals(publishedDate, result.getPublishedAt(), "Published date should match");
      assertEquals(htmlUrl.toString(), result.getHtmlUrl(), "HTML URL should match");
      assertTrue(result.isLatestRelease(), "Should be marked as latest release");

      // Verify the self link was added
      assertTrue(result.hasLinks(), "Result should have links");
      assertTrue(result.getLink("self").isPresent(), "Self link should be present");

      // Verify method calls
      verify(mockGhRelease).getName();
      verify(mockGhRelease).getBody();
      verify(mockGhRelease).getPublished_at();
      verify(mockGhRelease).getHtmlUrl();
      verify(mockGhRelease).getId();

      // Verify static method calls
      productContentUtilsMock.verify(() ->
          com.axonivy.market.util.ProductContentUtils.transformGithubReleaseBody(releaseBody, productSourceUrl));
      gitHubUtilsMock.verify(() ->
          GitHubUtils.createSelfLinkForGithubReleaseModel(productId, releaseId));
    }
  }

  @Test
  void testGetGitHubReleaseModelsWithMEmptyReleases() throws IOException {
    Product mockProduct = mock(Product.class);
    Pageable mockPageable = mock(Pageable.class);


    Page<GitHubReleaseModel> result = gitHubService.getGitHubReleaseModels(Collections.emptyList(), mockPageable,
        mockProduct.getId(), mockProduct.getRepositoryName(), mockProduct.getSourceUrl());

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
  void testGetRepoOfficialReleasesWithEmptyReleases() throws IOException {
    // Arrange
    String repoName = "test-org/empty-repo";
    String productId = "test-product-id";

    GHRepository mockRepository = mock(GHRepository.class);
    PagedIterable<GHRelease> mockPagedIterable = mock(PagedIterable.class);

    when(gitHubService.getRepository(repoName)).thenReturn(mockRepository);
    when(mockRepository.listReleases()).thenReturn(mockPagedIterable);
    doAnswer(invocation -> null).when(mockPagedIterable).forEach(any());

    // Act
    List<GHRelease> result = gitHubService.getRepoOfficialReleases(repoName, productId);

    // Assert
    assertNotNull(result, "Result should not be null");
    assertTrue(result.isEmpty(), "Should return empty list when no releases exist");

    verify(gitHubService, atLeastOnce()).getRepository(repoName);
    verify(mockRepository).listReleases();
  }

  @Test
  void testGetRepoOfficialReleasesWithRepositoryNotFound() throws IOException {
    String repoName = "test-org/non-existent-repo";
    String productId = "test-product-id";

    assertThrows(NullPointerException.class, () -> gitHubService.getRepoOfficialReleases(repoName, productId),
        "Should throw NullPointerException when repository is not found");

    verify(gitHubService, atLeastOnce()).getRepository(repoName);
  }

  @Test
  void testGetRepoOfficialReleasesWithIOExceptionThrown() throws IOException {
    String repoName = "test-org/error-repo";
    String productId = "test-product-id";

    when(gitHubService.getRepository(repoName)).thenThrow(new IOException("Network error"));

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

    when(gitHubService.getRepository(repoName)).thenReturn(mockRepository);
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
  void testGetGitHubReleaseModels_WithValidReleases() throws IOException, URISyntaxException {
    // Arrange
    List<GHRelease> ghReleases = new ArrayList<>();
    GHRelease release1 = mock(GHRelease.class);
    GHRelease release2 = mock(GHRelease.class);
    GHRelease latestRelease = mock(GHRelease.class);

    ghReleases.add(release1);
    ghReleases.add(release2);

    Pageable pageable = Pageable.ofSize(10).withPage(0);
    String productId = "test-product-id";
    String productRepoName = "test-org/test-repo";
    String productSourceUrl = "https://github.com/test-org/test-repo";

    // Mock release data
    when(release1.getName()).thenReturn("v1.0.0");
    when(release1.getBody()).thenReturn("Release 1 body");
    when(release1.getPublished_at()).thenReturn(new Date());
    when(release1.getHtmlUrl()).thenReturn(new URI("https://github.com/test-org/test-repo/releases/tag/v1.0.0").toURL());
    lenient().when(release1.getId()).thenReturn(1L);

    when(release2.getName()).thenReturn("v2.0.0");
    when(release2.getBody()).thenReturn("Release 2 body");
    when(release2.getPublished_at()).thenReturn(new Date());
    when(release2.getHtmlUrl()).thenReturn(new URI("https://github.com/test-org/test-repo/releases/tag/v2.0.0").toURL());
    lenient().when(release2.getId()).thenReturn(2L);

    when(latestRelease.getName()).thenReturn("v2.0.0");

    // Mock the spy method calls
    doReturn(latestRelease).when(gitHubService).getGitHubLatestReleaseByProductId(productRepoName);

    // Mock static utilities
    try (MockedStatic<ProductContentUtils> productContentUtilsMock = mockStatic(ProductContentUtils.class);
         MockedStatic<GitHubUtils> gitHubUtilsMock = mockStatic(GitHubUtils.class)) {

      // Mock extractReleasesPage to return both releases
      productContentUtilsMock.when(() -> ProductContentUtils.extractReleasesPage(ghReleases, pageable))
          .thenReturn(ghReleases);

      // Mock transformGithubReleaseBody
      productContentUtilsMock.when(() ->
          ProductContentUtils.transformGithubReleaseBody("Release 1 body", productSourceUrl))
          .thenReturn("Transformed Release 1 body");
      productContentUtilsMock.when(() ->
          ProductContentUtils.transformGithubReleaseBody("Release 2 body", productSourceUrl))
          .thenReturn("Transformed Release 2 body");

      // Mock createSelfLinkForGithubReleaseModel
      Link selfLink1 = Link.of("http://localhost/api/products/test-product-id/releases/1").withSelfRel();
      Link selfLink2 = Link.of("http://localhost/api/products/test-product-id/releases/2").withSelfRel();
      gitHubUtilsMock.when(() -> GitHubUtils.createSelfLinkForGithubReleaseModel(productId, 1L))
          .thenReturn(selfLink1);
      gitHubUtilsMock.when(() -> GitHubUtils.createSelfLinkForGithubReleaseModel(productId, 2L))
          .thenReturn(selfLink2);

      // Act
      Page<GitHubReleaseModel> result = gitHubService.getGitHubReleaseModels(
          ghReleases, pageable, productId, productRepoName, productSourceUrl);

      // Assert
      assertNotNull(result, "Result should not be null");
      assertEquals(2, result.getContent().size(), "Should return 2 release models");
      assertEquals(2, result.getTotalElements(), "Total elements should be 2");
      assertEquals(pageable, result.getPageable(), "Pageable should match");

      // Verify first release
      GitHubReleaseModel model1 = result.getContent().get(0);
      assertEquals("v1.0.0", model1.getName(), "First release name should match");
      assertEquals("Transformed Release 1 body", model1.getBody(), "First release body should be transformed");
      assertFalse(model1.isLatestRelease(), "First release should not be latest");

      // Verify second release (latest)
      GitHubReleaseModel model2 = result.getContent().get(1);
      assertEquals("v2.0.0", model2.getName(), "Second release name should match");
      assertEquals("Transformed Release 2 body", model2.getBody(), "Second release body should be transformed");
      assertTrue(model2.isLatestRelease(), "Second release should be latest");

      // Verify method calls
      verify(gitHubService).getGitHubLatestReleaseByProductId(productRepoName);
      productContentUtilsMock.verify(() -> ProductContentUtils.extractReleasesPage(ghReleases, pageable));
    }
  }

  @Test
  void testGetGitHubReleaseModelsWithEmptyReleasesList() throws IOException {
    // Arrange
    List<GHRelease> emptyReleases = new ArrayList<>();
    Pageable pageable = Pageable.ofSize(10).withPage(0);
    String productId = "test-product-id";
    String productRepoName = "test-org/test-repo";
    String productSourceUrl = "https://github.com/test-org/test-repo";

    // Act
    Page<GitHubReleaseModel> result = gitHubService.getGitHubReleaseModels(
        emptyReleases, pageable, productId, productRepoName, productSourceUrl);

    // Assert
    assertNotNull(result, "Result should not be null");
    assertTrue(result.getContent().isEmpty(), "Content should be empty");
    assertEquals(0, result.getTotalElements(), "Total elements should be 0");
    assertEquals(pageable, result.getPageable(), "Pageable should match");

    // Verify no method calls were made for processing releases
    verify(gitHubService, never()).getGitHubLatestReleaseByProductId(anyString());
  }

  @Test
  void testGetGitHubReleaseModelsWithNullReleasesList() throws IOException {
    // Arrange
    List<GHRelease> nullReleases = null;
    Pageable pageable = Pageable.ofSize(10).withPage(0);
    String productId = "test-product-id";
    String productRepoName = "test-org/test-repo";
    String productSourceUrl = "https://github.com/test-org/test-repo";

    // Act & Assert - Should throw NullPointerException due to ghReleases.size() call
    assertThrows(NullPointerException.class, () -> gitHubService.getGitHubReleaseModels(
        nullReleases, pageable, productId, productRepoName, productSourceUrl),
        "Should throw NullPointerException when ghReleases is null due to .size() call");

    // Verify no method calls were made for processing releases
    verify(gitHubService, never()).getGitHubLatestReleaseByProductId(anyString());
  }

  @Test
  void testGetGitHubReleaseModels_WithPagination() throws IOException, URISyntaxException {
    // Arrange
    List<GHRelease> allReleases = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
      GHRelease release = mock(GHRelease.class);
      when(release.getName()).thenReturn("v" + i + ".0.0");
      when(release.getBody()).thenReturn("Release " + i + " body");
      when(release.getPublished_at()).thenReturn(new Date());
      when(release.getHtmlUrl()).thenReturn(new URI("https://github.com/test-org/test-repo/releases/tag/v" + i + ".0.0").toURL());
      lenient().when(release.getId()).thenReturn((long) i);
      allReleases.add(release);
    }

    // Page with size 2, page 1 (second page)
    Pageable pageable = Pageable.ofSize(2).withPage(1);
    String productId = "test-product-id";
    String productRepoName = "test-org/test-repo";
    String productSourceUrl = "https://github.com/test-org/test-repo";

    GHRelease latestRelease = mock(GHRelease.class);
    when(latestRelease.getName()).thenReturn("v5.0.0");

    // Mock the spy method calls
    doReturn(latestRelease).when(gitHubService).getGitHubLatestReleaseByProductId(productRepoName);

}
