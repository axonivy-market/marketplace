package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
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
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.axonivy.market.constants.DirectoryConstants.DOC_DIR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestPropertySource("classpath:application-test.properties")
@SpringBootTest
class ExternalDocumentServiceImplTest extends BaseSetup {

  private static final String RELATIVE_WORKING_LOCATION = "/market-cache/portal/10.0.0/doc";
  private static final String RELATIVE_DOC_LOCATION = RELATIVE_WORKING_LOCATION + "/index.html";

  private static final String PORTAL = "portal";

  private static final String TEST_VERSION = "12.0";

  private final List<String> majorVersions = List.of("10.0", "12.0", "13.1", "dev");

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
    verify(externalDocumentMetaRepository, times(2)).save(any());

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
    verify(externalDocumentMetaRepository, times(1)).save(any());
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
    verify(externalDocumentMetaRepository, times(1)).save(any());
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

  private Optional<Product> mockPortalProductHasNoArtifact() {
    var product = mockPortal(true);
    return Optional.of(product);
  }

  @Test
  void testFindExternalDocumentURI() {
    var mockVersion = "10.0";
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
//
//  @Test
//  void testResolveBestMatchRedirectUrl() {
//    String path = "portal/guide/10.0/en";
//    ExternalDocumentMeta mockMeta = ExternalDocumentMeta.builder()
//        .productId(PORTAL)
//        .version("10.0")
//        .language(DocumentLanguage.ENGLISH)
//        .relativeLink("/market-cache/portal/guide/10.0/en/index.html")
//        .storageDirectory("/usr/share/nginx/html/market-cache/portal/guide/10.0/en")
//        .build();
//
//    when(externalDocumentMetaRepository.findByProductIdAndLanguageAndVersion(PORTAL, DocumentLanguage.ENGLISH, "10.0"))
//        .thenReturn(List.of(mockMeta));
//    doReturn(true).when(service).doesDocExistInShareFolder(anyString());
//
//    String result = service.resolveBestMatchRedirectUrl(path);
//    assertNotNull(result, "Should return a redirect URL");
//    assertTrue(result.startsWith("/market-cache/portal/guide/10.0/en/"),
//        "Should return normalized path");
//
//    path = "portal/guide/10.0/en/index.html";
//    result = service.resolveBestMatchRedirectUrl(path);
//    assertFalse(result.endsWith("index.html"),
//        "Should remove index.html from the end");
//    assertTrue(result.endsWith("/"),
//        "Should end with forward slash");
//
//    result = service.resolveBestMatchRedirectUrl("");
//    assertNull(result, "Should return null for empty path");
//
//    when(externalDocumentMetaRepository.findByProductIdAndLanguageAndVersion(any(), any(), any()))
//        .thenReturn(Collections.emptyList());
//    result = service.resolveBestMatchRedirectUrl("invalid/path");
//    assertNull(result, "Should return null when document meta not found");
//  }

  @Test
  void testResolveBestMatchRedirectUrlWithSymlink() {
    String path = "portal/guide/12/en";
    ExternalDocumentMeta mockMeta = ExternalDocumentMeta.builder()
        .productId(PORTAL)
        .version("12.0")
        .language(DocumentLanguage.ENGLISH)
        .relativeLink("/market-cache/portal/guide/12.0/en/index.html")
        .storageDirectory("/usr/share/nginx/html/market-cache/portal/guide/12.0/en")
        .build();

    when(externalDocumentMetaRepository.findByProductIdAndLanguageAndVersion(PORTAL, DocumentLanguage.ENGLISH, "12.0"))
        .thenReturn(List.of(mockMeta));
    doReturn(true).when(service).doesDocExistInShareFolder(anyString());
    doReturn("12.0").when(service).resolveSymlinkVersion(anyString(), anyString());

    String result = service.resolveBestMatchRedirectUrl(path);
    assertNotNull(result, "Should resolve symlink version");
    assertTrue(result.contains("12.0"), "Should contain resolved version");
  }

  @Test
  void testResolveSymlinkVersion() {
    String symlinkDir = "/usr/share/nginx/html/market-cache/portal/guide";
    String symlinkName = "12";

    try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.isSymbolicLink(any(Path.class)))
          .thenReturn(true);

      mockedFiles.when(() -> Files.readSymbolicLink(any(Path.class)))
          .thenReturn(Path.of("12.0"));

      String result = service.resolveSymlinkVersion(symlinkDir, symlinkName);
      assertEquals("12.0", result, "Should resolve symlink to actual version");

      mockedFiles.when(() -> Files.readSymbolicLink(any(Path.class)))
          .thenThrow(new IOException("Mock IO Exception"));

      result = service.resolveSymlinkVersion(symlinkDir, symlinkName);
      assertNull(result, "Should return null on error");
    }
  }

  @Test
  void testResolveBestMatchRedirectUrlWithVariousPatterns() {
    String path = "portal/guide/dev/en";
    ExternalDocumentMeta mockMeta = ExternalDocumentMeta.builder()
        .productId(PORTAL)
        .version("dev")
        .language(DocumentLanguage.ENGLISH)
        .relativeLink("/market-cache/portal/guide/dev/en/index.html")
        .storageDirectory("/usr/share/nginx/html/market-cache/portal/guide/dev/en")
        .build();

    when(externalDocumentMetaRepository.findByProductIdAndLanguageAndVersion(PORTAL, DocumentLanguage.ENGLISH, "dev"))
        .thenReturn(List.of(mockMeta));
    doReturn(true).when(service).doesDocExistInShareFolder(anyString());

    String result = service.resolveBestMatchRedirectUrl(path);
    assertNotNull(result, "Should handle dev version");

    path = "portal/guide/10.0-m1/en";
    mockMeta.setVersion("10.0-m1");
    mockMeta.setRelativeLink("/market-cache/portal/guide/10.0-m1/en/index.html");

    when(externalDocumentMetaRepository.findByProductIdAndLanguageAndVersion(PORTAL, DocumentLanguage.ENGLISH,
        "10.0-m1"))
        .thenReturn(List.of(mockMeta));

    result = service.resolveBestMatchRedirectUrl(path);
    assertNotNull(result, "Should handle milestone version");
    assertTrue(result.contains("10.0-m1"), "Should preserve milestone version in path");
  }

  private Optional<Product> mockPortalProduct() {
    var product = mockPortal(false);
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
  void testSetSymlinkPermissions_PosixSupported() throws IOException {
    Path mockPath = Path.of("/tmp/symlink");
    Set<String> views = Set.of("posix");

    var mockFileSystem = mock(java.nio.file.FileSystem.class);
    when(mockFileSystem.supportedFileAttributeViews()).thenReturn(views);

    try (MockedStatic<FileSystems> mockedFileSystems = Mockito.mockStatic(FileSystems.class);
         MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {

      mockedFileSystems.when(FileSystems::getDefault).thenReturn(mockFileSystem);

      PosixFileAttributeView mockView = mock(PosixFileAttributeView.class);
      mockedFiles.when(() -> Files.getFileAttributeView(
              eq(mockPath), eq(PosixFileAttributeView.class), eq(LinkOption.NOFOLLOW_LINKS)))
          .thenReturn(mockView);

      doNothing().when(mockView).setPermissions(any());

      String result = service.setSymlinkPermissions(mockPath);

      verify(mockView, times(1)).setPermissions(any());
      assertTrue(result.endsWith(File.separator + DOC_DIR), "Should return path with DOC_DIR");
    }
  }

  @Test
  void testSetSymlinkPermissions_NonPosix() {
    Path mockPath = Path.of("/tmp/symlink");
    Set<String> views = Set.of("basic");

    var mockFileSystem = mock(java.nio.file.FileSystem.class);
    when(mockFileSystem.supportedFileAttributeViews()).thenReturn(views);

    try (MockedStatic<FileSystems> mockedFileSystems = Mockito.mockStatic(FileSystems.class)) {
      mockedFileSystems.when(FileSystems::getDefault).thenReturn(mockFileSystem);

      String result = service.setSymlinkPermissions(mockPath);

      assertTrue(result.endsWith(File.separator + DOC_DIR), "Should return path with DOC_DIR");
    }
  }
}
