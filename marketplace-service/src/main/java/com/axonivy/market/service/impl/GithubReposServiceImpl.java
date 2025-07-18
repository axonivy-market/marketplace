package com.axonivy.market.service.impl;

import com.axonivy.market.assembler.GithubReposModelAssembler;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.GitHubConstants;
import com.axonivy.market.constants.PreviewConstants;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.GithubReposModel;
import com.axonivy.market.repository.GithubRepoRepository;
import com.axonivy.market.service.GithubReposService;
import com.axonivy.market.service.TestStepsService;
import com.axonivy.market.util.FileUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubReposServiceImpl implements GithubReposService {

    private static final String BADGE_URL = "https://github.com/%s/actions/workflows/%s/badge.svg";
    private static final String REPORT_FILE_NAME = "test_report.json";

    @Value("${ignored.repos}")
    private List<String> ignoredRepos;
    private final GithubRepoRepository githubRepoRepository;
    private final GithubReposModelAssembler githubReposModelAssembler;
    private final TestStepsService testStepsService;
    private final GitHubService gitHubService;


    @Override
    public void loadAndStoreTestReports() {
        try {
            var ghOrganization = gitHubService.getOrganization(GitHubConstants.AXONIVY_MARKET_ORGANIZATION_NAME);
            List<GHRepository> repositories = ghOrganization.listRepositories().toList();
            for (GHRepository repository : repositories) {
                processProduct(repository);
            }
        } catch (IOException | GHException | DataAccessException e) {
            log.error("Error loading and storing test reports", e);
        }
    }

    @Transactional
    public void processProduct(GHRepository ghRepo) {
        try {
            if (!shouldInclude(ghRepo)) {
                return;
            }
            GithubRepo githubRepo = null;
            var githubRepoOptional = githubRepoRepository.findByName(ghRepo.getName());

            if (githubRepoOptional.isPresent()) {
                githubRepo = githubRepoOptional.get();
                githubRepo.getTestSteps().clear();
                githubRepoRepository.save(githubRepo);
            }

            if (githubRepo == null) {
                String ciBadgeUrl = buildBadgeUrl(ghRepo, CommonConstants.CI_FILE);
                githubRepo = createNewGithubRepo(ghRepo, ciBadgeUrl, buildBadgeUrl(ghRepo
                        , CommonConstants.DEV_FILE));
            } else {
                githubRepo.setHtmlUrl(ghRepo.getHtmlUrl().toString());
                githubRepo.setLanguage(ghRepo.getLanguage());
                githubRepo.setLastUpdated(ghRepo.getUpdatedAt());
            }

            processWorkflowWithFallback(ghRepo, githubRepo, CommonConstants.DEV_FILE, WorkFlowType.DEV);
            processWorkflowWithFallback(ghRepo, githubRepo, CommonConstants.CI_FILE, WorkFlowType.CI);

        } catch (GHFileNotFoundException e) {
            log.warn("Workflow file not found for repo: {}", ghRepo.getFullName(), e);
        } catch (IOException e) {
            log.error("IO error processing GitHub repo: {}", ghRepo.getFullName(), e);
        } catch (DataAccessException e) {
            log.error("Database error while saving GitHub repo: {}", ghRepo.getFullName(), e);
        }
    }

    private void processWorkflowWithFallback(GHRepository ghRepo, GithubRepo dbRepo,
                                             String workflowFileName, WorkFlowType workflowType) {
        try {
            GHWorkflowRun run = gitHubService.getLatestWorkflowRun(ghRepo, workflowFileName);
            if (run != null) {
                GHArtifact artifact = gitHubService.getExportTestArtifact(run);
                if (artifact != null) {
                    processArtifact(artifact, dbRepo, workflowType);
                }
            }
        } catch (IOException | GHException e) {
            log.warn("Workflow file '{}' not found for repo: {}. Skipping. Error: {}", workflowFileName,
                    ghRepo.getFullName(), e.getMessage());
        }
    }

    private void processArtifact(GHArtifact artifact, GithubRepo dbRepo, WorkFlowType workflowType) throws IOException {

        var unzipDir = Paths.get(PreviewConstants.GITHUB_REPO_DIR);
        try (InputStream zipStream = gitHubService.downloadArtifactZip(artifact)) {
            FileUtils.prepareUnZipDirectory(unzipDir);
            FileUtils.unzipArtifact(zipStream, unzipDir.toFile());

            JsonNode testData = findTestReportJson(unzipDir.toFile());
            testStepsService.createTestSteps(dbRepo, testData, workflowType);

        } finally {
            FileUtils.clearDirectory(unzipDir);
            Files.deleteIfExists(unzipDir);
        }
    }

    public JsonNode findTestReportJson(File unzipDir) throws IOException {
        var file = new File(unzipDir, REPORT_FILE_NAME);
        if (file.exists()) {
            return new ObjectMapper().readTree(file);
        }
        log.warn("No '{}' found in directory: {}", REPORT_FILE_NAME, unzipDir);
        return null;
    }

    private static GithubRepo createNewGithubRepo(GHRepository repo, String ciBadgeUrl,
                                                  String devBadgeUrl) throws IOException {
        return GithubRepo.builder()
                .name(repo.getName())
                .htmlUrl(repo.getHtmlUrl().toString())
                .language(repo.getLanguage())
                .lastUpdated(repo.getUpdatedAt())
                .ciBadgeUrl(ciBadgeUrl)
                .devBadgeUrl(devBadgeUrl)
                .build();
    }

    private static String buildBadgeUrl(GHRepository repo, String workflowFileName) {
        return String.format(BADGE_URL, repo.getFullName(), workflowFileName);
    }

    private boolean shouldInclude(GHRepository repo) {
        boolean isValidRepo = !repo.isArchived() && !repo.isTemplate() && "master".equals(repo.getDefaultBranch());
        boolean isRelevant = repo.getLanguage() != null && !ignoredRepos.contains(repo.getName());

        return isValidRepo && isRelevant;
    }

    @Override
    public List<GithubReposModel> fetchAllRepositories() {
        List<GithubRepo> entities = githubRepoRepository.findAll();
        return entities.stream()
                .map(githubReposModelAssembler::toModel)
                .toList();
    }
}
