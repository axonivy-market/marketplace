package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.TestSteps;
import com.axonivy.market.entity.WorkflowRepo;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.repository.GithubRepoRepository;
import com.axonivy.market.repository.TestStepsRepository;
import com.axonivy.market.repository.WorkflowRepoRepository;
import com.axonivy.market.service.TestStepsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.kohsuke.github.GHArtifact;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflowRun;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServiceImplTest {

  @Mock
  private WorkflowRepoRepository workflowRepoRepository;

  @Mock
  private WorkflowRepoProcessorImpl workflowProcessor;

  @Mock
  private GithubRepoRepository githubRepoRepository;

  @Mock
  private TestStepsRepository testStepsRepository;

  @Mock
  private TestStepsService testStepsService;

  @Mock
  private GitHubService gitHubService;

  @Mock
  private GithubArtifactExtractImpl githubArtifactExtract;

  @Spy
  @InjectMocks
  private WorkflowServiceImpl workflowService;

  @TempDir
  File tempDir;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(workflowService, "ignoredRepos", Arrays.asList("ignored-repo"));
  }

  @Test
  void processProductValidRepositorySuccess() throws Exception {
    Product product = new Product();
    product.setRepositoryName("owner/repo");

    GHRepository ghRepo = mockGHRepository("repo", "master", "Java", false, false);
    when(ghRepo.getFullName()).thenReturn("owner/repo");
    when(ghRepo.getHtmlUrl()).thenReturn(new URL("https://github.com/owner/repo"));
    when(ghRepo.getUpdatedAt()).thenReturn(new Date());

    GHWorkflowRun workflowRun = mock(GHWorkflowRun.class);
    GHArtifact artifact = mock(GHArtifact.class);

    when(gitHubService.getRepository("owner/repo")).thenReturn(ghRepo);
    when(gitHubService.getLatestWorkflowRun(any(), eq("ci.yml"))).thenReturn(workflowRun);
    when(gitHubService.getLatestWorkflowRun(any(), eq("dev.yml"))).thenReturn(workflowRun);
    when(gitHubService.getExportTestArtifact(workflowRun)).thenReturn(artifact);

    ByteArrayInputStream zipStream = new ByteArrayInputStream("test data".getBytes());
    when(gitHubService.downloadArtifactZip(artifact)).thenReturn(zipStream);
    when(githubArtifactExtract.extractZipToTempDir(any(InputStream.class), anyString())).thenReturn(tempDir);

    when(githubRepoRepository.findByName("repo")).thenReturn(Optional.empty());

    GithubRepo savedRepo = new GithubRepo();
    savedRepo.setId(UUID.randomUUID().toString());
    savedRepo.setName("repo");
    when(githubRepoRepository.save(any(GithubRepo.class))).thenReturn(savedRepo);
    when(githubRepoRepository.findById(savedRepo.getId())).thenReturn(Optional.of(savedRepo));

    ObjectMapper mapper = new ObjectMapper();
    JsonNode testData = mapper.readTree("{\"tests\": []}");
    doReturn(testData).when(workflowService).findTestReportJson(any(File.class));

    WorkflowRepo workflowRepo = new WorkflowRepo();
    when(workflowProcessor.createWorkflowRepo(any(), any(), anyString())).thenReturn(workflowRepo);
    when(workflowRepoRepository.save(workflowRepo)).thenReturn(workflowRepo);

    workflowService.processProduct(product);

    verify(githubRepoRepository).save(any(GithubRepo.class));
    verify(workflowRepoRepository, times(2)).save(any(WorkflowRepo.class));
    verify(testStepsService, times(2)).createNewTestSteps(any(WorkflowRepo.class), any(JsonNode.class));
  }

  @Test
  void processProductInvalidRepositoryPathReturnsEarly() throws IOException {
    Product product = new Product();
    product.setRepositoryName(null);
    workflowService.processProduct(product);
    verify(gitHubService, never()).getRepository(anyString());
  }

  @Test
  void processProductIgnoredRepositoryReturnsEarly() throws Exception {
    Product product = new Product();
    product.setRepositoryName("owner/ignored-repo");

    GHRepository ghRepo = mockGHRepository("ignored-repo", "master", "Java", false, false);
    when(gitHubService.getRepository("owner/ignored-repo")).thenReturn(ghRepo);

    workflowService.processProduct(product);
    verify(githubRepoRepository, never()).save(any(GithubRepo.class));
  }

  @Test
  void processProductExceptionHandledGracefully() throws Exception {
    Product product = new Product();
    product.setRepositoryName("owner/repo");
    when(gitHubService.getRepository("owner/repo")).thenThrow(new IOException("Network error"));
    assertDoesNotThrow(() -> workflowService.processProduct(product));
  }

  @Test
  void deleteExistingGithubRepoIfExistsExistingRepoDeletesRelatedData() {
    GithubRepo existingRepo = new GithubRepo();
    existingRepo.setId("123");
    existingRepo.setName("existing-repo");

    WorkflowRepo workflow = new WorkflowRepo();
    workflow.setId("456");

    TestSteps testStep = new TestSteps();

    when(githubRepoRepository.findByName("existing-repo")).thenReturn(Optional.of(existingRepo));
    when(workflowRepoRepository.findByRepository(existingRepo)).thenReturn(Collections.singletonList(workflow));
    when(testStepsRepository.findByWorkflowId("456")).thenReturn(Collections.singletonList(testStep));

    workflowService.deleteExistingGithubRepoIfExists("existing-repo");

    verify(testStepsRepository).deleteAll(Collections.singletonList(testStep));
    verify(workflowRepoRepository).delete(workflow);
    verify(githubRepoRepository).delete(existingRepo);
  }

  @Test
  void deleteExistingGithubRepoIfExistsNonExistingRepoDoesNothing() {
    when(githubRepoRepository.findByName("non-existing-repo")).thenReturn(Optional.empty());
    workflowService.deleteExistingGithubRepoIfExists("non-existing-repo");
    verify(workflowRepoRepository, never()).delete(any());
    verify(githubRepoRepository, never()).delete(any());
  }

  @Test
  void deleteExistingGithubRepoIfExistsExceptionPropagatesException() {
    GithubRepo existingRepo = new GithubRepo();
    existingRepo.setId("123");
    existingRepo.setName("existing-repo");

    when(githubRepoRepository.findByName("existing-repo")).thenReturn(Optional.of(existingRepo));
    when(workflowRepoRepository.findByRepository(existingRepo)).thenThrow(new RuntimeException("Database error"));

    assertThrows(RuntimeException.class, () ->
        workflowService.deleteExistingGithubRepoIfExists("existing-repo"));
  }

  @Test
  void shouldIncludeValidRepoReturnsTrue() {
    GHRepository repo = mock(GHRepository.class);
    when(repo.getName()).thenReturn("valid-repo");
    when(repo.getDefaultBranch()).thenReturn("master");
    when(repo.getLanguage()).thenReturn("Java");
    when(repo.isArchived()).thenReturn(false);
    when(repo.isTemplate()).thenReturn(false);

    boolean result = ReflectionTestUtils.invokeMethod(workflowService, "shouldInclude", repo);
    assertEquals(true, result);
  }

  @Test
  void buildBadgeUrlReturnsCorrectUrl() {
    GHRepository repo = mock(GHRepository.class);
    when(repo.getFullName()).thenReturn("owner/test-repo");
    String result = ReflectionTestUtils.invokeMethod(workflowService, "buildBadgeUrl", repo, "ci.yml");
    assertEquals("https://github.com/owner/test-repo/actions/workflows/ci.yml/badge.svg", result);
  }

  @Test
  void createNewGithubRepoReturnsCorrectEntity() throws Exception {
    GHRepository repo = mock(GHRepository.class);
    when(repo.getName()).thenReturn("test-repo");
    when(repo.getHtmlUrl()).thenReturn(new URL("https://github.com/owner/test-repo"));
    when(repo.getLanguage()).thenReturn("Java");
    when(repo.getUpdatedAt()).thenReturn(new Date());

    GithubRepo result = ReflectionTestUtils.invokeMethod(
        workflowService,
        "createNewGithubRepo",
        repo,
        "ci-badge-url",
        "dev-badge-url"
    );

    assertEquals("test-repo", result.getName());
    assertEquals("https://github.com/owner/test-repo", result.getHtmlUrl());
    assertEquals("Java", result.getLanguage());
    assertEquals("ci-badge-url", result.getCiBadgeUrl());
    assertEquals("dev-badge-url", result.getDevBadgeUrl());
  }
  @Test
  void findTestReportJsonFileNotFoundReturnsNull() throws IOException {
    File emptyDir = tempDir;
    JsonNode result = workflowService.findTestReportJson(emptyDir);
    assertNull(result);
  }

  @Test
  void createNewWorkflowWithNullTestData() {
    // Arrange
    GithubRepo repo = new GithubRepo();
    repo.setName("test-repo");

    // Act
    WorkflowRepo result = ReflectionTestUtils.invokeMethod(
        workflowService,
        "createNewWorkflow",
        repo, null, "CI");

    // Assert
    assertNotNull(result);
    assertEquals(repo, result.getRepository());
    assertEquals("CI", result.getType());
    assertEquals(0, result.getPassed());
    assertEquals(0, result.getFailed());
    assertEquals(0, result.getMockPassed());
    assertEquals(0, result.getMockFailed());
    assertEquals(0, result.getRealPassed());
    assertEquals(0, result.getRealFailed());
  }
  @Test
  void shouldIncludeNullLanguageReturnsFalse() {
    GHRepository repo = mock(GHRepository.class);
    when(repo.getDefaultBranch()).thenReturn("master");
    when(repo.isArchived()).thenReturn(false);
    when(repo.isTemplate()).thenReturn(false);
    when(repo.getLanguage()).thenReturn(null);

    boolean result = ReflectionTestUtils.invokeMethod(workflowService, "shouldInclude", repo);
    assertFalse(result);
  }

  private GHRepository mockGHRepository(String name, String defaultBranch, String language,
      boolean archived, boolean template) throws IOException {
    GHRepository repo = mock(GHRepository.class);
    lenient().when(repo.getName()).thenReturn(name);
    lenient().when(repo.getDefaultBranch()).thenReturn(defaultBranch);
    lenient().when(repo.getLanguage()).thenReturn(language);
    lenient().when(repo.isArchived()).thenReturn(archived);
    lenient().when(repo.isTemplate()).thenReturn(template);
    return repo;
  }
}
