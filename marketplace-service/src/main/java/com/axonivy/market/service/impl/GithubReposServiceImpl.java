package com.axonivy.market.service.impl;

import com.axonivy.market.assembler.GithubReposModelAssembler;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.TestSteps;
import com.axonivy.market.entity.WorkflowRepo;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.GithubReposModel;
import com.axonivy.market.repository.GithubRepoRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.repository.TestStepsRepository;
import com.axonivy.market.repository.WorkflowRepoRepository;
import com.axonivy.market.service.GithubReposService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.kohsuke.github.GHArtifact;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowRun;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class GithubReposServiceImpl implements GithubReposService {

  @Value("${ignored.repos}")
  List<String> ignoredRepos;

  @Value("${MARKET_GITHUB_TOKEN}")
  private String githubToken;

  private final GithubArtifactExtractImpl artifactDownloader;
  private final WorkflowRepoProcessorImpl workflowProcessor;
  private final TestStepProcessorImpl stepProcessor;
  private final GithubRepoRepository githubRepoRepository;
  private final WorkflowRepoRepository workflowRepoRepository;
  private final TestStepsRepository testStepsRepository;
  private final GitHubService gitHubService;
  private final ProductRepository productRepository;
  private final GithubReposModelAssembler githubReposModelAssembler;

  private static final String REPORT_FILE_NAME = "test_report.json";
  private static final String BADGE_URL = "https://github.com/%s/actions/workflows/%s/badge.svg";

  @Override
  @Transactional
  public void loadAndStoreTestReports() {
    try {
      GitHub gitHub = gitHubService.getGitHub();
      List<Product> products = productRepository.findAll();

      for (Product product : products) {
        processProduct(product, gitHub);
      }

    } catch (Exception e) {
      log.error("Error loading and storing test reports", e);
    }
  }

  @Override
  public List<GithubReposModel> fetchAllRepositories() {
    List<GithubRepo> entities = githubRepoRepository.findAll();

    return entities.stream()
        .map(githubReposModelAssembler::toModel)
        .collect(Collectors.toList());
  }

  private void processProduct(Product product, GitHub gitHub) {
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

      processWorkflowWithFallback(ghRepo, savedRepo, "ci.yml", "CI");
      processWorkflowWithFallback(ghRepo, savedRepo, "dev.yml", "DEV");

    } catch (Exception e) {
      log.error("Error processing product repo: {}", product.getRepositoryName(), e);
    }
  }

  @Transactional
  private void deleteExistingGithubRepoIfExists(String repoName) {
    Optional<GithubRepo> existingRepoOpt = githubRepoRepository.findByName(repoName);
    if (existingRepoOpt.isPresent()) {
      GithubRepo existingRepo = existingRepoOpt.get();
      githubRepoRepository.deleteById(existingRepo.getId());
      log.info("Deleted existing GitHub repo and all related data: {}", repoName);
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
          File unzipDir = artifactDownloader.extractZipToTempDir(zipStream, ghRepo.getName());
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

    createNewTestSteps(savedWorkflow, testData);

    log.info("Created new workflow {} for repo {}", workflowType, repo.getName());
  }

  private WorkflowRepo createNewWorkflow(GithubRepo repo, JsonNode testData, String workflowType) {
    if (testData != null) {
      return workflowProcessor.createWorkflowRepo(testData, repo, workflowType);
    } else {
      return WorkflowRepo.builder()
          .id(UUID.randomUUID().toString())
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

  private void createNewTestSteps(WorkflowRepo workflow, JsonNode testData) {
    if (testData != null) {
      List<TestSteps> newSteps = stepProcessor.parseTestSteps(testData, workflow);
      if (newSteps != null && !newSteps.isEmpty()) {
        workflow.setTestSteps(newSteps);
        workflowRepoRepository.save(workflow);
        log.info("Created {} test steps for workflow {}", newSteps.size(), workflow.getId());
      }
    }
  }

  private JsonNode findTestReportJson(File unzipDir) throws IOException {
    File file = new File(unzipDir, REPORT_FILE_NAME);
    if (file.exists()) {
      log.info("Found test report file: {}", file.getAbsolutePath());
      return new ObjectMapper().readTree(file);
    }
    log.warn("No '{}' found in directory: {}", REPORT_FILE_NAME, unzipDir);
    return null;
  }

  private GithubRepo createNewGithubRepo(GHRepository repo, String ciBadgeUrl, String devBadgeUrl) throws IOException {
    return GithubRepo.builder()
        .id(UUID.randomUUID().toString())
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