package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.SyncTaskExecutionModel;
import com.axonivy.market.service.SyncTaskExecutionService;
import com.axonivy.market.util.validator.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.SYNC_TASK_EXECUTION;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
@RequestMapping(SYNC_TASK_EXECUTION)
@Tag(name = "Sync Task Execution Controller", description = "API to inspect sync task executions")
public class SyncTaskExecutionController {

  private final SyncTaskExecutionService syncTaskExecutionService;
  private final GitHubService gitHubService;

  @GetMapping
  @Operation(hidden = true)
  public ResponseEntity<List<SyncTaskExecutionModel>> getAllSyncTaskExecutions(
      @RequestHeader(value = AUTHORIZATION) String authorizationHeader) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserInOrganizationAndTeam(token, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME);
    List<SyncTaskExecutionModel> models = syncTaskExecutionService.getAllSyncTaskExecutions();
    return new ResponseEntity<>(models, HttpStatus.OK);
  }

  @GetMapping("/{jobKey}")
  @Operation(hidden = true)
  public ResponseEntity<SyncTaskExecutionModel> getSyncTaskExecutionByKey(
      @RequestHeader(value = AUTHORIZATION) String authorizationHeader, @PathVariable String jobKey) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserInOrganizationAndTeam(token, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME);
    SyncTaskExecutionModel model = syncTaskExecutionService.getSyncTaskExecutionByKey(jobKey);
    if (ObjectUtils.isEmpty(model)) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(model, HttpStatus.OK);
  }
}
