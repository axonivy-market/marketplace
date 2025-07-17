package com.axonivy.market.service.impl;

import com.axonivy.market.assembler.GithubReposModelAssembler;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.TestSteps;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.GithubReposModel;
import com.axonivy.market.repository.GithubRepoRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.GithubArtifactExtract;
import com.axonivy.market.service.GithubReposService;
import com.axonivy.market.service.TestStepsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHArtifact;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowRun;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubReposServiceImpl implements GithubReposService {
  @Value("${ignored.repos}")
  List<String> ignoredRepos;
  private final GithubRepoRepository githubRepoRepository;
  private final GithubReposModelAssembler githubReposModelAssembler;
  private final TestStepsService testStepsService;
  private final GitHubService gitHubService;
  private final GithubArtifactExtract githubArtifactExtract;

  private static final String BADGE_URL = "https://github.com/%s/actions/workflows/%s/badge.svg";
  private static final String REPORT_FILE_NAME = "test_report.json";

  @Override
  public void loadAndStoreTestReports() {
    try {
      GHOrganization ghOrganization = gitHubService.getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
      List<GHRepository> repositories = ghOrganization.listRepositories().toList();
      log.info("Found {} repositories in organization {}", repositories.size(), GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
      for (GHRepository repository : repositories) {
        processProduct(repository);
      }
    } catch (Exception e) {
      log.error("Error loading and storing test reports", e);
    }
  }

  @Transactional
  public void processProduct(GHRepository ghRepo) {
    try {
      if (!shouldInclude(ghRepo)) {
        return;
      }
      GithubRepo githubRepo = testStepsService.deleteExistingGithubRepoIfExists(ghRepo.getName());
      if(githubRepo==null) {
        githubRepo = createNewGithubRepo(ghRepo, buildBadgeUrl(ghRepo, "ci.yml"), buildBadgeUrl(ghRepo, "dev.yml"));
      } else {
        githubRepo.setHtmlUrl(ghRepo.getHtmlUrl().toString());
        githubRepo.setLanguage(ghRepo.getLanguage());
        githubRepo.setLastUpdated(ghRepo.getUpdatedAt());
      }
      GithubRepo savedRepo = githubRepoRepository.save(githubRepo);
      githubRepoRepository.flush();

      savedRepo = githubRepoRepository.findById(savedRepo.getId())
          .orElseThrow(() -> new RuntimeException("Failed to save GitHub repo: " + ghRepo.getName()));

      processWorkflowWithFallback(ghRepo, savedRepo, "dev.yml", "DEV");
      processWorkflowWithFallback(ghRepo, savedRepo, "ci.yml", "CI");

    } catch (Exception e) {
      log.error("Error processing product repo: {}", e);
    }
  }


  private void processWorkflowWithFallback(GHRepository ghRepo, GithubRepo dbRepo,
      String workflowFileName, String workflowType) {
    try {
      GHWorkflowRun run = gitHubService.getLatestWorkflowRun(ghRepo, workflowFileName);
      if (run != null) {
        GHArtifact artifact = gitHubService.getExportTestArtifact(run);
        if (artifact != null) {
          try (InputStream zipStream = gitHubService.downloadArtifactZip(artifact)) {
            File unzipDir = githubArtifactExtract.extractZipToTempDir(zipStream, ghRepo.getName());
            JsonNode testData = findTestReportJson(unzipDir);
            testStepsService.createTestSteps(dbRepo, testData, workflowType);
          }
        }
      }

    } catch (Exception e) {
      log.error("Error processing workflow {} for repo {}", workflowType, dbRepo.getName(), e);
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

  @Override
  public List<GithubReposModel> fetchAllRepositories() {
    List<GithubRepo> entities = githubRepoRepository.findAll();
    return entities.stream()
        .map(githubReposModelAssembler::toModel)
        .collect(Collectors.toList());
  }
}