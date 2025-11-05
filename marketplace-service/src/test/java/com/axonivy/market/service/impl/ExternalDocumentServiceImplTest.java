package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.DocumentLanguage;
import com.axonivy.market.factory.VersionFactory;
import com.axonivy.market.repository.ArtifactRepository;
import com.axonivy.market.repository.ExternalDocumentMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.rest.axonivy.AxonIvyClient;
import com.axonivy.market.service.FileDownloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@TestPropertySource("classpath:application-test.properties")
@SpringBootTest
class ExternalDocumentServiceImplTest extends BaseSetup {
  private static final String TEST_VERSION_12_5 = "12.5";
  private static final String VERSION_INVALID = "99.0";
  private static final String NON_EXISTENT_PRODUCT = "nonexistent";

  private static final String RELATIVE_WORKING_LOCATION = "/market-cache/portal/10.0.0/doc";
  private static final String RELATIVE_DOC_LOCATION = RELATIVE_WORKING_LOCATION + "/index.html";
  private static final String PORTAL = "portal";
  private static final String ARTIFACT_NAME = "portal-guide";
  private static final String TEST_VERSION = "12.0";
  private static final String DEV_VERSION = "dev";
  private static final String LATEST_VERSION = "latest";
  private static final String EN_LANG = "en";
  private static final String DOC_DIR = "doc";
  private static final String BASE_PATH = CommonConstants.SLASH + PORTAL + CommonConstants.SLASH + ARTIFACT_NAME + CommonConstants.SLASH;

  private final List<String> majorVersions = List.of(TEST_VERSION, TEST_VERSION, "13.1", DEV_VERSION);

  @MockBean
  ProductRepository productRepository;

  @MockBean
  ExternalDocumentMetaRepository externalDocumentMetaRepository;

  @MockBean
  FileDownloadService fileDownloadService;

  @MockBean
  AxonIvyClient axonIvyClient;

  @MockBean
  ArtifactRepository artifactRepository;

  @SpyBean
  ExternalDocumentServiceImpl service;

  @TempDir
  Path tempDir;
  @BeforeEach
  void setup() {
    when(axonIvyClient.getDocumentVersions()).thenReturn(majorVersions);
    service.init();
  }

  @Test
  void testSyncDocumentForNonProduct() {
    when(productRepository.findProductByIdAndRelatedData(null)).thenReturn(null);
    service.syncDocumentForProduct(null, false, null);
    verify(productRepository, times(1)).findProductByIdAndRelatedData(any());
  }

  @Test
  void testSkipSyncDocDueToDevelopmentMode() throws IOException {
    prepareProductDataForSyncTest();
    doReturn(false).when(service).shouldDownloadDocAndUnzipToShareFolder();

    service.syncDocumentForProduct(PORTAL, true, MOCK_RELEASED_VERSION);
    verify(productRepository, times(1)).findProductByIdAndRelatedData(any());
    verify(externalDocumentMetaRepository, times(0)).save(any());
  }

  @Test
  void testSyncDocumentForProduct() throws IOException {
    when(productRepository.findProductByIdAndRelatedData(PORTAL)).thenReturn(mockPortalProductHasNoArtifact().get());
    service.syncDocumentForProduct(PORTAL, true, null);
    verify(productRepository, times(1)).findProductByIdAndRelatedData(any());
    when(artifactRepository.findAllByIdInAndFetchArchivedArtifacts(any())).thenReturn(
        mockPortalProduct().get().getArtifacts());
    when(productRepository.findProductByIdAndRelatedData(PORTAL)).thenReturn(mockPortalProduct().get());
    service.syncDocumentForProduct(PORTAL, false, null);
    verify(externalDocumentMetaRepository, times(1)).findByProductIdAndVersionIn(any(), any());
    when(fileDownloadService.downloadAndUnzipFile(any(), any())).thenReturn("data" + RELATIVE_DOC_LOCATION);
    when(fileDownloadService.generateCacheStorageDirectory(any())).thenReturn("data" + RELATIVE_WORKING_LOCATION);
    doReturn(true).when(service).doesDocExistInShareFolder(anyString());
    service.syncDocumentForProduct(PORTAL, true, null);
    verify(externalDocumentMetaRepository, times(3)).save(any());

    when(artifactRepository.findAllByIdInAndFetchArchivedArtifacts(any())).thenReturn(
        mockPortalProduct().get().getArtifacts());
    when(productRepository.findProductByIdAndRelatedData(PORTAL)).thenReturn(mockPortalProduct().get());
    when(externalDocumentMetaRepository.findByProductIdAndVersionIn(any(), any())).thenReturn(
        List.of(createExternalDocumentMock()));
    service.syncDocumentForProduct(PORTAL, false, null);
    verify(externalDocumentMetaRepository, times(2)).findByProductIdAndVersionIn(any(), any());
  }

  @Test
  void testSyncDocumentForProductButCannotExtractToShareFolder() throws IOException {
    prepareProductDataForSyncTest();
    service.syncDocumentForProduct(PORTAL, true, MOCK_RELEASED_VERSION);
    verify(productRepository, times(1)).findProductByIdAndRelatedData(any());
    verify(externalDocumentMetaRepository, times(2)).save(any());
  }

  private void prepareProductDataForSyncTest() throws IOException {
    when(artifactRepository.findAllByIdInAndFetchArchivedArtifacts(any()))
        .thenReturn(mockPortalProduct().map(Product::getArtifacts).orElse(null));
    when(productRepository.findProductByIdAndRelatedData(PORTAL)).thenReturn(mockPortalProduct().orElse(null));
    when(externalDocumentMetaRepository.findByProductIdAndVersionIn(any(), any()))
        .thenReturn(List.of(createExternalDocumentMock()));
    when(fileDownloadService.downloadAndUnzipFile(any(), any())).thenReturn("data" + RELATIVE_DOC_LOCATION);
  }

  @Test
  void testSyncDocumentForProductIdAndVersion() throws IOException {
    prepareProductDataForSyncTest();
    doReturn(true).when(service).doesDocExistInShareFolder(anyString());
    service.syncDocumentForProduct(PORTAL, true, MOCK_RELEASED_VERSION);
    verify(productRepository, times(1)).findProductByIdAndRelatedData(any());
    verify(externalDocumentMetaRepository, times(2)).save(any());
  }

  @Test
  void testFindAllProductsHaveDocument() {
    var result = service.findAllProductsHaveDocument();
    verify(productRepository, times(1)).findAllProductsHaveDocument();
    assertTrue(result.isEmpty(), "Expected the result to be empty when repository returns nothing");

    when(productRepository.findAllProductsHaveDocument()).thenReturn(List.of(mockPortalProduct().get()));
    result = service.findAllProductsHaveDocument();
    assertNotNull(result, "Expected the result not to be null when repository returns a product");
    assertEquals(PORTAL, result.get(0).getId(), "Expected the first product ID to be PORTAL");
  }

  @Test
  void testFindBestMatchVersion() {
    String productId = "product1";
    String version = "3.0.5";

    when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
    when(externalDocumentMetaRepository.findByProductId(productId)).thenReturn(List.of(
        ExternalDocumentMeta.builder().version("3.0.0").build(),
        ExternalDocumentMeta.builder().version("3.1.0").build()));

    try (MockedStatic<VersionFactory> mockedVersionFactory = mockStatic(VersionFactory.class)) {
      mockedVersionFactory.when(() -> VersionFactory.get(any(), eq(version)))
          .thenReturn("3.0.0");

      String result = service.findBestMatchVersion(productId, version);

      assertEquals("3.0.0", result, "Should return the matched version");
    }
  }

  @Test
  void testFindExternalDocumentURI() {
  var mockVersion = TEST_VERSION;
    var mockProductDocumentMeta = new ExternalDocumentMeta();
  when(productRepository.findById(PORTAL)).thenReturn(mockPortalProduct());
    var result = service.findExternalDocument(PORTAL, mockVersion);
    verify(productRepository, times(1)).findById(any());
    assertNull(result, "Expected result to be null when no matching document meta exists");

  mockProductDocumentMeta.setProductId(PORTAL);
  mockProductDocumentMeta.setVersion(mockVersion);
  mockProductDocumentMeta.setRelativeLink(RELATIVE_DOC_LOCATION);
    when(externalDocumentMetaRepository.findByProductIdAndLanguage(PORTAL, DocumentLanguage.ENGLISH))
        .thenReturn(List.of(mockProductDocumentMeta));
    result = service.findExternalDocument(PORTAL, mockVersion);
    assertNotNull(result, "Expected result not to be null when matching document meta exists");
    assertTrue(result.getRelativeLink().contains("/index.html"),
        "Expected the relative link to contain '/index.html'");
  }

  private Optional<Product> mockPortalProduct() {
    var product = mockPortal(false);
    return Optional.of(product);
  }

  private Optional<Product> mockPortalProductHasNoArtifact() {
    var product = mockPortal(true);
    return Optional.of(product);
  }

  private static Product mockPortal(boolean isEmptyArtifacts) {
    return Product.builder().id(PORTAL)
        .artifacts(isEmptyArtifacts ? List.of() : List.of(mockPortalMavenArtifact()))
        .releasedVersions(List.of("8.0.0", "10.0.0"))
        .build();
  }

  private static Artifact mockPortalMavenArtifact() {
    return Artifact.builder().artifactId("portal-guide").doc(true).groupId(PORTAL)
        .name("Portal Guide").type("zip").build();
  }

  @Test
  void testFindDocVersionsAndLanguagesSuccess() {
    ExternalDocumentMeta enMeta = ExternalDocumentMeta.builder()
        .productId(PORTAL)
        .version(TEST_VERSION)
        .language(DocumentLanguage.ENGLISH)
        .relativeLink("docs/portal/12/en")
        .build();

    ExternalDocumentMeta jaMeta = ExternalDocumentMeta.builder()
        .productId(PORTAL)
        .version(TEST_VERSION)
        .language(DocumentLanguage.JAPANESE)
        .relativeLink("docs/portal/12/ja")
        .build();

    when(externalDocumentMetaRepository.findByArtifactNameAndVersionIn(PORTAL, majorVersions))
        .thenReturn(List.of(enMeta, jaMeta));
    when(externalDocumentMetaRepository.findByArtifactNameAndVersionIn(PORTAL,
        Collections.singletonList(TEST_VERSION)))
        .thenReturn(List.of(enMeta, jaMeta));

    String host = "http://localhost:8080";
    var result = service.findDocVersionsAndLanguages(PORTAL, TEST_VERSION,
        DocumentLanguage.ENGLISH.getCode(), host);

    assertNotNull(result, "Result should not be null");
    assertEquals(1, result.getVersions().size(), "Should have one version");
    assertEquals(2, result.getLanguages().size(), "Should have two languages");
  }

  @Test
  void testGetRelativePathWithLanguage_ValidLanguageDirectories() throws IOException {
    Path testDir = tempDir.resolve("test-doc");
    Files.createDirectories(testDir);
    Files.createDirectory(testDir.resolve("en"));
    Files.createDirectory(testDir.resolve("ja"));
    Files.createDirectory(testDir.resolve("other"));

    Map<DocumentLanguage, String> result = service.getRelativePathWithLanguage(testDir.toString());

    assertEquals(2, result.size(), "Should have 2 languages");
    assertTrue(result.containsKey(DocumentLanguage.ENGLISH));
    assertTrue(result.containsKey(DocumentLanguage.JAPANESE));
  }

  @Test
  void testGetRelativePathWithLanguage_NonDirectory() throws IOException {
    Path testFile = tempDir.resolve("test-file.txt");
    Files.createFile(testFile);

    Map<DocumentLanguage, String> result = service.getRelativePathWithLanguage(testFile.toString());

    assertTrue(result.isEmpty(), "Should return empty map for non-directory");
  }

  @Test
  void testGetRelativePathWithLanguage_EmptyDirectory() throws IOException {
    Path testDir = tempDir.resolve("empty-dir");
    Files.createDirectories(testDir);

    Map<DocumentLanguage, String> result = service.getRelativePathWithLanguage(testDir.toString());

    assertTrue(result.isEmpty(), "Should return empty map for directory without language subdirs");
  }

  @Test
  void testDoesDocExistInShareFolder_ExistsWithFiles() throws IOException {
    Path testDir = tempDir.resolve("doc-folder");
    Files.createDirectories(testDir);
    Files.createFile(testDir.resolve(CommonConstants.INDEX_HTML));

    boolean result = service.doesDocExistInShareFolder(testDir.toString());

    assertTrue(result, "Should return true for existing folder with files");
  }

  @Test
  void testDoesDocExistInShareFolder_EmptyFolder() throws IOException {
    Path testDir = tempDir.resolve("empty-folder");
    Files.createDirectories(testDir);

    boolean result = service.doesDocExistInShareFolder(testDir.toString());

    assertFalse(result, "Should return false for empty folder");
  }

  @Test
  void testDoesDocExistInShareFolder_NonExistent() {
    String nonExistentPath = tempDir.resolve("nonexistent").toString();

    boolean result = service.doesDocExistInShareFolder(nonExistentPath);

    assertFalse(result, "Should return false for non-existent folder");
  }

  @Test
  void testResolveBestMatchRedirectUrl_DevVersion() {
    String path = BASE_PATH + DEV_VERSION + CommonConstants.SLASH + DOC_DIR + CommonConstants.SLASH + EN_LANG + "/index.html";
    String result = service.resolveBestMatchRedirectUrl(path);
    assertNotNull(result, "Should return valid path for dev version");
    assertTrue(result.contains(DEV_VERSION), "Should contain dev in path");
  }

  @Test
  void testResolveBestMatchRedirectUrl_LatestVersion() {
    String path = BASE_PATH + LATEST_VERSION + CommonConstants.SLASH + DOC_DIR + CommonConstants.SLASH + EN_LANG + "/index.html";
    String result = service.resolveBestMatchRedirectUrl(path);
    assertNotNull(result, "Should return valid path for latest version");
    assertTrue(result.contains(LATEST_VERSION), "Should contain latest in path");
  }

  @Test
  void testResolveBestMatchRedirectUrl_MissingProductName() {
    String path = "/market-cache//" + ARTIFACT_NAME + "/10.0/" + EN_LANG + "/index.html";
    String result = service.resolveBestMatchRedirectUrl(path);
    assertNull(result, "Should return null for missing product name");
  }

  @Test
  void testResolveBestMatchRedirectUrl_MissingArtifactName() {
    String path = "/market-cache/" + PORTAL + "//10.0/" + EN_LANG + "/index.html";
    String result = service.resolveBestMatchRedirectUrl(path);
    assertNull(result, "Should return null for missing artifact name");
  }

  @Test
  void testResolveBestMatchRedirectUrl_WithBestMatchVersion() {
  String path = BASE_PATH + TEST_VERSION_12_5 + CommonConstants.SLASH + DOC_DIR + CommonConstants.SLASH + EN_LANG + "/index.html";
    when(productRepository.findById(PORTAL)).thenReturn(Optional.of(new Product()));
  ExternalDocumentMeta meta = ExternalDocumentMeta.builder()
    .version(TEST_VERSION)
    .productId(PORTAL)
    .artifactName(ARTIFACT_NAME)
    .build();
    when(externalDocumentMetaRepository.findByProductId(PORTAL))
        .thenReturn(List.of(meta));

  try (MockedStatic<VersionFactory> mockedVersionFactory = mockStatic(VersionFactory.class)) {
    mockedVersionFactory.when(() -> VersionFactory.get(any(), eq(TEST_VERSION_12_5)))
      .thenReturn(null);
    mockedVersionFactory.when(() -> VersionFactory.get(any(List.class), eq(TEST_VERSION_12_5)))
      .thenReturn(TEST_VERSION);

      String result = service.resolveBestMatchRedirectUrl(path);

      assertNotNull(result, "Should return valid path");
  assertTrue(result.contains(TEST_VERSION), "Should contain matched version");
    }
  }

  @Test
  void testCreateSymlinkForMajorVersion_Success() throws IOException {
    if (!FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
      return;
    }

    Path productDir = tempDir.resolve("portal");
    Path versionDir = productDir.resolve("1.0.0");
    Path docDir = versionDir.resolve("doc");
    Files.createDirectories(docDir);

    String result = service.createSymlinkForMajorVersion(docDir, TEST_VERSION);

    assertNotNull(result, "Should return symlink path");
    assertTrue(result.contains(TEST_VERSION), "Should contain major version");
    assertTrue(Files.exists(Paths.get(result)), "Symlink should exist");
  }

  @Test
  void testCreateSymlinkForMajorVersion_InvalidInput() {
    String result = service.createSymlinkForMajorVersion(null, TEST_VERSION);
    assertEquals("", result, "Should return empty string for null path");

    result = service.createSymlinkForMajorVersion(Paths.get("/tmp"), null);
    assertEquals("", result, "Should return empty string for null version");

    result = service.createSymlinkForMajorVersion(Paths.get("/tmp"), "");
    assertEquals("", result, "Should return empty string for empty version");
  }

  @Test
  void testCreateSymlinkForMajorVersion_SymlinkAlreadyExists() throws IOException {
    if (!FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
      return;
    }

    Path productDir = tempDir.resolve("portal");
    Path versionDir = productDir.resolve("1.0.0");
    Path docDir = versionDir.resolve("doc");
    Files.createDirectories(docDir);

    String firstResult = service.createSymlinkForMajorVersion(docDir, TEST_VERSION);
    assertNotNull(firstResult, "Should create symlink first time");

    String secondResult = service.createSymlinkForMajorVersion(docDir, TEST_VERSION);
    assertNotNull(secondResult, "Should return existing symlink path");
    assertEquals(firstResult, secondResult, "Should return same path");
  }

  @Test
  void testCreateSymlinkForMajorVersion_InvalidPathStructure() {
    Path rootPath = Paths.get(CommonConstants.SLASH);
    String result = service.createSymlinkForMajorVersion(rootPath, TEST_VERSION);
    assertEquals("", result, "Should return empty string for root path");
  }

  @Test
  void testFindDocVersionsAndLanguages_EmptyDocMetas() {
    when(externalDocumentMetaRepository.findByArtifactNameAndVersionIn(PORTAL, majorVersions))
        .thenReturn(Collections.emptyList());

    String host = "http://localhost:8080";
    var result = service.findDocVersionsAndLanguages(PORTAL, TEST_VERSION,
        DocumentLanguage.ENGLISH.getCode(), host);

    assertNull(result, "Result should be null when no doc metas found");
  }

  @Test
  void testFindDocVersionsAndLanguages_DevVersionSorting() {
  ExternalDocumentMeta devMeta = ExternalDocumentMeta.builder()
    .productId(PORTAL)
    .version(DEV_VERSION)
    .language(DocumentLanguage.ENGLISH)
    .relativeLink("docs/portal/dev/en")
    .build();

  ExternalDocumentMeta normalMeta = ExternalDocumentMeta.builder()
    .productId(PORTAL)
    .version(TEST_VERSION)
    .language(DocumentLanguage.ENGLISH)
    .relativeLink("docs/portal/10/en")
    .build();

  when(externalDocumentMetaRepository.findByArtifactNameAndVersionIn(PORTAL, majorVersions))
    .thenReturn(List.of(devMeta, normalMeta));
  when(externalDocumentMetaRepository.findByArtifactNameAndVersionIn(PORTAL,
    Collections.singletonList(DEV_VERSION)))
    .thenReturn(List.of(devMeta));

    String host = "http://localhost:8080";
  var result = service.findDocVersionsAndLanguages(PORTAL, DEV_VERSION,
    DocumentLanguage.ENGLISH.getCode(), host);

    assertNotNull(result, "Result should not be null");
    assertEquals(2, result.getVersions().size(), "Should have two versions");
    // Dev version should be sorted last
  assertEquals(DEV_VERSION, result.getVersions().get(1).getVersion(), "Dev should be last");
  }

  @Test
  void testFindDocVersionsAndLanguages_LanguageFallback() {
  ExternalDocumentMeta enMeta = ExternalDocumentMeta.builder()
    .productId(PORTAL)
    .version(TEST_VERSION)
    .language(DocumentLanguage.ENGLISH)
    .relativeLink("docs/portal/12/en")
    .build();

    when(externalDocumentMetaRepository.findByArtifactNameAndVersionIn(PORTAL, majorVersions))
        .thenReturn(List.of(enMeta));
    when(externalDocumentMetaRepository.findByArtifactNameAndVersionIn(PORTAL,
        Collections.singletonList(TEST_VERSION)))
        .thenReturn(List.of(enMeta));

    String host = "http://localhost:8080";
    var result = service.findDocVersionsAndLanguages(PORTAL, TEST_VERSION,
        DocumentLanguage.JAPANESE.getCode(), host);

    assertNotNull(result, "Result should not be null");
    assertEquals(1, result.getVersions().size(), "Should have one version");
    assertTrue(result.getVersions().get(0).getUrl().contains("/en"),
        "Should fallback to English");
  }

  @Test
  void testFindExternalDocument_EmptyProduct() {
  when(productRepository.findById(NON_EXISTENT_PRODUCT)).thenReturn(Optional.empty());

  var result = service.findExternalDocument(NON_EXISTENT_PRODUCT, TEST_VERSION);

    assertNull(result, "Should return null for non-existent product");
    verify(externalDocumentMetaRepository, never()).findByProductIdAndLanguage(any(), any());
  }

  @Test
  void testFindExternalDocument_NoMatchingVersion() {
    when(productRepository.findById(PORTAL)).thenReturn(Optional.of(new Product()));

    ExternalDocumentMeta meta = ExternalDocumentMeta.builder()
        .productId(PORTAL)
        .version("10.0")
        .language(DocumentLanguage.ENGLISH)
        .relativeLink("docs/portal/10/en")
        .build();

    when(externalDocumentMetaRepository.findByProductIdAndLanguage(PORTAL, DocumentLanguage.ENGLISH))
        .thenReturn(List.of(meta));

    try (MockedStatic<VersionFactory> mockedVersionFactory = mockStatic(VersionFactory.class)) {
      mockedVersionFactory.when(() -> VersionFactory.getBestMatchMajorVersion(any(), eq(VERSION_INVALID), any()))
          .thenReturn(null);

      var result = service.findExternalDocument(PORTAL, VERSION_INVALID);

      assertNull(result, "Should return null when no matching version found");
    }
  }

  @Test
  void testResolveBestMatchSymlinkVersion_BlankBestMatchVersion() {
  try (MockedStatic<VersionFactory> mockedVersionFactory = mockStatic(VersionFactory.class)) {
    mockedVersionFactory.when(() -> VersionFactory.get(majorVersions, VERSION_INVALID))
      .thenReturn("");

    String result = service.resolveBestMatchSymlinkVersion(PORTAL, ARTIFACT_NAME, VERSION_INVALID,
      DocumentLanguage.ENGLISH);

    assertEquals("", result, "Should return empty string when bestMatchVersion is blank");
  }
  }

  @Test
  void testResolveBestMatchSymlinkVersion_NullBestMatchVersion() {
  try (MockedStatic<VersionFactory> mockedVersionFactory = mockStatic(VersionFactory.class)) {
    mockedVersionFactory.when(() -> VersionFactory.get(majorVersions, VERSION_INVALID))
      .thenReturn(null);

    String result = service.resolveBestMatchSymlinkVersion(PORTAL, ARTIFACT_NAME, VERSION_INVALID,
      DocumentLanguage.ENGLISH);

    assertEquals("", result, "Should return empty string when bestMatchVersion is null");
  }
  }

  @Test
  void testResolveBestMatchSymlinkVersion_Success() {
  try (MockedStatic<VersionFactory> mockedVersionFactory = mockStatic(VersionFactory.class)) {
    mockedVersionFactory.when(() -> VersionFactory.get(majorVersions, TEST_VERSION_12_5))
      .thenReturn(TEST_VERSION);

    String result = service.resolveBestMatchSymlinkVersion(PORTAL, ARTIFACT_NAME, TEST_VERSION_12_5,
      DocumentLanguage.ENGLISH);

    assertNotNull(result, "Should return symlink path");
    assertTrue(result.contains(PORTAL), "Should contain product name");
    assertTrue(result.contains(TEST_VERSION), "Should contain matched version");
  }
  }

  @Test
  void testResolveBestMatchSymlinkVersion_MissingParameters() {
  String result = service.resolveBestMatchSymlinkVersion(null, ARTIFACT_NAME, TEST_VERSION,
    DocumentLanguage.ENGLISH);
  assertEquals("", result, "Should return empty string for null productName");

  result = service.resolveBestMatchSymlinkVersion(PORTAL, null, TEST_VERSION,
    DocumentLanguage.ENGLISH);
  assertEquals("", result, "Should return empty string for null artifactName");

  result = service.resolveBestMatchSymlinkVersion(PORTAL, ARTIFACT_NAME, null,
    DocumentLanguage.ENGLISH);
  assertEquals("", result, "Should return empty string for null version");

  result = service.resolveBestMatchSymlinkVersion("", ARTIFACT_NAME, TEST_VERSION,
    DocumentLanguage.ENGLISH);
  assertEquals("", result, "Should return empty string for empty productName");
  }

  @Test
  void testFindBestMatchVersion_ProductNotFound() {
  String productId = NON_EXISTENT_PRODUCT;
  String version = "3.0.0";

  when(productRepository.findById(productId)).thenReturn(Optional.empty());

  String result = service.findBestMatchVersion(productId, version);

  assertNull(result, "Should return null when product not found");
  verify(productRepository).findById(productId);
  verify(externalDocumentMetaRepository, never()).findByProductId(any());
  }

  @Test
  void testResolveBestMatchRedirectUrl_InvalidPath() {
    String path = "/invalid/path";
    String result = service.resolveBestMatchRedirectUrl(path);
    assertNull(result, "Should return null for invalid path");

    path = "/market-cache";
    result = service.resolveBestMatchRedirectUrl(path);
    assertNull(result, "Should return null for incomplete path");

    path = "/market-cache/portal";
    result = service.resolveBestMatchRedirectUrl(path);
    assertNull(result, "Should return null for path without version");
  }
}