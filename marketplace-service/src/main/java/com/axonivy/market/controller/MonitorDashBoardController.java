package com.axonivy.market.controller;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.PostgresDBConstants;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

import java.io.IOException;
import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.constants.RequestParamConstants.ID;
import static com.axonivy.market.constants.RequestParamConstants.IS_FOCUSED;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequiredArgsConstructor
@RequestMapping(MONITOR_DASHBOARD)
@Tag(name = "GitHub Repos API", description = "API to fetch GitHub repositories and workflows")
public class MonitorDashBoardController {

  private final GithubReposService githubReposService;
  private final TestStepsService testStepsService;
  private final GitHubService gitHubService;
  private final PagedResourcesAssembler<GithubReposModel> pagedResourcesAssembler;

  @GetMapping(REPOS_REPORT)
  public ResponseEntity<List<TestStepsModel>> getTestReport(
      @PathVariable(PostgresDBConstants.PRODUCT_ID)
      @Parameter(description = "productId", example = "portal", in = ParameterIn.PATH) String productId,
      @PathVariable(WORKFLOW) @Parameter(description = "Workflow name", example = "CI",
          in = ParameterIn.PATH) WorkFlowType workflow) {
    List<TestStepsModel> response = testStepsService.fetchTestReport(productId, workflow);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PutMapping(SYNC)
  @Operation(summary = "Sync GitHub monitor",
      description = "Load and store test reports from GitHub repositories")
  public ResponseEntity<String> syncGithubMonitor() throws IOException {
    githubReposService.loadAndStoreTestReports();
    return ResponseEntity.ok("Repositories loaded successfully.");
  }

  @PutMapping(SYNC_ONE_PRODUCT_BY_ID)
  @Operation(hidden = true)
  public ResponseEntity<String> syncOneGithubMonitor(
      @PathVariable(ID) @Parameter(description = "Product id (from meta.json)", example = "portal",
          in = ParameterIn.PATH) String id) throws IOException {
    githubReposService.loadAndStoreTestRepostsForOneProduct(id);
    return ResponseEntity.ok("Repository loaded successfully.");
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

  @GetMapping(REPOS)
  public ResponseEntity<PagedModel<GithubReposModel>> findAllFeedbacks(@RequestParam(value = IS_FOCUSED,
          required = false) Boolean isFocused, @ParameterObject Pageable pageable,
      @RequestParam(value = "search", required = false) String searchText,
      @RequestParam(value = "workflowType", required = false, defaultValue = "name") String type,
      @RequestParam(value = "sortDirection", required = false, defaultValue = "ASC") String sortDirection
  ) {
    Page<GithubReposModel> results = githubReposService.fetchAllRepositories(isFocused, searchText, type, sortDirection,
        pageable);
    PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(results.getSize(), results.getNumber(),
        results.getTotalElements(), results.getTotalPages());
    PagedModel<GithubReposModel> pagedModel = PagedModel.of(results.getContent(), pageMetadata);
    return ResponseEntity.ok(pagedModel);
  }
}
