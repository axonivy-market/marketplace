package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.GithubReposModel;
import com.axonivy.market.model.TestStepsModel;
import com.axonivy.market.service.GithubReposService;
import com.axonivy.market.service.TestStepsService;
import com.axonivy.market.util.validator.AuthorizationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.*;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
@RequestMapping(MONITOR_DASHBOARD)
@Tag(name = "GitHub Repos API", description = "API to fetch GitHub repositories and workflows")
public class MonitorDashBoardController {

  private final GithubReposService githubReposService;
  private final TestStepsService testStepsService;
  private final GitHubService gitHubService;

  @Operation(summary = "Get all GitHub repositories",
      description = "Fetch all GitHub repositories with their details and test results")
  @ApiResponse(
      responseCode = "200",
      description = "Successfully fetched GitHub repositories"
  )
  @GetMapping(REPOS)
  public ResponseEntity<List<GithubReposModel>> getGitHubRepos() {
    List<GithubReposModel> response = githubReposService.fetchAllRepositories();
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping(REPOS_REPORT)
  @Operation(summary = "Get test report by repository and workflow",
      description = "Fetch test report details for a specific repository and workflow")
  public ResponseEntity<List<TestStepsModel>> getTestReport(
      @PathVariable(REPO) @Parameter(description = "Repository name", example = "my-repo",
          in = ParameterIn.PATH) String repo,
      @PathVariable(WORKFLOW) @Parameter(description = "Workflow name", example = "build-workflow",
          in = ParameterIn.PATH) WorkFlowType workflow) {
    List<TestStepsModel> response = testStepsService.fetchTestReport(repo, workflow);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PutMapping(SYNC)
  @Operation(summary = "Sync GitHub monitor",
      description = "Load and store test reports from GitHub repositories")
  public ResponseEntity<String> syncGithubMonitor() throws IOException {
    githubReposService.loadAndStoreTestReports();
    return ResponseEntity.ok("Repositories loaded successfully.");
  }

  @PutMapping(FOCUSED)
  @Operation(hidden = true)
  public ResponseEntity<String> updateFocusedRepo(@RequestHeader(value = AUTHORIZATION) String authorizationHeader,
                                                  @RequestParam(REPOS) List<String> repos) {
    String token = AuthorizationUtils.getBearerToken(authorizationHeader);
    gitHubService.validateUserInOrganizationAndTeam(token, GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME,
            GitHubConstants.AXONIVY_MARKET_TEAM_NAME);
    githubReposService.updateFocusedRepo(repos);
    return ResponseEntity.ok("Focused repository updated successfully.");
  }
}