package com.axonivy.market.controller;

import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.SyncTaskExecutionModel;
import com.axonivy.market.service.SyncTaskExecutionService;
import com.axonivy.market.util.validator.AuthorizationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static com.axonivy.market.constants.GitHubConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SyncTaskExecutionControllerTest {

  private static final String BEARER_TOKEN = "Bearer token";
  private static final String TOKEN = "token";
  private static final String JOB_KEY = "jobKey";

  private SyncTaskExecutionService syncTaskExecutionService;
  private GitHubService gitHubService;
  private SyncTaskExecutionController controller;

  @BeforeEach
  void setUp() {
    syncTaskExecutionService = mock(SyncTaskExecutionService.class);
    gitHubService = mock(GitHubService.class);
    controller = new SyncTaskExecutionController(syncTaskExecutionService, gitHubService);
  }

  @Test
  void testGetAllSyncTaskExecutions() {
    List<SyncTaskExecutionModel> models = Collections.singletonList(new SyncTaskExecutionModel());
    when(syncTaskExecutionService.getAllSyncTaskExecutions()).thenReturn(models);
    try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
      utils.when(() -> AuthorizationUtils.getBearerToken(BEARER_TOKEN)).thenReturn(TOKEN);
      doNothing().when(gitHubService).validateUserInOrganizationAndTeam(
          TOKEN, AXONIVY_MARKET_ORGANIZATION_NAME, AXONIVY_MARKET_TEAM_NAME);
      ResponseEntity<List<SyncTaskExecutionModel>> response = controller.getAllSyncTaskExecutions(BEARER_TOKEN);
      assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK for getAllSyncTaskExecutions");
      assertEquals(models, response.getBody(), "Response body should match the expected models");
    }
  }

  @Test
  void testGetSyncTaskExecutionByKeyFound() {
    SyncTaskExecutionModel model = new SyncTaskExecutionModel();
    when(syncTaskExecutionService.getSyncTaskExecutionByKey(JOB_KEY)).thenReturn(model);
    try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
      utils.when(() -> AuthorizationUtils.getBearerToken(BEARER_TOKEN)).thenReturn(TOKEN);
      doNothing().when(gitHubService).validateUserInOrganizationAndTeam(
          TOKEN, AXONIVY_MARKET_ORGANIZATION_NAME, AXONIVY_MARKET_TEAM_NAME);
      ResponseEntity<SyncTaskExecutionModel> response = controller.getSyncTaskExecutionByKey(BEARER_TOKEN, JOB_KEY);
      assertEquals(HttpStatus.OK, response.getStatusCode(), "Status should be OK for found SyncTaskExecution");
      assertEquals(model, response.getBody(), "Response body should match the expected model");
    }
  }

  @Test
  void testGetSyncTaskExecutionByKeyNotFound() {
    when(syncTaskExecutionService.getSyncTaskExecutionByKey(JOB_KEY)).thenReturn(null);
    try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
      utils.when(() -> AuthorizationUtils.getBearerToken(BEARER_TOKEN)).thenReturn(TOKEN);
      doNothing().when(gitHubService).validateUserInOrganizationAndTeam(
          TOKEN, AXONIVY_MARKET_ORGANIZATION_NAME, AXONIVY_MARKET_TEAM_NAME);
      ResponseEntity<SyncTaskExecutionModel> response = controller.getSyncTaskExecutionByKey(BEARER_TOKEN, JOB_KEY);
      assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Status should be NOT_FOUND for missing SyncTaskExecution");
      assertNull(response.getBody(), "Response body should be null when not found");
    }
  }
}