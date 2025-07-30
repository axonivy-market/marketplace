package com.axonivy.market.service.impl;

import com.axonivy.market.assembler.GithubReposModelAssembler;
import com.axonivy.market.entity.GithubRepo;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.TestStep;
import com.axonivy.market.enums.WorkFlowType;
import com.axonivy.market.github.service.GitHubService;
import com.axonivy.market.model.GithubReposModel;
import com.axonivy.market.model.RepoFocusedUpdateModel;
import com.axonivy.market.repository.GithubRepoRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.TestStepsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kohsuke.github.GHArtifact;
import org.kohsuke.github.GHRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GithubReposServiceImplTest {

  String badgeUrl;
  @InjectMocks
  GithubReposServiceImpl service;

  @Mock
  GithubRepoRepository githubRepoRepository;
  GithubReposServiceImpl serviceSpy;
  @Mock
  private GithubReposModelAssembler githubReposModelAssembler;
  @Mock
  private TestStepsService testStepsService;
  @Mock
  private GitHubService gitHubService;
  @Mock
  private ProductRepository productRepository;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    serviceSpy = spy(service);
    badgeUrl = "https://github.com/test/demo";
  }

  @Test
  void testProcessProductExistingRepo() throws Exception {
    GHRepository ghRepo = mock(GHRepository.class);
    when(ghRepo.getName()).thenReturn("demo");
    when(ghRepo.getHtmlUrl()).thenReturn(new URI
        (badgeUrl).toURL());
    when(ghRepo.getLanguage()).thenReturn("Java");
    when(ghRepo.getUpdatedAt()).thenReturn(new Date());

    GithubRepo existing = GithubRepo.builder()
        .name("demo")
        .htmlUrl("https://old-url")
        .testSteps(new ArrayList<>())
        .build();
    when(githubRepoRepository.findByName("demo")).thenReturn(Optional.of(existing));

    doReturn(List.of(new TestStep())).when(serviceSpy)
        .processWorkflowWithFallback(any(), any(), anyString(), any());

    assertDoesNotThrow(() -> serviceSpy.processProduct(ghRepo),
        "Processing product should not throw an exception");

    verify(githubRepoRepository).save(any());
  }

  @Test
  void testProcessProductNewRepo() throws Exception {
    GHRepository ghRepo = mock(GHRepository.class);
    when(ghRepo.getName()).thenReturn("demo");
    when(ghRepo.getHtmlUrl()).thenReturn(new URI(badgeUrl).toURL());
    when(ghRepo.getLanguage()).thenReturn("Java");
    when(ghRepo.getUpdatedAt()).thenReturn(new Date());
    when(ghRepo.getFullName()).thenReturn("owner/demo");

    when(githubRepoRepository.findByName("demo")).thenReturn(Optional.empty());

    doReturn(List.of(new TestStep())).when(serviceSpy)
        .processWorkflowWithFallback(any(), any(), anyString(), any());

    assertDoesNotThrow(() -> serviceSpy.processProduct(ghRepo),
        "Processing product should not throw an exception");

    verify(githubRepoRepository).save(any());
  }

  @Test
  void testProcessProductDataAccessException() throws IOException, URISyntaxException {
    GHRepository ghRepo = mock(GHRepository.class);
    when(ghRepo.getName()).thenReturn("demo");
    when(ghRepo.getHtmlUrl()).thenReturn(new URI(badgeUrl).toURL());
    when(ghRepo.getLanguage()).thenReturn("Java");
    when(ghRepo.getUpdatedAt()).thenReturn(new Date());
    when(ghRepo.getFullName()).thenReturn("owner/demo");

    when(githubRepoRepository.findByName("demo")).thenReturn(Optional.empty());

    doReturn(List.of(new TestStep())).when(serviceSpy)
        .processWorkflowWithFallback(any(), any(), anyString(), any());

    doThrow(new DataAccessException("DB error") {
    }).when(githubRepoRepository).save(any());

    assertDoesNotThrow(() -> serviceSpy.processProduct(ghRepo),
        "Processing product should not throw an exception even if saving fails");
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
    GHRepository ghRepo = mock(GHRepository.class);
    GithubRepo dbRepo = new GithubRepo();
    GHWorkflowRun run = mock(GHWorkflowRun.class);
    GHArtifact artifact = mock(GHArtifact.class);

    when(gitHubService.getLatestWorkflowRun(ghRepo, "ci.yml")).thenReturn(run);
    when(gitHubService.getExportTestArtifact(run)).thenReturn(artifact);
    when(gitHubService.downloadArtifactZip(artifact)).thenReturn(getZipWithTestReport());

    when(testStepsService.createTestSteps(any(), eq(WorkFlowType.CI)))
        .thenReturn(List.of(new TestStep()));

    List<TestStep> steps = service.processWorkflowWithFallback(ghRepo, dbRepo, "ci.yml", WorkFlowType.CI);

    assertEquals(1, steps.size(),
        "Should return one test step when workflow run and artifact are found");
  }

  @Test
  void testProcessWorkflowWithFallbackNoRun() throws Exception {
    GHRepository ghRepo = mock(GHRepository.class);
    GithubRepo dbRepo = new GithubRepo();

    when(gitHubService.getLatestWorkflowRun(any(), any())).thenReturn(null);
    List<TestStep> result = service.processWorkflowWithFallback(ghRepo, dbRepo, "ci.yml", WorkFlowType.CI);

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
  void testFetchAllRepositories() {
    GithubRepo repo = new GithubRepo();
    repo.setFocusedRepo(true);
    when(githubRepoRepository.findAll()).thenReturn(List.of(repo));
    GithubReposModel model = new GithubReposModel();
    when(githubReposModelAssembler.toModel(repo)).thenReturn(model);

    var result = service.fetchAllRepositories();

    assertNotNull(result, "Result should not be null");
    assertEquals(1, result.getFocusedRepos().size(), "Should return one focused repo");
    assertEquals(model, result.getFocusedRepos().get(0), "Focused repo should match model");
    assertEquals(0, result.getStandardRepos().size(), "Should return zero standard repos");
    verify(githubReposModelAssembler).toModel(repo);
  }

  @Test
  void testUpdateFocusedRepo() {
    GithubRepo repo1 = new GithubRepo();
    repo1.setName("repo1");
    repo1.setFocusedRepo(false);

    GithubRepo repo2 = new GithubRepo();
    repo2.setName("repo2");
    repo2.setFocusedRepo(true);

    List<GithubRepo> allRepos = List.of(repo1, repo2);
    when(githubRepoRepository.findAll()).thenReturn(allRepos);

    RepoFocusedUpdateModel updates = new RepoFocusedUpdateModel();
    updates.setRepoNames(List.of("repo1"));

    service.updateFocusedRepo(updates);

    assertTrue(repo1.isFocusedRepo(), "repo1 should be focused repo");
    assertFalse(repo2.isFocusedRepo(), "repo2 should not be focused repo");
    verify(githubRepoRepository).saveAll(allRepos);
  }
}
