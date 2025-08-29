package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.BuildInformation;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static com.axonivy.market.constants.DirectoryConstants.GITHUB_REPO_DIR;
import static com.axonivy.market.entity.GithubRepo.from;
import static com.axonivy.market.enums.WorkFlowType.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubReposServiceImpl implements GithubReposService {

  private static final String REPORT_FILE_NAME = "test_report.json";

  private final GithubRepoRepository githubRepoRepository;
  private final TestStepsService testStepsService;
  private final GitHubService gitHubService;
  private final ProductRepository productRepository;

  @Override
  public void loadAndStoreTestReports() {
    List<Product> products = productRepository.findAll().stream()
            .filter(product -> Boolean.FALSE != product.getListed()
                    && product.getRepositoryName() != null).toList();

    for (Product product : products) {
      try {
        log.info("#loadAndStoreTestReports Starting sync data TestReports of repo: {}", product.getRepositoryName());
        GHRepository repository = gitHubService.getRepository(product.getRepositoryName());
        processProduct(repository);
      } catch (IOException | GHException | DataAccessException e) {
        log.error("#loadAndStoreTestReports Error processing product {}", product.getRepositoryName(), e);
      }
    }
  }

  @Transactional
  public synchronized void processProduct(GHRepository ghRepo) throws IOException {
    if (ghRepo == null) {
      return;
    }
    GithubRepo githubRepo;
    var githubRepoOptional = githubRepoRepository.findByName(ghRepo.getName());
      if (githubRepoOptional.isPresent()) {
        githubRepo = githubRepoOptional.get();
        githubRepo.getTestSteps().clear();
//        githubRepo.getWorkflows().clear();
        githubRepo.setHtmlUrl(ghRepo.getHtmlUrl().toString());
      } else {
        githubRepo = from(ghRepo);
      }
      List<TestStep> testSteps = Arrays.stream(values()).map(
          workflow -> processWorkflowWithFallback(ghRepo, githubRepo, workflow)).flatMap(Collection::stream).toList();
      githubRepo.getTestSteps().addAll(testSteps);
      githubRepoRepository.save(githubRepo);
  }

  public List<TestStep> processWorkflowWithFallback(GHRepository ghRepo, GithubRepo dbRepo,
      WorkFlowType workflowType) {
    try {
      GHWorkflowRun run = gitHubService.getLatestWorkflowRun(ghRepo, workflowType.getFileName());
      if (run != null) {
        updateBuildInfo(dbRepo, workflowType, run);
        GHArtifact artifact = gitHubService.getExportTestArtifact(run);
        if (artifact != null) {
          return processArtifact(artifact, dbRepo, workflowType);
        }
      }
    } catch (IOException | GHException e) {
      log.warn("Workflow file '{}' not found for repo: {}. Skipping. Error: {}", workflowType.getFileName(),
          ghRepo.getFullName(), e.getMessage());
    }
    return Collections.emptyList();
  }

  private void updateBuildInfo(GithubRepo dbRepo, WorkFlowType workflowType, GHWorkflowRun ghWorkflowRun)
      throws IOException {
    if (dbRepo.getWorkflows() == null) {
      dbRepo.setWorkflows(new HashMap<>());
    }

    BuildInformation buildInfo = dbRepo.getWorkflows()
        .computeIfAbsent(WorkFlowType.valueOf(workflowType.name()), k -> new BuildInformation());

    buildInfo.setLastBuilt(ghWorkflowRun.getCreatedAt());
    buildInfo.setConclusion(String.valueOf(ghWorkflowRun.getConclusion()));
    buildInfo.setLastBuiltRun(ghWorkflowRun.getHtmlUrl().toString());
  }

  private List<TestStep> processArtifact(GHArtifact artifact, GithubRepo dbRepo,
      WorkFlowType workflowType) {
    var unzipDir = Paths.get(GITHUB_REPO_DIR);
    try (InputStream zipStream = gitHubService.downloadArtifactZip(artifact)) {
      FileUtils.prepareUnZipDirectory(unzipDir);
      FileUtils.unzipArtifact(zipStream, unzipDir.toFile());

      JsonNode testData = findTestReportJson(unzipDir.toFile());
      return testStepsService.createTestSteps(testData, workflowType);
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

  @Override
  public List<GithubReposModel> fetchAllRepositories() {
    List<GithubRepo> entities = githubRepoRepository.findAll();
    return entities.stream()
        .map(GithubReposModel::from)
        .toList();
  }

  @Override
  public void updateFocusedRepo(List<String> repos) {
    if (repos == null || repos.isEmpty()) {
      return;
    }
    githubRepoRepository.updateFocusedRepoByName(repos);
  }
}
