package com.axonivy.market.service.impl;

import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.GithubReposService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubReposServiceImpl implements GithubReposService {

  @Value("${market.github.token}")
  private String githubToken;

  private final FileDownloadService fileDownloadService;

  private static final String REPOS_URL = "https://api.github.com/orgs/axonivy-market/repos?per_page=100";

  private final RestTemplate restTemplate = new RestTemplate();

  @Override
  public String fetchAllRepositories() {
    HttpHeaders headers = createHeaders();
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {
      ResponseEntity<String> response = restTemplate.exchange(
          REPOS_URL,
          HttpMethod.GET,
          entity,
          String.class
      );
      return response.getBody();
    } catch (Exception e) {
      log.error("Failed to fetch GitHub repositories", e);
      throw new RuntimeException("Error fetching repositories from GitHub");
    }
  }

  @Override
  public String fetchWorkflowRuns(String repo, String workflow) {
    JsonNode run = fetchLatestWorkflowRun("alfresco-connector", "ci.yml");
    if (run != null) {
      long runId = run.path("id").asLong();
      JsonNode artifact = fetchExportTestJsonArtifact("alfresco-connector", runId);
      if (artifact != null) {
        String downloadUrl = artifact.path("archive_download_url").asText();
        log.info("Artifact download URL: {}", downloadUrl);
        try {
          byte[] fileContent = fileDownloadService.safeDownload(downloadUrl);
          if (fileContent != null && fileContent.length > 0) {
            String fileName = artifact.path("name").asText() + ".zip";
            return DownloadOption.builder().toString();
          } else {
            log.warn("No content found for artifact: {}", artifact.path("name").asText());
          }
        } catch (Exception e) {
          log.error("Error downloading artifact", e);
          throw new RuntimeException("Error downloading artifact");
        }
      }
    }

    return "";
  }


  public JsonNode fetchLatestWorkflowRun(String repo, String workflow) {
    String url = String.format("https://api.github.com/repos/axonivy-market/%s/actions/workflows/%s/runs", repo, workflow);
    HttpHeaders headers = createHeaders();
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {
      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(response.getBody());

      JsonNode workflowRuns = root.path("workflow_runs");
      if (workflowRuns.isArray() && !workflowRuns.isEmpty()) {
        return workflowRuns.get(0);
      } else {
        log.warn("No workflow runs found for {} / {}", repo, workflow);
        return null;
      }
    } catch (Exception e) {
      log.error("Failed to fetch workflow runs", e);
      throw new RuntimeException("Error fetching workflow runs");
    }
  }
  public JsonNode fetchExportTestJsonArtifact(String repo, long runId) {
    String url = String.format("https://api.github.com/repos/axonivy-market/%s/actions/runs/%d/artifacts", repo, runId);
    HttpHeaders headers = createHeaders();
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    try {
      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
      ObjectMapper mapper = new ObjectMapper();
      JsonNode root = mapper.readTree(response.getBody());

      JsonNode artifacts = root.path("artifacts");
      for (JsonNode artifact : artifacts) {
        if ("export-test-json-file".equals(artifact.path("name").asText())) {
          return artifact;
        }
      }

      log.warn("Artifact 'export-test-json-file' not found in run {}", runId);
      return null;
    } catch (Exception e) {
      log.error("Failed to fetch artifacts for run {}", runId, e);
      throw new RuntimeException("Error fetching artifacts");
    }
  }
  public void processWorkflow(Repository repoEntity, String workflow) {
    JsonNode run = fetchLatestWorkflowRun(repoEntity.getName(), workflow);
    if (run == null) return;

    long runId = run.path("id").asLong();
    JsonNode artifact = fetchExportTestJsonArtifact(repoEntity.getName(), runId);
    if (artifact == null) return;

    String downloadUrl = artifact.path("archive_download_url").asText();

    try {
      String unzipDir = fileDownloadService.downloadAndUnzipFile(downloadUrl, null);
      File testFile = Paths.get(unzipDir, "export-test.json").toFile();
      if (testFile.exists()) {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode testData = mapper.readTree(testFile);
        storeTestData(repoEntity, workflow, testData);
      }
    } catch (Exception e) {
      log.error("Error processing workflow {} for repo {}", workflow, repoEntity.getName(), e);
    }
  }

  private HttpHeaders createHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Accept", "application/vnd.github+json");
    headers.set("Authorization", "Bearer " + githubToken);
    return headers;
  }
}
