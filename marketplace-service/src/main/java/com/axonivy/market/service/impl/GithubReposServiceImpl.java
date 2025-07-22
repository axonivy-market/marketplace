package com.axonivy.market.service.impl;

import com.axonivy.market.assembler.GithubReposModelAssembler;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.GithubReposModel;
import com.axonivy.market.repository.GithubRepoRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.GithubReposService;
import com.axonivy.market.service.TestStepsService;
import com.axonivy.market.util.FileUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHArtifact;
import org.kohsuke.github.GHException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowRun;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import static com.axonivy.market.constants.DirectoryConstants.GITHUB_REPO_DIR;
import static com.axonivy.market.entity.GithubRepo.createNewGithubRepo;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubReposServiceImpl implements GithubReposService {

  private static final String BADGE_URL = "https://github.com/%s/actions/workflows/%s/badge.svg";
  private static final String REPORT_FILE_NAME = "test_report.json";

  private final GithubRepoRepository githubRepoRepository;
  private final GithubReposModelAssembler githubReposModelAssembler;
  private final TestStepsService testStepsService;
  private final GitHubService gitHubService;
  private final ProductRepository productRepository;

  @Override
  public void loadAndStoreTestReports() {
    try {
      List<Product> products = productRepository.findAll().stream()
          .filter(product -> Boolean.FALSE != product.getListed()
              && product.getRepositoryName() != null).toList();
      for (Product product : products) {
        log.info("#loadAndStoreTestReports {}", product.getRepositoryName());
        GHRepository repository = gitHubService.getRepository(product.getRepositoryName());
        processProduct(repository);
      }
    } catch (IOException | GHException | DataAccessException e) {
      log.error("Error loading and storing test reports", e);
    }
  }

  @Transactional
  public void processProduct(GHRepository ghRepo) throws IOException {
    GithubRepo githubRepo;
    var githubRepoOptional = githubRepoRepository.findByName(ghRepo.getName());

    if (githubRepoOptional.isPresent()) {
      githubRepo = githubRepoOptional.get();
      githubRepo.getTestSteps().clear();
      githubRepo.setHtmlUrl(ghRepo.getHtmlUrl().toString());
      githubRepo.setLanguage(ghRepo.getLanguage());
      githubRepo.setLastUpdated(ghRepo.getUpdatedAt());
    } else {
      String ciBadgeUrl = buildBadgeUrl(ghRepo, CommonConstants.CI_FILE);
      githubRepo = createNewGithubRepo(ghRepo, ciBadgeUrl, buildBadgeUrl(ghRepo, CommonConstants.DEV_FILE));
    }

    githubRepo.getTestSteps().addAll(
        processWorkflowWithFallback(ghRepo, githubRepo, CommonConstants.DEV_FILE, WorkFlowType.DEV));
    githubRepo.getTestSteps().addAll(
        processWorkflowWithFallback(ghRepo, githubRepo, CommonConstants.CI_FILE, WorkFlowType.CI));
    try {
      githubRepoRepository.save(githubRepo);
    } catch (DataAccessException e) {
      log.error("Database error while saving GitHub repo: {}", ghRepo.getFullName(), e);
    }
  }

  public List<TestStep> processWorkflowWithFallback(GHRepository ghRepo, GithubRepo dbRepo,
      String workflowFileName, WorkFlowType workflowType) {
    try {
      GHWorkflowRun run = gitHubService.getLatestWorkflowRun(ghRepo, workflowFileName);
      if (run != null) {
        GHArtifact artifact = gitHubService.getExportTestArtifact(run);
        if (artifact != null) {
          return processArtifact(artifact, dbRepo, workflowType);
        }
      }
    } catch (IOException | GHException e) {
      log.warn("Workflow file '{}' not found for repo: {}. Skipping. Error: {}", workflowFileName,
          ghRepo.getFullName(), e.getMessage());
    }
    return Collections.emptyList();
  }

  private List<TestStep> processArtifact(GHArtifact artifact, GithubRepo dbRepo,
      WorkFlowType workflowType) {
    var unzipDir = Paths.get(GITHUB_REPO_DIR);
    try (InputStream zipStream = gitHubService.downloadArtifactZip(artifact)) {
      FileUtils.prepareUnZipDirectory(unzipDir);
      FileUtils.unzipArtifact(zipStream, unzipDir.toFile());

      JsonNode testData = findTestReportJson(unzipDir.toFile());
      return testStepsService.createTestSteps(dbRepo, testData, workflowType);
    } catch (IOException e) {
      log.error("IO error processing artifact for repo: {}", dbRepo.getName(), e);
    } finally {
      try {
        FileUtils.clearDirectory(unzipDir);
        Files.deleteIfExists(unzipDir);
      } catch (IOException e) {
        log.warn("Failed to clean up unzip directory: {}", unzipDir, e);
      }
    }
    return Collections.emptyList();
  }

  public JsonNode findTestReportJson(File unzipDir) throws IOException {
    var file = new File(unzipDir, REPORT_FILE_NAME);
    if (file.exists()) {
      return new ObjectMapper().readTree(file);
    }
    log.warn("No '{}' found in directory: {}", REPORT_FILE_NAME, unzipDir);
    return null;
  }


  private String buildBadgeUrl(GHRepository repo, String workflowFileName) {
    return String.format(BADGE_URL, repo.getFullName(), workflowFileName);
  }

  @Override
  public List<GithubReposModel> fetchAllRepositories() {
    List<GithubRepo> entities = githubRepoRepository.findAll();
    return entities.stream()
        .map(githubReposModelAssembler::toModel)
        .toList();
  }
}
