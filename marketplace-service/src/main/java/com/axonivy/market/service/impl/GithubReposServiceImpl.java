package com.axonivy.market.service.impl;

import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.criteria.MonitoringSearchCriteria;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.enums.WorkflowStatus;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.GithubReposModel;
import com.axonivy.market.entity.WorkflowInformation;
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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.util.Strings;
import org.kohsuke.github.GHArtifact;
import org.kohsuke.github.GHException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflow;
import org.kohsuke.github.GHWorkflowRun;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.axonivy.market.constants.DirectoryConstants.GITHUB_REPO_DIR;
import static com.axonivy.market.entity.GithubRepo.from;
import static com.axonivy.market.enums.WorkFlowType.values;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubReposServiceImpl implements GithubReposService {

  private static final String REPORT_FILE_NAME = "test_report.json";
  private static final Map<String, String> REPO_NAME_TO_PRODUCT_ID = Map.of(
      GitHubConstants.Repository.MSGRAPH_CONNECTOR, GitHubConstants.Repository.MSGRAPH_CONNECTOR,
      GitHubConstants.Repository.DOC_FACTORY, GitHubConstants.Repository.DOC_FACTORY,
      GitHubConstants.Repository.DEMO_PROJECTS, Strings.EMPTY
  );

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
      syncGithubRepos(product);
    }
  }

  @Override
  public void loadAndStoreTestReportsForOneProduct(String productId) {
    var product = productRepository.findById(productId).orElse(null);
    if (ObjectUtils.isEmpty(product)) {
      log.error("There is no this product {} to sync for monitoring page", productId);
      return;
    }
    syncGithubRepos(product);
  }

  private void syncGithubRepos(Product product) {
    try {
      log.info("#loadAndStoreTestReports Starting sync data TestReports of repo: {}", product.getRepositoryName());
      GHRepository repository = gitHubService.getRepository(product.getRepositoryName());
      processProduct(repository, product.getId());
    } catch (IOException | GHException | DataAccessException e) {
      log.error("#loadAndStoreTestReports Error processing product {}", product.getRepositoryName(), e);
    }
  }

  @Transactional
  public synchronized void processProduct(GHRepository ghRepo, String productId) throws IOException {
    if (ghRepo == null) {
      return;
    }

    String resolvedProductId = getProductId(ghRepo.getName(), productId);
    var githubRepo = Optional.ofNullable(githubRepoRepository.findByNameOrProductId(ghRepo.getName(), productId))
        .map((GithubRepo repo) -> {
          repo.getTestSteps().clear();
          repo.getWorkflowInformation().clear();
          repo.setHtmlUrl(ghRepo.getHtmlUrl().toString());
          repo.setProductId(resolvedProductId);
          return repo;
        }).orElse(from(ghRepo, resolvedProductId));
    List<TestStep> testSteps = Arrays.stream(values()).map(
        workflow -> processWorkflowWithFallback(ghRepo, githubRepo, workflow)).flatMap(Collection::stream).toList();
    githubRepo.getTestSteps().addAll(testSteps);
    githubRepoRepository.save(githubRepo);
  }

  private static String getProductId(String repoName, String productId) {
    return REPO_NAME_TO_PRODUCT_ID.entrySet().stream()
        .filter(e -> repoName.startsWith(e.getKey()))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(productId);
  }

  public List<TestStep> processWorkflowWithFallback(GHRepository ghRepo, GithubRepo dbRepo, WorkFlowType workflowType) {
    try {
      GHWorkflowRun run = gitHubService.getLatestWorkflowRun(ghRepo, workflowType.getFileName());
      if (run != null) {
        var workflowInformation = dbRepo.getWorkflowInformation().stream()
            .filter(workflow -> workflowType == workflow.getWorkflowType())
            .findFirst()
            .orElseGet(() -> {
              var newWorkflowInfo = new WorkflowInformation();
              newWorkflowInfo.setWorkflowType(workflowType);
              dbRepo.getWorkflowInformation().add(newWorkflowInfo);
              return newWorkflowInfo;
            });

        updateWorkflowInfo(ghRepo, workflowInformation, workflowType, run);
        return processActiveWorkflowArtifact(run, dbRepo, workflowInformation, workflowType);
      }
    } catch (IOException | GHException e) {
      log.warn("Workflow file '{}' not found for repo: {}. Skipping. Error: {}", workflowType.getFileName(),
          ghRepo.getFullName(), e.getMessage());

    }
    return Collections.emptyList();
  }

  private List<TestStep> processActiveWorkflowArtifact(GHWorkflowRun run, GithubRepo dbRepo,
      WorkflowInformation workflowInfo, WorkFlowType workflowType) throws IOException {

    if (WorkflowStatus.ACTIVE.getStatus().equals(workflowInfo.getCurrentWorkflowState())) {
      GHArtifact artifact = gitHubService.getExportTestArtifact(run);
      if (artifact != null) {
        return processArtifact(artifact, dbRepo, workflowType);
      }
    }
    return Collections.emptyList();
  }

  private static void updateWorkflowInfo(GHRepository ghRepo, WorkflowInformation workflowInformation,
      WorkFlowType workflowType, GHWorkflowRun run) throws IOException {
    var repoWorkflow = ghRepo.getWorkflow(workflowType.getFileName());

    addWorkflowState(repoWorkflow, workflowInformation);

    workflowInformation.setLastBuilt(run.getCreatedAt());
    workflowInformation.setConclusion(String.valueOf(run.getConclusion()));
    workflowInformation.setLastBuiltRunUrl(run.getHtmlUrl().toString());
  }

  private static void addWorkflowState(GHWorkflow repoWorkflow,
      WorkflowInformation workflowInformation) throws IOException {
    WorkflowStatus currentStatus = Arrays.stream(WorkflowStatus.values())
        .filter(workflowStatus -> workflowStatus.getStatus().equals(repoWorkflow.getState()))
        .findFirst()
        .orElse(null);

    if (currentStatus != null) {
      workflowInformation.setCurrentWorkflowState(currentStatus.getStatus());
      if(WorkflowStatus.ACTIVE != currentStatus){
        workflowInformation.setDisabledDate(repoWorkflow.getUpdatedAt());
      } else {
        workflowInformation.setDisabledDate(null);
      }
    }
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
      log.error("IO error processing artifact for repo: {}", dbRepo.getProductId(), e);
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
  public void updateFocusedRepo(List<String> repos) {
    if (repos == null || repos.isEmpty()) {
      return;
    }
    githubRepoRepository.updateFocusedRepoByName(repos);
  }

  @Override
  public Page<GithubReposModel> fetchAllRepositories(Boolean isFocused, String searchText, String workFlowType,
      String sortDirection, Pageable pageable) {

    var criteria = new MonitoringSearchCriteria();
    criteria.setSearchText(searchText);
    criteria.setWorkFlowType(workFlowType);
    criteria.setSortDirection(sortDirection);
    criteria.setIsFocused(isFocused);

    Page<GithubRepo> result = githubRepoRepository.findAllByFocusedSorted(criteria, pageable);
    List<GithubReposModel> githubRepos = result.getContent().stream().map(GithubReposModel::from).toList();
    return new PageImpl<>(githubRepos, pageable, result.getTotalElements());
  }
}
