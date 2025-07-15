package com.axonivy.market.controller;

import com.axonivy.market.model.GithubReposModel;
import com.axonivy.market.service.GithubReposService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

import static com.axonivy.market.constants.RequestMappingConstants.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(GITHUB_REPOS)
@Tag(name = "GitHub Repos API", description = "API to fetch GitHub repositories and workflows")
public class GithubReposController {

    private final GithubReposService githubReposService;

      @GetMapping(REPOS)
      public ResponseEntity<List<GithubReposModel>> getGitHubRepos() {
        List<GithubReposModel> response = githubReposService.fetchAllRepositories();
        return new ResponseEntity<>(response, HttpStatus.OK);
      }

/*      @GetMapping(REPOS_REPORT)
      public ResponseEntity<TestReport> getTestReport(@PathVariable String repo, @PathVariable String workflow) {
        List<TestStep> steps = List.of(
            new TestStep("testLogin", "passed"),
            new TestStep("testLogout", "failed"),
            new TestStep("testFetchData", "skipped")
        );

        TestReport report = TestReport.fromSteps(steps);
        return ResponseEntity.ok(report);
      }*/

    @GetMapping(SYNC_GITHUB_MONITOR)
    public ResponseEntity<String> syncGithubMonitor() throws IOException {
        githubReposService.loadAndStoreTestReports();
        return ResponseEntity.ok("Repositories loaded successfully.");
    }
}