package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.enums.SyncJobType;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.SyncJobExecutionModel;
import com.axonivy.market.service.SyncJobExecutionService;
import com.axonivy.market.util.validator.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.SCHEDULED_TASK;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
@RequestMapping(SCHEDULED_TASK)
@Tag(name = "Scheduled Task Controller", description = "API to inspect sync job executions")
public class ScheduledTaskController {

  private final SyncJobExecutionService syncJobExecutionService;
  private final GitHubService gitHubService;

  @GetMapping
  @Operation(hidden = true)
  public ResponseEntity<List<SyncJobExecutionModel>> findLatestExecutions(
      @RequestHeader(value = AUTHORIZATION) String authorizationHeader) {
    validateToken(authorizationHeader);
    List<SyncJobExecutionModel> models = syncJobExecutionService.findLatestExecutions().stream()
        .map(execution -> SyncJobExecutionModel.builder()
            .jobKey(execution.getJobType().getJobKey())
            .status(execution.getStatus())
            .triggeredAt(execution.getTriggeredAt())
            .completedAt(execution.getCompletedAt())
            .message(execution.getMessage())
            .reference(execution.getReference())
            .build())
        .toList();
    return ResponseEntity.ok(models);
  }

  @GetMapping("/{jobKey}")
  @Operation(hidden = true)
  public ResponseEntity<SyncJobExecutionModel> findLatestExecutionByJobKey(
      @RequestHeader(value = AUTHORIZATION) String authorizationHeader,
      @PathVariable String jobKey) {
    validateToken(authorizationHeader);
    return SyncJobType.fromJobKey(jobKey)
        .flatMap(syncJobExecutionService::findLatestExecution)
        .map(execution -> SyncJobExecutionModel.builder()
            .jobKey(execution.getJobType().getJobKey())
            .status(execution.getStatus())
            .triggeredAt(execution.getTriggeredAt())
            .completedAt(execution.getCompletedAt())
            .message(execution.getMessage())
            .reference(execution.getReference())
            .build())
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  private void validateToken(String authorizationHeader) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserInOrganizationAndTeam(token, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
        GitHubConstants.AXONIVY_MARKET_TEAM_NAME);
  }
}
