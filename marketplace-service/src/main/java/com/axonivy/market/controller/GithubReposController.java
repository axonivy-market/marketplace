package com.axonivy.market.controller;

import com.axonivy.market.service.GithubReposService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.axonivy.market.constants.RequestMappingConstants.*;
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(GITHUB_REPOS)
@Tag(name = "GitHub Repos API", description = "API to fetch GitHub repositories and workflows")
public class GithubReposController {

  private final GithubReposService githubReposService;

  @GetMapping(REPOS)
  public ResponseEntity<String> getGitHubRepos() {
    String response = githubReposService.fetchAllRepositories();
    return ResponseEntity.ok(response);
  }

  @GetMapping(REPOS_REPORT)
  public ResponseEntity<String> getGitHubWorkflow(
      @PathVariable(REPO) @Parameter(description = "Repo name", example = "Coffee machine connector",
          in = ParameterIn.PATH) String repo,
      @PathVariable(WORKFLOW) @Parameter(description = "Type of workflow", example = "ci",
          in = ParameterIn.PATH) String workflow) {
    String response = githubReposService.fetchWorkflowRuns(repo, workflow);
    return ResponseEntity.ok(response);
  }
}