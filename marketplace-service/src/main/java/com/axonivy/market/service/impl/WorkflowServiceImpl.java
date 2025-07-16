package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.TestSteps;
import com.axonivy.market.entity.WorkflowRepo;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.repository.GithubRepoRepository;
import com.axonivy.market.repository.TestStepsRepository;
import com.axonivy.market.repository.WorkflowRepoRepository;
import com.axonivy.market.service.GithubArtifactExtract;
import com.axonivy.market.service.TestStepsService;
import com.axonivy.market.service.WorkflowRepoProcessor;
import com.axonivy.market.service.WorkflowService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.GHArtifact;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowRun;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
@Service
@Log4j2
@RequiredArgsConstructor
public class WorkflowServiceImpl implements WorkflowService {

  @Value("${ignored.repos}")
  List<String> ignoredRepos;
  private final WorkflowRepoRepository workflowRepoRepository;
  private final WorkflowRepoProcessor workflowProcessor;
  private final GithubRepoRepository githubRepoRepository;
  private final TestStepsRepository testStepsRepository;
  private final TestStepsService testStepsService;
  private final GitHubService gitHubService;
  private final GithubArtifactExtract githubArtifactExtract;
  private static final String BADGE_URL = "https://github.com/%s/actions/workflows/%s/badge.svg";

  private static final String REPORT_FILE_NAME = "test_report.json";

  @Transactional
  public void processProduct(Product product) {
    try {
      String repoPath = product.getRepositoryName();
      if (repoPath == null || !repoPath.contains("/")) {
        log.warn("Invalid repository path for product: {}", repoPath);
        return;
      }

      GHRepository ghRepo = gitHubService.getRepository(repoPath);
      if (!shouldInclude(ghRepo)) {
        return;
      }

      String ciBadge = buildBadgeUrl(ghRepo, "ci.yml");
      String devBadge = buildBadgeUrl(ghRepo, "dev.yml");

      deleteExistingGithubRepoIfExists(ghRepo.getName());

      GithubRepo githubRepo = createNewGithubRepo(ghRepo, ciBadge, devBadge);
      GithubRepo savedRepo = githubRepoRepository.save(githubRepo);
      githubRepoRepository.flush();

      savedRepo = githubRepoRepository.findById(savedRepo.getId())
          .orElseThrow(() -> new RuntimeException("Failed to save GitHub repo: " + ghRepo.getName()));

      processWorkflowWithFallback(ghRepo, savedRepo, "ci.yml", "CI");
      processWorkflowWithFallback(ghRepo, savedRepo, "dev.yml", "DEV");

    } catch (Exception e) {
      log.error("Error processing product repo: {}", product.getRepositoryName(), e);
    }
  }

  @Transactional
  public void deleteExistingGithubRepoIfExists(String repoName) {
    Optional<GithubRepo> existingRepoOptional = githubRepoRepository.findByName(repoName);
    if (existingRepoOptional.isPresent()) {
      GithubRepo existingRepo = existingRepoOptional.get();
      try {
        List<WorkflowRepo> workflows = workflowRepoRepository.findByRepository(existingRepo);

        for (WorkflowRepo workflow : workflows) {
          List<TestSteps> testSteps = testStepsRepository.findByWorkflowId(workflow.getId());
          testStepsRepository.deleteAll(testSteps);

          workflowRepoRepository.delete(workflow);
        }
        githubRepoRepository.delete(existingRepo);

      } catch (Exception e) {
        log.error("Error deleting GitHub repo {}: {}", repoName, e.getMessage());
        throw e;
      }
    }
  }

  private void processWorkflowWithFallback(GHRepository ghRepo, GithubRepo dbRepo,
      String workflowFileName, String workflowType) {
    try {
      GHWorkflowRun run = gitHubService.getLatestWorkflowRun(ghRepo, workflowFileName);
      if (run == null) {
        log.warn("No workflow run found for {} -> {}", ghRepo.getFullName(), workflowFileName);
        createWorkflow(dbRepo, null, workflowType);
        return;
      }

      GHArtifact artifact = gitHubService.getExportTestArtifact(run);
      if (artifact != null) {
        try (InputStream zipStream = gitHubService.downloadArtifactZip(artifact)) {
          File unzipDir = githubArtifactExtract.extractZipToTempDir(zipStream, ghRepo.getName());
          JsonNode testData = findTestReportJson(unzipDir);
          createWorkflow(dbRepo, testData, workflowType);
          return;
        }
      }

      createWorkflow(dbRepo, null, workflowType);

    } catch (Exception e) {
      log.error("Error processing workflow {} for repo {}", workflowType, dbRepo.getName(), e);
      createWorkflow(dbRepo, null, workflowType);
    }
  }

  @Transactional
  private void createWorkflow(GithubRepo repo, JsonNode testData, String workflowType) {
    WorkflowRepo newWorkflow = createNewWorkflow(repo, testData, workflowType);
    WorkflowRepo savedWorkflow = workflowRepoRepository.save(newWorkflow);

    testStepsService.createNewTestSteps(savedWorkflow, testData);
  }

  private WorkflowRepo createNewWorkflow(GithubRepo repo, JsonNode testData, String workflowType) {
    if (testData != null) {
      return workflowProcessor.createWorkflowRepo(testData, repo, workflowType);
    } else {
      return WorkflowRepo.builder()
          .repository(repo)
          .type(workflowType)
          .passed(0)
          .failed(0)
          .mockPassed(0)
          .mockFailed(0)
          .realPassed(0)
          .realFailed(0)
          .build();
    }
  }

  public JsonNode findTestReportJson(File unzipDir) throws IOException {
    File file = new File(unzipDir, REPORT_FILE_NAME);
    if (file.exists()) {
      return new ObjectMapper().readTree(file);
    }
    log.warn("No '{}' found in directory: {}", REPORT_FILE_NAME, unzipDir);
    return null;
  }

  private GithubRepo createNewGithubRepo(GHRepository repo, String ciBadgeUrl, String devBadgeUrl) throws IOException {
    return GithubRepo.builder()
        .name(repo.getName())
        .htmlUrl(repo.getHtmlUrl().toString())
        .language(repo.getLanguage())
        .lastUpdated(repo.getUpdatedAt())
        .ciBadgeUrl(ciBadgeUrl)
        .devBadgeUrl(devBadgeUrl)
        .build();
  }

  private String buildBadgeUrl(GHRepository repo, String workflowFileName) {
    return String.format(BADGE_URL, repo.getFullName(), workflowFileName);
  }

  private boolean shouldInclude(GHRepository repo) {
    return !repo.isArchived() &&
        !repo.isTemplate() &&
        "master".equals(repo.getDefaultBranch()) &&
        repo.getLanguage() != null &&
        !ignoredRepos.contains(repo.getName());
  }
}