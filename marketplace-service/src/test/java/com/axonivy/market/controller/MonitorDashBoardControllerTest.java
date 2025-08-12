package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.GithubReposModel;
import com.axonivy.market.model.TestStepsModel;
import com.axonivy.market.service.GithubReposService;
import com.axonivy.market.service.TestStepsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MonitorDashBoardControllerTest {
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
  void testGetGitHubReposReturnsList() {
    GithubReposModel model = new GithubReposModel();
    when(githubReposService.fetchAllRepositories()).thenReturn(List.of(model));
    ResponseEntity<List<GithubReposModel>> response = controller.getGitHubRepos();
    assertEquals(200, response.getStatusCode().value(), "Status code should be 200 OK");
    assertEquals(1, response.getBody().size(), "Response body should contain one element");
    assertSame(model, response.getBody().get(0), "Returned model should match the mocked model");
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
    doNothing().when(githubReposService).loadAndStoreTestReports();
    ResponseEntity<String> response = controller.syncGithubMonitor();
    assertEquals(200, response.getStatusCode().value(), "Status code should be 200 OK");
    assertEquals("Repositories loaded successfully.", response.getBody(),
        "Response body should match expected message");
  }

  @Test
  void testSyncGithubMonitorHandlesException() throws IOException {
    doThrow(new IOException("fail")).when(githubReposService).loadAndStoreTestReports();
    assertThrows(IOException.class, () -> controller.syncGithubMonitor(),
        "IOException should be thrown when service fails to load and store test reports");
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
}