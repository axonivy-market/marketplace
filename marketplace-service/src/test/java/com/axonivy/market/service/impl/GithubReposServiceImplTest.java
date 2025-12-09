package com.axonivy.market.service.impl;

import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.enums.WorkflowStatus;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.entity.WorkflowInformation;
import com.axonivy.market.repository.GithubRepoRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.TestStepsService;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kohsuke.github.GHArtifact;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHWorkflow;
import org.kohsuke.github.GHWorkflowRun;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.axonivy.market.enums.WorkFlowType.CI;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GithubReposServiceImplTest {

  private static final String URL_EXAMPLE = "https://github.com/test/demo";
  @InjectMocks
  GithubReposServiceImpl service;

  @Mock
  GithubRepoRepository githubRepoRepository;

  GithubReposServiceImpl serviceSpy;
  @Mock
  private TestStepsService testStepsService;
  @Mock
  private GitHubService gitHubService;
  @Mock
  private ProductRepository productRepository;
  @TempDir
  Path tempDir;
  private GHRepository ghRepo;
  private GithubRepo dbRepo;

  @BeforeEach
  void setUp() throws IOException, URISyntaxException {
    MockitoAnnotations.openMocks(this);
    serviceSpy = spy(service);

    ghRepo = mock(GHRepository.class);
    when(ghRepo.getName()).thenReturn("demo");
    when(ghRepo.getHtmlUrl()).thenReturn(new URI(URL_EXAMPLE).toURL());
    when(ghRepo.getUpdatedAt()).thenReturn(new Date());
    when(ghRepo.getFullName()).thenReturn("owner/demo");

    dbRepo = GithubRepo.builder()
        .productId("demo")
        .htmlUrl("https://old-url")
        .workflowInformation(new HashSet<>())
        .testSteps(new HashSet<>())
        .build();
  }

  @Test
  void testProcessProductExistingRepo() {
    doReturn(List.of(new TestStep())).when(serviceSpy)
        .processWorkflowWithFallback(any(), any(), any());

    assertDoesNotThrow(() -> serviceSpy.processProduct(ghRepo, dbRepo.getProductId()),
        "Processing product should not throw an exception");

    verify(githubRepoRepository).save(any());
  }

  @Test
  void testProcessProductNewRepo() {
    doReturn(List.of(new TestStep())).when(serviceSpy)
        .processWorkflowWithFallback(any(), any(), any());

    assertDoesNotThrow(() -> serviceSpy.processProduct(ghRepo, dbRepo.getProductId()),
        "Processing product should not throw an exception");

    verify(githubRepoRepository).save(any());
  }

  @Test
  void testProcessProductDataAccessException() {
    doReturn(List.of(new TestStep())).when(serviceSpy)
        .processWorkflowWithFallback(any(), any(), any());

    doThrow(new DataAccessException("DB error") {
    }).when(githubRepoRepository).save(any());

    String productId = dbRepo.getProductId();
    assertThrows(DataAccessException.class, () -> serviceSpy.processProduct(ghRepo, productId),
        "Should throw DataAccessException");
  }

  @Test
  void testLoadAndStoreTestReportsSuccess() throws Exception {
    Product product = new Product();
    product.setRepositoryName("repo1");
    product.setListed(true);

    when(productRepository.findAll()).thenReturn(List.of(product));
    when(gitHubService.getRepository(product.getRepositoryName())).thenReturn(ghRepo);
    doReturn(List.of(new TestStep())).when(serviceSpy)
        .processWorkflowWithFallback(any(), any(), any());
    assertDoesNotThrow(() -> serviceSpy.loadAndStoreTestReports(),
        "Should not throw an exception even if GitHub service fails");
    verify(githubRepoRepository).save(any());
  }

  @Test
  void testLoadAndStoreTestReportsWithIOException() throws Exception {
    Product product = new Product();
    product.setRepositoryName("repo1");
    product.setListed(true);

    when(productRepository.findAll()).thenReturn(List.of(product));
    when(gitHubService.getRepository("repo1")).thenThrow(new IOException("Error"));

    assertDoesNotThrow(() -> service.loadAndStoreTestReports(),
        "Should not throw an exception even if GitHub service fails");
  }

  @Test
  void testProcessWorkflowWithFallbackSuccess() throws Exception {
    GHWorkflowRun run = mock(GHWorkflowRun.class);
    GHArtifact artifact = mock(GHArtifact.class);
    JsonNode jsonNode = mock(JsonNode.class);
    GHWorkflow workflow = mock(GHWorkflow.class);

    when(ghRepo.getWorkflow(CI.getFileName())).thenReturn(workflow);
    when(workflow.getState()).thenReturn(WorkflowStatus.ACTIVE.getStatus());
    when(workflow.getUpdatedAt()).thenReturn(new Date());

    when(gitHubService.getLatestWorkflowRun(ghRepo, CI.getFileName())).thenReturn(run);
    when(gitHubService.getExportTestArtifact(run)).thenReturn(artifact);
    when(gitHubService.downloadArtifactZip(artifact)).thenReturn(getZipWithTestReport());
    when(run.getHtmlUrl()).thenReturn(URI.create(URL_EXAMPLE).toURL());

    when(serviceSpy.findTestReportJson(any())).thenReturn(jsonNode);
    when(testStepsService.createTestSteps(any(), eq(WorkFlowType.CI)))
        .thenReturn(List.of(new TestStep()));

    List<TestStep> steps = service.processWorkflowWithFallback(ghRepo, dbRepo, WorkFlowType.CI);

    assertEquals(1, steps.size(),
        "Should return one test step when workflow run and artifact are found");

    WorkflowInformation workflowInformation = dbRepo.getWorkflowInformation().stream()
        .filter(info -> WorkFlowType.CI == info.getWorkflowType())
        .findFirst()
        .orElse(null);

    assertNotNull(workflowInformation, "Workflow information entry should exist for CI");
    assertEquals(WorkflowStatus.ACTIVE.getStatus(), workflowInformation.getCurrentWorkflowState(),
        "Should record active state");
    assertNull(workflowInformation.getDisabledDate(), "Disabled date should be null for active state");
  }

  @Test
  void testProcessWorkflowWithFallbackHandlesException() throws Exception {
    when(gitHubService.getLatestWorkflowRun(any(), any()))
        .thenThrow(new IOException("Simulated IO error"));

    List<TestStep> result = service.processWorkflowWithFallback(ghRepo, dbRepo, WorkFlowType.CI);

    assertTrue(result.isEmpty(), "Result list should be empty when exception is thrown");
  }

  @Test
  void testProcessWorkflowWithFallbackNoRun() throws Exception {
    when(gitHubService.getLatestWorkflowRun(any(), any())).thenReturn(null);
    List<TestStep> result = service.processWorkflowWithFallback(ghRepo, dbRepo, WorkFlowType.CI);

    assertTrue(result.isEmpty(), "Result list should be empty when no workflow run is found");
  }

  @Test
  void testFindTestReportJsonFound() throws IOException {
    File json = tempDir.resolve("test_report.json").toFile();
    Files.writeString(json.toPath(), "{\"summary\": \"some result\"}");

    var result = service.findTestReportJson(tempDir.toFile());

    assertNotNull(result, "Result should not be null when test report JSON is found");
    assertEquals("some result", result.get("summary").asText(),
        "The summary in the JSON should match the expected value");
  }

  @Test
  void testFindTestReportJsonNotFound() throws IOException {
    var result = service.findTestReportJson(tempDir.toFile());
    assertNull(result, "Result should be null when no test report JSON is found");
  }

  private InputStream getZipWithTestReport() throws IOException {
    Path zipPath = tempDir.resolve("test.zip");
    try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(zipPath))) {
      ZipEntry entry = new ZipEntry("test_report.json");
      zipOut.putNextEntry(entry);
      zipOut.write("{\"summary\":\"test\"}".getBytes());
      zipOut.closeEntry();
    }
    return Files.newInputStream(zipPath);
  }

  @Test
  void testUpdateFocused() {
    List<String> updates = List.of("repo1", "repo2");

    service.updateFocusedRepo(updates);

    verify(githubRepoRepository).updateFocusedRepoByName(updates);
  }

  @Test
  void testUpdateFocusedWithEmptyList() {
    List<String> updates = new ArrayList<>();

    service.updateFocusedRepo(updates);

    verify(githubRepoRepository, never()).updateFocusedRepoByName(any());
  }

  @Test
  void testUpdateFocusedWithNullList() {
    List<String> updates = null;

    service.updateFocusedRepo(updates);

    verify(githubRepoRepository, never()).updateFocusedRepoByName(any());
  }

  @Test
  void testProcessProductWithNullRepo() {
    assertDoesNotThrow(() -> service.processProduct(null, dbRepo.getProductId()),
        "Should handle null repository gracefully");

    verifyNoInteractions(githubRepoRepository);
  }

  @Test
  void testProcessArtifactWithIOException() throws Exception {
    GHWorkflowRun run = mock(GHWorkflowRun.class);
    GHArtifact artifact = mock(GHArtifact.class);
    GHWorkflow workflow = mock(GHWorkflow.class);

    when(gitHubService.getLatestWorkflowRun(ghRepo, WorkFlowType.CI.getFileName())).thenReturn(run);
    when(gitHubService.getExportTestArtifact(run)).thenReturn(artifact);
    when(gitHubService.downloadArtifactZip(artifact)).thenThrow(new IOException("Download failed"));
    when(run.getHtmlUrl()).thenReturn(URI.create(URL_EXAMPLE).toURL());
    when(ghRepo.getWorkflow(WorkFlowType.CI.getFileName())).thenReturn(workflow);
    when(workflow.getState()).thenReturn(WorkflowStatus.DISABLED_MANUALLY.getStatus());
    Date disabledDate = new Date();
    when(workflow.getUpdatedAt()).thenReturn(disabledDate);

    List<TestStep> result = service.processWorkflowWithFallback(ghRepo, dbRepo, WorkFlowType.CI);

    assertTrue(result.isEmpty(), "Should return empty list when IO exception occurs");
    verify(gitHubService).getLatestWorkflowRun(ghRepo, WorkFlowType.CI.getFileName());

    WorkflowInformation workflowInformation = dbRepo.getWorkflowInformation().stream()
        .filter(info -> WorkFlowType.CI == info.getWorkflowType())
        .findFirst()
        .orElse(null);

    assertNotNull(workflowInformation, "Workflow info should exist despite artifact IO error");
    assertEquals(WorkflowStatus.DISABLED_MANUALLY.getStatus(), workflowInformation.getCurrentWorkflowState(),
        "Should capture disabled state");
    assertEquals(disabledDate, workflowInformation.getDisabledDate(),
        "Disabled date should be set when workflow disabled");
  }

  @Test
  void testProcessExistingRepo() throws Exception {
    GithubRepo existingRepo = new GithubRepo();
    existingRepo.setProductId("demo");
    existingRepo.setWorkflowInformation(new HashSet<>());
    existingRepo.getWorkflowInformation().add(new WorkflowInformation());
    existingRepo.setTestSteps(new HashSet<>());
    existingRepo.getTestSteps().add(new TestStep());

    when(githubRepoRepository.findByNameOrProductId(ghRepo.getName(), existingRepo.getProductId()))
        .thenReturn(existingRepo);
    doReturn(List.of()).when(serviceSpy).processWorkflowWithFallback(any(), any(), any());

    serviceSpy.processProduct(ghRepo, dbRepo.getProductId());

    assertEquals(ghRepo.getHtmlUrl().toString(), existingRepo.getHtmlUrl(),
        "HTML URL should be updated");
    assertTrue(existingRepo.getWorkflowInformation().isEmpty(),
        "Workflow information should be cleared before processing");
    assertTrue(existingRepo.getTestSteps().isEmpty(),
        "Test steps should be cleared before processing");

    verify(githubRepoRepository).save(existingRepo);
  }

  @Test
  void testUpdateWorkflowInfoForAllWorkflowTypes() throws Exception {
    GHWorkflowRun run = mock(GHWorkflowRun.class);
    when(run.getHtmlUrl()).thenReturn(URI.create(URL_EXAMPLE).toURL());
    when(gitHubService.getLatestWorkflowRun(any(), any())).thenReturn(run);

    for (WorkFlowType type : WorkFlowType.values()) {
      GHWorkflow workflow = mock(GHWorkflow.class);
      when(ghRepo.getWorkflow(type.getFileName())).thenReturn(workflow);
      when(workflow.getState()).thenReturn(WorkflowStatus.ACTIVE.getStatus());
      when(workflow.getUpdatedAt()).thenReturn(new Date());
    }

    for (WorkFlowType type : WorkFlowType.values()) {
      service.processWorkflowWithFallback(ghRepo, dbRepo, type);
    }

    assertEquals(WorkFlowType.values().length, dbRepo.getWorkflowInformation().size(),
        "Should have one WorkflowInformation entry per workflow type"
    );

    dbRepo.getWorkflowInformation().forEach(info -> {
      assertEquals(WorkflowStatus.ACTIVE.getStatus(), info.getCurrentWorkflowState(),
          "Each workflow should be active in this test");
      assertNull(info.getDisabledDate(), "Disabled date should be null for active workflows");
    });
  }

  @Test
  void testUpdateWorkflowInfoCreatesNewEntry() throws Exception {
    GHRepository ghRepoMock = mock(GHRepository.class);
    GHWorkflowRun run = mock(GHWorkflowRun.class);
    GHWorkflow workflow = mock(GHWorkflow.class);

    when(run.getConclusion()).thenReturn(GHWorkflowRun.Conclusion.SUCCESS);
    when(run.getHtmlUrl()).thenReturn(URI.create(URL_EXAMPLE).toURL());
    Date workflowUpdated = new Date();

    when(gitHubService.getLatestWorkflowRun(any(), any())).thenReturn(run);
    when(gitHubService.getExportTestArtifact(run)).thenReturn(null);

    when(ghRepoMock.getWorkflow(any())).thenReturn(workflow);
    when(workflow.getState()).thenReturn(WorkflowStatus.ACTIVE.getStatus());
    when(workflow.getUpdatedAt()).thenReturn(workflowUpdated);

    when(gitHubService.getLatestWorkflowRun(any(), any())).thenReturn(run);
    when(gitHubService.getExportTestArtifact(run)).thenReturn(null);

    GithubRepo repo = new GithubRepo();
    repo.setWorkflowInformation(new HashSet<>());

    service.processWorkflowWithFallback(ghRepoMock, repo, WorkFlowType.CI);

    assertEquals(1, repo.getWorkflowInformation().size(), "Should create new workflow info");
    WorkflowInformation info = repo.getWorkflowInformation().iterator().next();
    assertEquals(WorkFlowType.CI, info.getWorkflowType(), "Should have CI workflow type as expected");
    assertEquals(run.getCreatedAt(), info.getLastBuilt(), "Last built should be set from run creation time");
    assertEquals("success", info.getConclusion(), "Conclusion should be set to 'success'");
    assertEquals(URL_EXAMPLE, info.getLastBuiltRunUrl(), "Result should return a build run url");
    assertEquals(WorkflowStatus.ACTIVE.getStatus(), info.getCurrentWorkflowState(),
        "Current workflow state should match mocked state");
    assertNull(info.getDisabledDate(), "Disabled date should be null when workflow is active");
  }

  @Test
  void testUpdateWorkflowInfoUpdatesExistingEntry() throws Exception {
    GHRepository ghRepoMock = mock(GHRepository.class);
    GHWorkflowRun run = mock(GHWorkflowRun.class);
    GHWorkflow workflow = mock(GHWorkflow.class);

    when(run.getConclusion()).thenReturn(GHWorkflowRun.Conclusion.SUCCESS);
    when(run.getHtmlUrl()).thenReturn(URI.create(URL_EXAMPLE).toURL());
    Date workflowUpdated = new Date();

    when(gitHubService.getLatestWorkflowRun(any(), any())).thenReturn(run);
    when(gitHubService.getExportTestArtifact(run)).thenReturn(null);

    when(ghRepoMock.getWorkflow(any())).thenReturn(workflow);
    when(workflow.getState()).thenReturn(WorkflowStatus.DISABLED_INACTIVITY.getStatus());
    when(workflow.getUpdatedAt()).thenReturn(workflowUpdated);

    GithubRepo repo = new GithubRepo();
    WorkflowInformation existing = new WorkflowInformation();
    existing.setWorkflowType(WorkFlowType.CI);
    repo.setWorkflowInformation(new HashSet<>(List.of(existing)));

    service.processWorkflowWithFallback(ghRepoMock, repo, WorkFlowType.CI);

    assertEquals(1, repo.getWorkflowInformation().size(), "Should not create a duplicate");
    WorkflowInformation info = repo.getWorkflowInformation().iterator().next();
    assertEquals("success", info.getConclusion(), "Conclusion should be 'success'");
    assertEquals(URL_EXAMPLE, info.getLastBuiltRunUrl(), "Result should return a build run url");
    assertEquals(WorkflowStatus.DISABLED_INACTIVITY.getStatus(), info.getCurrentWorkflowState(),
        "Current workflow state should match mocked state");
    assertEquals(workflowUpdated, info.getDisabledDate(), "Disabled date should be set when workflow is not active");
  }

  @Test
  void testLoadAndStoreTestRepostsForOneProductProductNotFound() {
    when(productRepository.findById("p1")).thenReturn(java.util.Optional.empty());

    service.loadAndStoreTestReportsForOneProduct("p1");

    verify(productRepository).findById("p1");
    verifyNoMoreInteractions(productRepository, githubRepoRepository, testStepsService, gitHubService);
  }

  @Test
  void testLoadAndStoreTestRepostsForOneProductProductFound() throws Exception {
    Product product = new Product();
    product.setId("p1");
    product.setRepositoryName("repoName");
    when(productRepository.findById("p1")).thenReturn(java.util.Optional.of(product));

    when(gitHubService.getRepository("repoName")).thenReturn(ghRepo);

    service.loadAndStoreTestReportsForOneProduct("p1");

    verify(productRepository).findById("p1");
    verify(gitHubService).getRepository("repoName");
    verify(githubRepoRepository).save(any());
  }

  @Test
  void testLoadAndStoreTestRepostsForOneProductHandlesException() throws Exception {
    Product product = new Product();
    product.setId("p1");
    product.setRepositoryName("repoName");
    when(productRepository.findById("p1")).thenReturn(java.util.Optional.of(product));
    when(gitHubService.getRepository("repoName")).thenThrow(new DataAccessException("DB Error") {
    });

    assertDoesNotThrow(() -> service.loadAndStoreTestReportsForOneProduct("p1"),
        "Should not throw an exception when GitHubService.getRepository throws DataAccessException");
    verify(gitHubService).getRepository("repoName");
  }
}