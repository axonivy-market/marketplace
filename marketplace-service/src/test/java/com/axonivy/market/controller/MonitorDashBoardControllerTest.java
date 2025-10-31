package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.GithubReposModel;
import com.axonivy.market.model.TestStepsModel;
import com.axonivy.market.service.GithubReposService;
import com.axonivy.market.service.TestStepsService;
import com.axonivy.market.util.validator.AuthorizationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

class MonitorDashBoardControllerTest {

  private static final String TOKEN = "dummy-token";

  @Mock
  private GithubReposService githubReposService;
  @Mock
  private GitHubService githubService;
  @Mock
  private TestStepsService testStepsService;
  @InjectMocks
  private MonitorDashBoardController controller;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }



  @Test
  void testGetTestReportReturnsList() {
    TestStepsModel model = new TestStepsModel();
    when(testStepsService.fetchTestReport("repo", WorkFlowType.CI)).thenReturn(List.of(model));
    ResponseEntity<List<TestStepsModel>> response = controller.getTestReport("repo", WorkFlowType.CI);
    assertEquals(200, response.getStatusCode().value(), "Status code should be 200 OK");
    assertEquals(1, response.getBody().size(), "Response body should contain one element");
    assertSame(model, response.getBody().get(0), "Returned model should match the mocked model");
  }

  @Test
  void testSyncGithubMonitorReturnsOk() throws IOException {
    // Mock static method
    try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
      utils.when(() -> AuthorizationUtils.getBearerToken(AUTHORIZATION)).thenReturn(TOKEN);
      doNothing().when(githubService).validateUserInOrganizationAndTeam(TOKEN,
          GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
          GitHubConstants.AXONIVY_MARKET_TEAM_NAME);
      doNothing().when(githubReposService).loadAndStoreTestReports();

      ResponseEntity<String> response = controller.syncGithubMonitor(AUTHORIZATION);
      assertEquals(200, response.getStatusCode().value(), "Status code should be 200 OK");
      assertEquals("Repositories loaded successfully.", response.getBody(),
          "Response body should match expected message");
    }
  }

  @Test
  void testSyncGithubMonitorHandlesException() throws IOException {
    try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
      utils.when(() -> AuthorizationUtils.getBearerToken(AUTHORIZATION)).thenReturn(TOKEN);
      doNothing().when(githubService).validateUserInOrganizationAndTeam(TOKEN,
          GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME, GitHubConstants.AXONIVY_MARKET_TEAM_NAME);
      doThrow(new IOException("fail")).when(githubReposService).loadAndStoreTestReports();

      assertThrows(IOException.class, () -> controller.syncGithubMonitor(AUTHORIZATION),
          "IOException should be thrown when service fails to load and store test reports");
    }
  }

  @Test
  void testUpdateRepoPriorities() {
    List<String> updates = List.of("repo1", "repo2");

    doNothing().when(githubReposService).updateFocusedRepo(updates);
    doNothing().when(githubService).validateUserInOrganizationAndTeam("token", GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
            GitHubConstants.AXONIVY_MARKET_TEAM_NAME);

    ResponseEntity<String> response = controller.updateFocusedRepo("token", updates);

    assertEquals(200, response.getStatusCode().value(), "Status code should be 200 OK");
    assertEquals("Focused repository updated successfully.", response.getBody(),
         "Response body should match expected message");
  }

  @Test
  void testFindAllFeedbacksReturnPagedModel() {
    // Arrange
    GithubReposModel model = new GithubReposModel();
    List<GithubReposModel> models = List.of(model);
    Page<GithubReposModel> page = new PageImpl<>(models, PageRequest.of(0, 10), 1);

    when(githubReposService.fetchAllRepositories(
        eq(true), eq("feedback"), eq("name"), eq("ASC"), any(PageRequest.class)))
        .thenReturn(page);

    // Act
    ResponseEntity<PagedModel<GithubReposModel>> response = controller.findAllFeedbacks(
        true, PageRequest.of(0, 10), "feedback", "name", "ASC");

    // Assert
    PagedModel<GithubReposModel> pagedModel = response.getBody();
    assertNotNull(pagedModel, "PagedModel should not be null");
    assertEquals(1, pagedModel.getContent().size(), "Content size should be 1");
    assertTrue(pagedModel.getContent().contains(model), "Content should contain the mocked model");

    PagedModel.PageMetadata metadata = pagedModel.getMetadata();
    assertNotNull(metadata, "Page metadata should not be null");
    assertEquals(10, metadata.getSize(), "Page size should be 10");
    assertEquals(0, metadata.getNumber(), "Page number should be 0");
    assertEquals(1, metadata.getTotalElements(), "Total elements should be 1");
    assertEquals(1, metadata.getTotalPages(), "Total pages should be 1");
  }
}