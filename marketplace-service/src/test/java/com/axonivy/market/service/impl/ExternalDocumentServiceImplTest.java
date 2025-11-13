package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.DirectoryConstants;
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
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
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
  private static final String RELATIVE_WORKING_LOCATION_EN = "/market-cache/portal/12.5.0/doc/en";
  private static final String INDEX_FILE = "/index.html";
  private static final String RELATIVE_DOC_LOCATION = RELATIVE_WORKING_LOCATION + INDEX_FILE;
  private static final String RELATIVE_DOC_LOCATION_EN = RELATIVE_WORKING_LOCATION_EN + INDEX_FILE;

  private static final String PORTAL = "portal";

  private static final String ARTIFACT_NAME = "portal-guide";
  private static final String TEST_VERSION = "12.0";
  private static final String DEV_VERSION = "dev";
  private static final String LATEST_VERSION = "latest";
  private static final String DOC_DIR = "doc";
  private static final String BASE_PATH =
      CommonConstants.SLASH + PORTAL + CommonConstants.SLASH + ARTIFACT_NAME + CommonConstants.SLASH;
  private static final Path PATH_TMP = Paths.get("/tmp");
  private static final Path CACHE_ROOT_PATH = Paths.get(DirectoryConstants.DATA_CACHE_DIR).toAbsolutePath().normalize();
  private static final Product EMPTY_PRODUCT = new Product();
  private static final String HOST = "http://localhost:8080";

  private final List<String> majorVersions = List.of(TEST_VERSION_12_5, TEST_VERSION, "13.1", DEV_VERSION);

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
    verify(productRepository, times(0)).findProductByIdAndRelatedData(any());
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
    when(productRepository.findProductByIdAndRelatedData(PORTAL)).thenReturn(mockPortalProductHasNoArtifact().orElse(null));
    service.syncDocumentForProduct(PORTAL, true, null);
    verify(productRepository, times(1)).findProductByIdAndRelatedData(any());
    when(artifactRepository.findAllByIdInAndFetchArchivedArtifacts(any())).thenReturn(
        mockPortalProduct().orElse(new Product()).getArtifacts());
    when(productRepository.findProductByIdAndRelatedData(PORTAL)).thenReturn(mockPortalProduct().orElse(null));
    service.syncDocumentForProduct(PORTAL, false, null);
    verify(externalDocumentMetaRepository, times(1)).findByProductIdAndVersionIn(any(), any());
    when(fileDownloadService.downloadAndUnzipFile(any(), any())).thenReturn(DirectoryConstants.DATA_DIR + RELATIVE_DOC_LOCATION);
    when(fileDownloadService.generateCacheStorageDirectory(any())).thenReturn(DirectoryConstants.DATA_DIR + RELATIVE_WORKING_LOCATION);
    doReturn(true).when(service).doesDocExistInShareFolder(anyString());
    service.syncDocumentForProduct(PORTAL, true, null);
    verify(externalDocumentMetaRepository, atLeastOnce()).save(any());

    when(artifactRepository.findAllByIdInAndFetchArchivedArtifacts(any())).thenReturn(
        mockPortalProduct().orElse(new Product()).getArtifacts());
    when(productRepository.findProductByIdAndRelatedData(PORTAL)).thenReturn(mockPortalProduct().orElse(null));
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
    verify(externalDocumentMetaRepository, atLeastOnce()).save(any());
  }

  private void prepareProductDataForSyncTest() throws IOException {
    when(artifactRepository.findAllByIdInAndFetchArchivedArtifacts(any()))
        .thenReturn(mockPortalProduct().map(Product::getArtifacts).orElse(null));
    when(productRepository.findProductByIdAndRelatedData(PORTAL)).thenReturn(mockPortalProduct().orElse(null));
    when(externalDocumentMetaRepository.findByProductIdAndVersionIn(any(), any()))
        .thenReturn(List.of(createExternalDocumentMock()));
    when(fileDownloadService.downloadAndUnzipFile(any(), any())).thenReturn(DirectoryConstants.DATA_DIR + RELATIVE_DOC_LOCATION);
  }

  @Test
  void testSyncDocumentForProductIdAndVersion() throws IOException {
    prepareProductDataForSyncTest();
    doReturn(true).when(service).doesDocExistInShareFolder(anyString());
    service.syncDocumentForProduct(PORTAL, true, MOCK_RELEASED_VERSION);
    verify(productRepository, times(1)).findProductByIdAndRelatedData(any());
    verify(externalDocumentMetaRepository, atLeastOnce()).save(any());
  }

  @Test
  void testFindAllProductsHaveDocument() {
    var result = service.findAllProductsHaveDocument();
    verify(productRepository, times(1)).findAllProductsHaveDocument();
    assertTrue(result.isEmpty(), "Expected the result to be empty when repository returns nothing");

    when(productRepository.findAllProductsHaveDocument()).thenReturn(List.of(mockPortalProduct().orElse(new Product())));
    result = service.findAllProductsHaveDocument();
    assertNotNull(result, "Expected the result not to be null when repository returns a product");
    assertFalse(result.isEmpty(), "Expected the result to contain products");
    assertEquals(PORTAL, result.get(0).getId(), "Expected the first product ID to be PORTAL");
  }

  @Test
  void testFindBestMatchVersion() {
    String version = TEST_VERSION_12_5;

    when(productRepository.findById(PORTAL)).thenReturn(Optional.of(EMPTY_PRODUCT));
    when(externalDocumentMetaRepository.findByProductId(PORTAL)).thenReturn(List.of(
        ExternalDocumentMeta.builder().version(TEST_VERSION).build(),
        ExternalDocumentMeta.builder().version(TEST_VERSION_12_5).build()));

    try (MockedStatic<VersionFactory> mockedVersionFactory = mockStatic(VersionFactory.class)) {
      mockedVersionFactory.when(() -> VersionFactory.get(any(), eq(version)))
          .thenReturn(TEST_VERSION);

      String result = service.fallbackFindBestMatchVersion(PORTAL, version);

      assertEquals(TEST_VERSION, result, "Should return the matched version");
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
    assertTrue(result.getRelativeLink().contains(INDEX_FILE),
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
        .releasedVersions(List.of("8.0.0", TEST_VERSION))
        .build();
  }

  private static Artifact mockPortalMavenArtifact() {
    return Artifact.builder().artifactId(ARTIFACT_NAME).doc(true).groupId(PORTAL)
        .name("Portal Guide").type("zip").build();
  }

  @Test
  void testFindDocVersionsAndLanguagesSuccess() {
    ExternalDocumentMeta enMeta = ExternalDocumentMeta.builder()
        .productId(PORTAL)
        .version(TEST_VERSION)
        .language(DocumentLanguage.ENGLISH)
        .relativeLink(RELATIVE_WORKING_LOCATION_EN)
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

    var result = service.findDocVersionsAndLanguages(PORTAL, TEST_VERSION,
        DocumentLanguage.ENGLISH.getCode(), HOST);

    assertNotNull(result, "Result should not be null");
    assertEquals(1, result.getVersions().size(), "Should have one version");
    assertEquals(2, result.getLanguages().size(), "Should have two languages");
  }

  @Test
  void testGetRelativePathWithLanguageValidLanguageDirectories() throws IOException {
    Path testDir = tempDir.resolve(PORTAL);
    Files.createDirectories(testDir);
    Files.createDirectory(testDir.resolve(DocumentLanguage.ENGLISH.getCode()));
    Files.createDirectory(testDir.resolve(DocumentLanguage.JAPANESE.getCode()));
    Files.createDirectory(testDir.resolve(PORTAL));

    Map<DocumentLanguage, String> result = service.getRelativePathWithLanguage(testDir.toString());

    assertEquals(2, result.size(), "Should have 2 languages");
    assertTrue(result.containsKey(DocumentLanguage.ENGLISH), "Should contain English language");
    assertTrue(result.containsKey(DocumentLanguage.JAPANESE), "Should contain Japanese language");
  }

  @Test
  void testGetRelativePathWithLanguageNonDirectory() throws IOException {
    Path testFile = tempDir.resolve(PORTAL);
    Files.createFile(testFile);
    Map<DocumentLanguage, String> result = service.getRelativePathWithLanguage(testFile.toString());

    assertTrue(result.isEmpty(), "Should return empty map for non-directory");
  }

  @Test
  void testGetRelativePathWithLanguageEmptyDirectory() throws IOException {
    Path testDir = tempDir.resolve(PORTAL);
    Files.createDirectories(testDir);
    Map<DocumentLanguage, String> result = service.getRelativePathWithLanguage(testDir.toString());

    assertTrue(result.isEmpty(), "Should return empty map for directory without language sub dirs");
  }

  @Test
  void testDoesDocExistInShareFolderExistsWithFiles() throws IOException {
    Path testDir = tempDir.resolve(PORTAL);
    Files.createDirectories(testDir);
    Files.createFile(testDir.resolve(CommonConstants.INDEX_HTML));
    boolean result = service.doesDocExistInShareFolder(testDir.toString());

    assertTrue(result, "Should return true for existing folder with files");
  }

  @Test
  void testDoesDocExistInShareFolderEmptyFolder() throws IOException {
    Path testDir = tempDir.resolve(PORTAL);
    Files.createDirectories(testDir);
    boolean result = service.doesDocExistInShareFolder(testDir.toString());

    assertFalse(result, "Should return false for empty folder");
  }

  @Test
  void testDoesDocExistInShareFolderNonExistent() {
    String nonExistentPath = tempDir.resolve(NON_EXISTENT_PRODUCT).toString();
    boolean result = service.doesDocExistInShareFolder(nonExistentPath);

    assertFalse(result, "Should return false for non-existent folder");
  }

  @Test
  void testResolveBestMatchRedirectUrlDevVersion() {
    try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
      filesMock.when(() -> Files.exists(any(Path.class), any()))
          .thenReturn(true);

      String path = String.join(CommonConstants.SLASH,
          BASE_PATH + DEV_VERSION, DOC_DIR,
          DocumentLanguage.ENGLISH.getCode() + INDEX_FILE);
      String result = service.resolveBestMatchRedirectUrl(path);

      assertNotNull(result, "Should return valid path for dev version");
      assertTrue(result.contains(DEV_VERSION), "Should contain dev in path");
      assertTrue(result.contains(DirectoryConstants.CACHE_DIR), "Should contain cache directory");
      assertTrue(result.contains(DocumentLanguage.ENGLISH.getCode()), "Should contain language code");
    }
  }

  @Test
  void testResolveBestMatchRedirectUrlLatestVersion() {
    try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
      filesMock.when(() -> Files.exists(any(Path.class), any()))
          .thenReturn(true);

      String path = String.join(CommonConstants.SLASH,
          BASE_PATH + LATEST_VERSION, DOC_DIR,
          DocumentLanguage.ENGLISH.getCode() + INDEX_FILE);
      String result = service.resolveBestMatchRedirectUrl(path);

      assertNotNull(result, "Should return valid path for latest version");
      assertTrue(result.contains(LATEST_VERSION), "Should contain latest in path");
      assertTrue(result.contains(DirectoryConstants.CACHE_DIR), "Should contain cache directory");
      assertTrue(result.contains(DocumentLanguage.ENGLISH.getCode()), "Should contain language code");
    }
  }

  @Test
  void testResolveBestMatchRedirectUrlMissingProductName() {
    String path = "/market-cache//" + ARTIFACT_NAME + "/10.0/" + DocumentLanguage.ENGLISH.getCode() + INDEX_FILE;
    String result = service.resolveBestMatchRedirectUrl(path);
    assertNull(result, "Should return null for missing product name");
  }

  @Test
  void testResolveBestMatchRedirectUrlMissingArtifactName() {
    String path = "/market-cache/" + PORTAL + "//10.0/" + DocumentLanguage.ENGLISH.getCode() + INDEX_FILE;
    String result = service.resolveBestMatchRedirectUrl(path);
    assertNull(result, "Should return null for missing artifact name");
  }

  @Test
  void testCreateSymlinkForMajorVersionSuccess() throws IOException {
    Path productDir = CACHE_ROOT_PATH.resolve(PORTAL);
    Path versionDir = productDir.resolve(TEST_VERSION);
    Path docDir = versionDir.resolve(DOC_DIR);
    Files.createDirectories(docDir);

    try (
        MockedStatic<Files> filesMock = mockStatic(Files.class, CALLS_REAL_METHODS)
    ) {
      filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(false);
      filesMock.when(() -> Files.createSymbolicLink(any(Path.class), any(Path.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));
      String result = service.createSymlinkForMajorVersion(docDir, TEST_VERSION);
      assertNotNull(result, "Should return symlink path");
      assertFalse(result.isEmpty(), "Symlink path should not be empty");
      filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(true);
      assertTrue(Files.exists(Paths.get(result)), "Symlink should exist");
    }
  }

  @Test
  void testCreateSymlinkForMajorVersionInvalidInput() {
    String result = service.createSymlinkForMajorVersion(null, TEST_VERSION);
    assertEquals(StringUtils.EMPTY, result, "Should return empty string for null path");

    result = service.createSymlinkForMajorVersion(PATH_TMP, null);
    assertEquals(StringUtils.EMPTY, result, "Should return empty string for null version");

    result = service.createSymlinkForMajorVersion(PATH_TMP, StringUtils.EMPTY);
    assertEquals(StringUtils.EMPTY, result, "Should return empty string for empty version");
  }

  @Test
  void testCreateSymlinkForMajorVersionSymlinkAlreadyExists() throws IOException {
    Path productDir = tempDir.resolve(PORTAL);
    Path versionDir = productDir.resolve(TEST_VERSION);
    Path docDir = versionDir.resolve(DOC_DIR);
    Files.createDirectories(docDir);

    String firstResult = service.createSymlinkForMajorVersion(docDir, TEST_VERSION);
    assertNotNull(firstResult, "Should create symlink first time");

    String secondResult = service.createSymlinkForMajorVersion(docDir, TEST_VERSION);
    assertNotNull(secondResult, "Should return existing symlink path");
    assertEquals(firstResult, secondResult, "Should return same path");
  }

  @Test
  void testCreateSymlinkForMajorVersionInvalidPathStructure() {
    Path rootPath = Paths.get(CommonConstants.SLASH);
    String result = service.createSymlinkForMajorVersion(rootPath, TEST_VERSION);
    assertEquals(StringUtils.EMPTY, result, "Should return empty string for root path");
  }

  @Test
  void testFindDocVersionsAndLanguagesEmptyDocMetas() {
    when(externalDocumentMetaRepository.findByArtifactNameAndVersionIn(PORTAL, majorVersions))
        .thenReturn(Collections.emptyList());

    var result = service.findDocVersionsAndLanguages(PORTAL, TEST_VERSION,
        DocumentLanguage.ENGLISH.getCode(), HOST);

    assertNull(result, "Result should be null when no doc metas found");
  }

  @Test
  void testFindDocVersionsAndLanguagesDevVersionSorting() {
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
        .relativeLink(RELATIVE_DOC_LOCATION_EN)
        .build();

    when(externalDocumentMetaRepository.findByArtifactNameAndVersionIn(PORTAL, majorVersions))
        .thenReturn(List.of(devMeta, normalMeta));
    when(externalDocumentMetaRepository.findByArtifactNameAndVersionIn(PORTAL,
        Collections.singletonList(DEV_VERSION)))
        .thenReturn(List.of(devMeta));

    var result = service.findDocVersionsAndLanguages(PORTAL, DEV_VERSION,
        DocumentLanguage.ENGLISH.getCode(), HOST);

    assertNotNull(result, "Result should not be null");
    assertEquals(2, result.getVersions().size(), "Should have two versions");
    assertEquals(DEV_VERSION, result.getVersions().get(1).getVersion(), "Dev should be last");
  }

  @Test
  void testFindDocVersionsAndLanguagesLanguageFallback() {
    ExternalDocumentMeta enMeta = ExternalDocumentMeta.builder()
        .productId(PORTAL)
        .version(TEST_VERSION)
        .language(DocumentLanguage.ENGLISH)
        .relativeLink(RELATIVE_DOC_LOCATION_EN)
        .build();

    when(externalDocumentMetaRepository.findByArtifactNameAndVersionIn(PORTAL, majorVersions))
        .thenReturn(List.of(enMeta));
    when(externalDocumentMetaRepository.findByArtifactNameAndVersionIn(PORTAL,
        Collections.singletonList(TEST_VERSION)))
        .thenReturn(List.of(enMeta));

    var result = service.findDocVersionsAndLanguages(PORTAL, TEST_VERSION,
        DocumentLanguage.JAPANESE.getCode(), HOST);

    assertNotNull(result, "Result should not be null");
    assertEquals(1, result.getVersions().size(), "Should have one version");
    assertTrue(result.getVersions().get(0).getUrl().contains(DocumentLanguage.ENGLISH.getCode()),
        "Should fallback to English");
  }

  @Test
  void testFindExternalDocumentEmptyProduct() {
    when(productRepository.findById(NON_EXISTENT_PRODUCT)).thenReturn(Optional.empty());

    var result = service.findExternalDocument(NON_EXISTENT_PRODUCT, TEST_VERSION);

    assertNull(result, "Should return null for non-existent product");
    verify(externalDocumentMetaRepository, never()).findByProductIdAndLanguage(any(), any());
  }

  @Test
  void testFindExternalDocumentNoMatchingVersion() {
    when(productRepository.findById(PORTAL)).thenReturn(Optional.of(EMPTY_PRODUCT));

    ExternalDocumentMeta meta = ExternalDocumentMeta.builder()
        .productId(PORTAL)
        .version(TEST_VERSION)
        .language(DocumentLanguage.ENGLISH)
        .relativeLink(RELATIVE_DOC_LOCATION_EN)
        .build();

    when(externalDocumentMetaRepository.findByProductIdAndLanguage(PORTAL, DocumentLanguage.ENGLISH))
        .thenReturn(List.of(meta));

    try (MockedStatic<VersionFactory> mockedVersionFactory = mockStatic(VersionFactory.class)) {
      mockedVersionFactory.when(() -> VersionFactory.getBestMatchMajorVersion(any(), eq(VERSION_INVALID)))
          .thenReturn(null);

      var result = service.findExternalDocument(PORTAL, VERSION_INVALID);

      assertNull(result, "Should return null when no matching version found");
    }
  }

  @Test
  void testResolveBestMatchSymlinkVersionBlankBestMatchVersion() {
    try (MockedStatic<VersionFactory> mockedVersionFactory = mockStatic(VersionFactory.class)) {
      mockedVersionFactory.when(() -> VersionFactory.get(majorVersions, VERSION_INVALID))
          .thenReturn(StringUtils.EMPTY);

      String result = service.resolveBestMatchSymlinkVersion(VERSION_INVALID);

      assertEquals(StringUtils.EMPTY, result, "Should return empty string when bestMatchVersion is blank");
    }
  }

  @Test
  void testResolveBestMatchSymlinkVersionNullBestMatchVersion() {
    try (MockedStatic<VersionFactory> mockedVersionFactory = mockStatic(VersionFactory.class)) {
      mockedVersionFactory.when(() -> VersionFactory.get(majorVersions, VERSION_INVALID))
          .thenReturn(null);

      String result = service.resolveBestMatchSymlinkVersion(VERSION_INVALID);
      assertEquals(StringUtils.EMPTY, result, "Should return empty string when bestMatchVersion is null");
    }
  }

  @Test
  void testResolveBestMatchSymlinkVersionSuccess() {
    try (MockedStatic<VersionFactory> mockedVersionFactory = mockStatic(VersionFactory.class)) {
      mockedVersionFactory.when(() -> VersionFactory.getBestMatchMajorVersion(majorVersions, TEST_VERSION_12_5))
          .thenReturn(TEST_VERSION);

      String result = service.resolveBestMatchSymlinkVersion(TEST_VERSION_12_5);

      assertNotNull(result, "Should return symlink path");
      assertTrue(result.contains(TEST_VERSION), "Should contain matched version");
    }
  }

  @Test
  void testFindBestMatchVersionProductNotFound() {
    String productId = NON_EXISTENT_PRODUCT;

    when(productRepository.findById(productId)).thenReturn(Optional.empty());
    String result = service.fallbackFindBestMatchVersion(productId, TEST_VERSION);
    assertNull(result, "Should return null when product not found");
    verify(productRepository).findById(productId);
    verify(externalDocumentMetaRepository, never()).findByProductId(any());
  }

  @Test
  void testResolveBestMatchRedirectUrlInvalidPath() {
    String path = "/invalid/path";
    String result = service.resolveBestMatchRedirectUrl(path);
    assertNull(result, "Should return null for invalid path");
  }

  @Test
  void testResolveBestMatchRedirectUrlWithDevVersion() {
    try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
      filesMock.when(() -> Files.exists(any(Path.class), any()))
          .thenReturn(true);

      String devPath = String.join(CommonConstants.SLASH, StringUtils.EMPTY, PORTAL, ARTIFACT_NAME, DEV_VERSION,
          DOC_DIR, CommonConstants.INDEX_HTML);
      String result = service.resolveBestMatchRedirectUrl(devPath);
      String expectedDevResult = String.join(CommonConstants.SLASH, StringUtils.EMPTY, DirectoryConstants.CACHE_DIR,
          PORTAL, ARTIFACT_NAME, DEV_VERSION, DOC_DIR, DocumentLanguage.ENGLISH.getCode(), CommonConstants.INDEX_HTML);
      assertEquals(expectedDevResult, result, "Should handle dev version correctly");

      String latestPath = String.join(CommonConstants.SLASH, StringUtils.EMPTY, PORTAL, ARTIFACT_NAME, LATEST_VERSION,
          DOC_DIR, CommonConstants.INDEX_HTML);
      result = service.resolveBestMatchRedirectUrl(latestPath);
      String expectedLatestResult = String.join(CommonConstants.SLASH, StringUtils.EMPTY, DirectoryConstants.CACHE_DIR,
          PORTAL, ARTIFACT_NAME, LATEST_VERSION, DOC_DIR, DocumentLanguage.ENGLISH.getCode(), CommonConstants.INDEX_HTML);
      assertEquals(expectedLatestResult, result, "Should handle latest version correctly");

      filesMock.when(() -> Files.exists(any(Path.class), any()))
          .thenReturn(false);
      result = service.resolveBestMatchRedirectUrl(RELATIVE_DOC_LOCATION_EN);
      assertNull(result, "Should return null for invalid path components");
    }
  }

  @Test
  void testResolveBestMatchRedirectUrlWithNonExistentSymlinks() {
    when(productRepository.findById(PORTAL)).thenReturn(Optional.of(EMPTY_PRODUCT));
    when(externalDocumentMetaRepository.findByProductId(PORTAL)).thenReturn(Collections.emptyList());

    String result = service.resolveBestMatchRedirectUrl(RELATIVE_DOC_LOCATION_EN);
    assertNull(result, "Should return null when symlinks don't exist");
  }

  @Test
  void testResolveBestMatchRedirectUrlSymlinkPathValidation() {
    when(productRepository.findById(PORTAL)).thenReturn(Optional.of(EMPTY_PRODUCT));

    String result = service.resolveBestMatchRedirectUrl(RELATIVE_WORKING_LOCATION_EN);
    assertNull(result, "Should return null for invalid symlink paths");
  }

  @Test
  void testCreateSymlinkForParentWithInvalidMajorVersion() throws IOException {
    Path productDir = CACHE_ROOT_PATH.resolve(PORTAL);
    Path versionDir = productDir.resolve(TEST_VERSION);
    Files.createDirectories(versionDir);

    String result = service.createSymlinkForMajorVersion(versionDir, RELATIVE_WORKING_LOCATION);
    assertEquals(StringUtils.EMPTY, result, "Should return empty string for invalid major version");

    result = service.createSymlinkForMajorVersion(versionDir, StringUtils.EMPTY);
    assertEquals(StringUtils.EMPTY, result, "Should return empty string for blank major version");
  }

  @Test
  void testCreateSymlinkForParentWithNullParentComponents() {
    Path rootPath = Paths.get(CommonConstants.SLASH);
    String result = service.createSymlinkForMajorVersion(rootPath, TEST_VERSION);
    assertEquals(StringUtils.EMPTY, result, "Should return empty string when parent structure is invalid");
  }

  @Test
  void testCreateSymlinkForParentOutsideCacheRoot() throws IOException {
    Path tempPath = tempDir.resolve(RELATIVE_WORKING_LOCATION);
    Files.createDirectories(tempPath);

    String result = service.createSymlinkForMajorVersion(tempPath, TEST_VERSION);
    assertEquals(StringUtils.EMPTY, result, "Should return empty string for path outside cache root");
  }

  @Test
  void testCreateSymlinkForParentExistingSymlinkWithSameTarget() throws IOException {
    Path productDir = CACHE_ROOT_PATH.resolve(PORTAL);
    Path versionDir = productDir.resolve(TEST_VERSION);
    Path docDir = versionDir.resolve(DOC_DIR);
    Files.createDirectories(docDir);

    try (MockedStatic<Files> filesMock = mockStatic(Files.class, CALLS_REAL_METHODS)) {
      Path symlinkPath = productDir.resolve(TEST_VERSION);
      Path targetPath = Path.of(TEST_VERSION);

      filesMock.when(() -> Files.exists(eq(symlinkPath), any()))
          .thenReturn(true);
      filesMock.when(() -> Files.isSymbolicLink(symlinkPath))
          .thenReturn(true);
      filesMock.when(() -> Files.readSymbolicLink(symlinkPath))
          .thenReturn(targetPath);
      filesMock.when(() -> Files.createSymbolicLink(eq(symlinkPath), eq(targetPath)))
          .thenReturn(symlinkPath);

      String result = service.createSymlinkForMajorVersion(docDir, TEST_VERSION);

      assertNotNull(result, "Should return symlink path when symlink exists with correct target");
      assertEquals(symlinkPath.toString(), result, "Result should be the symlink path");
    }
  }

  @Test
  void testCreateSymlinkForParentIOException() throws IOException {
    Path cacheRoot = Paths.get(DirectoryConstants.DATA_CACHE_DIR).toAbsolutePath().normalize();
    Path productDir = cacheRoot.resolve(PORTAL);
    Path versionDir = productDir.resolve(TEST_VERSION);
    Path docDir = versionDir.resolve(DOC_DIR);
    Files.createDirectories(docDir);

    try (MockedStatic<Files> filesMock = mockStatic(Files.class, CALLS_REAL_METHODS)) {
      Path symlinkPath = productDir.resolve(TEST_VERSION);
      Path targetPath = Path.of(TEST_VERSION);
      filesMock.when(() -> Files.exists(eq(symlinkPath), any()))
          .thenReturn(false);
      filesMock.when(() -> Files.createSymbolicLink(eq(symlinkPath), eq(targetPath)))
          .thenThrow(new IOException("Cannot create symlink"));

      String result = service.createSymlinkForMajorVersion(docDir, TEST_VERSION);

      assertEquals(StringUtils.EMPTY, result, "Should return empty string when IOException occurs");
    }
  }

  @Test
  void testUpdatedPathSymlinkExists() {
    when(productRepository.findById(PORTAL)).thenReturn(Optional.of(EMPTY_PRODUCT));
    when(externalDocumentMetaRepository.findByProductId(PORTAL)).thenReturn(List.of(
        ExternalDocumentMeta.builder().version(TEST_VERSION).build()));

    try (MockedStatic<VersionFactory> mockedVersionFactory = mockStatic(VersionFactory.class);
         MockedStatic<Files> filesMock = mockStatic(Files.class)) {
      mockedVersionFactory.when(() -> VersionFactory.getBestMatchMajorVersion(any(), eq(TEST_VERSION_12_5)))
          .thenReturn(TEST_VERSION);
      filesMock.when(() -> Files.exists(any(Path.class), any()))
          .thenReturn(true);
      String testPath = String.join(CommonConstants.SLASH, StringUtils.EMPTY, PORTAL, ARTIFACT_NAME, TEST_VERSION_12_5,
          DOC_DIR, DocumentLanguage.ENGLISH.getCode(), CommonConstants.INDEX_HTML);
      String result = service.resolveBestMatchRedirectUrl(testPath);

      assertNotNull(result, "Should return path when symlink exists");
      String expectedPath = String.join(CommonConstants.SLASH, StringUtils.EMPTY, DirectoryConstants.CACHE_DIR, PORTAL,
          ARTIFACT_NAME, TEST_VERSION, DOC_DIR, DocumentLanguage.ENGLISH.getCode(), CommonConstants.INDEX_HTML);
      assertEquals(expectedPath, result, "Should return updated path with best match version");
    }
  }

  @Test
  void testUpdatedPathSymlinkDoesNotExist() {
    when(productRepository.findById(PORTAL)).thenReturn(Optional.of(EMPTY_PRODUCT));
    when(externalDocumentMetaRepository.findByProductId(PORTAL)).thenReturn(List.of(
        ExternalDocumentMeta.builder().version(TEST_VERSION).build()));

    try (MockedStatic<VersionFactory> mockedVersionFactory = mockStatic(VersionFactory.class);
         MockedStatic<Files> filesMock = mockStatic(Files.class)) {
      mockedVersionFactory.when(() -> VersionFactory.get(any(), eq(TEST_VERSION_12_5)))
          .thenReturn(TEST_VERSION);
      filesMock.when(() -> Files.exists(any(Path.class), any()))
          .thenReturn(false);
      String testPath = String.join(CommonConstants.SLASH, StringUtils.EMPTY, PORTAL, ARTIFACT_NAME,
          TEST_VERSION_12_5, DOC_DIR, DocumentLanguage.ENGLISH.getCode(), CommonConstants.INDEX_HTML);
      String result = service.resolveBestMatchRedirectUrl(testPath);

      assertNull(result, "Should return null when symlink does not exist");
    }
  }

  @Test
  void testTryBuildUpdatedPathInvalidPathException() {
    when(productRepository.findById(PORTAL)).thenReturn(Optional.of(EMPTY_PRODUCT));
    when(externalDocumentMetaRepository.findByProductId(PORTAL)).thenReturn(List.of(
        ExternalDocumentMeta.builder().version(TEST_VERSION).build()));

    try (MockedStatic<VersionFactory> mockedVersionFactory = mockStatic(VersionFactory.class);
         MockedStatic<Paths> pathsMock = mockStatic(Paths.class)) {
      mockedVersionFactory.when(() -> VersionFactory.get(any(), eq(TEST_VERSION_12_5)))
          .thenReturn(TEST_VERSION);
      pathsMock.when(() -> Paths.get(anyString()))
          .thenThrow(new InvalidPathException("invalid:path", "Invalid character"));
      String result = service.resolveBestMatchRedirectUrl(RELATIVE_DOC_LOCATION_EN);

      assertNull(result, "Should return null when InvalidPathException occurs in symlink check");
    }
  }
}