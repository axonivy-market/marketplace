package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.factory.VersionFactory;
import com.axonivy.market.repository.ArtifactRepository;
import com.axonivy.market.repository.ExternalDocumentMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestPropertySource("classpath:application-test.properties")
@SpringBootTest
class ExternalDocumentServiceImplTest extends BaseSetup {

  private static final String RELATIVE_WORKING_LOCATION = "/market-cache/portal/10.0.0/doc";
  private static final String RELATIVE_DOC_LOCATION = RELATIVE_WORKING_LOCATION + "/index.html";

  private static final String PORTAL = "portal";

  @MockBean
  ProductRepository productRepository;

  @MockBean
  ExternalDocumentMetaRepository externalDocumentMetaRepository;

  @MockBean
  FileDownloadService fileDownloadService;

  @MockBean
  ArtifactRepository artifactRepository;

  @SpyBean
  ExternalDocumentServiceImpl service;

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
    verify(externalDocumentMetaRepository, times(6)).findByProductIdAndVersionIn(any(), any());
  }

  @Test
  void testSyncDocumentForProductButCannotExtractToShareFolder() throws IOException {
    prepareProductDataForSyncTest();

    service.syncDocumentForProduct(PORTAL, true, MOCK_RELEASED_VERSION);
    verify(productRepository, times(1)).findProductByIdAndRelatedData(any());
    verify(externalDocumentMetaRepository, times(0)).save(any());
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
    assertTrue(result.isEmpty());

    when(productRepository.findAllProductsHaveDocument()).thenReturn(List.of(mockPortalProduct().get()));
    result = service.findAllProductsHaveDocument();
    assertNotNull(result);
    assertEquals(PORTAL, result.get(0).getId());
  }

  @Test
  void testFindBestMatchVersion() {
    String productId = "product1";
    String version = "3.0.0";

    when(productRepository.findById(productId)).thenReturn(Optional.of(new Product()));
    when(externalDocumentMetaRepository.findByProductId(productId)).thenReturn(List.of(
            ExternalDocumentMeta.builder().version(version).build()));
    try (MockedStatic<VersionFactory> mockedVersionFactory = mockStatic(VersionFactory.class)) {
      mockedVersionFactory.when(() -> VersionFactory.get(Collections.singletonList(version), version)).thenReturn(version);

      String result = service.findBestMatchVersion(productId, version);

      assertEquals(version, result, "Should return the matched version");
      mockedVersionFactory.verify(() -> VersionFactory.get(Collections.singletonList(version), version));
    }

    verify(productRepository).findById(productId);
    verify(externalDocumentMetaRepository).findByProductId(productId);
  }

  @Test
  void testFindExternalDocumentURI() {
    var mockVersion = "10.0.0";
    var mockProductDocumentMeta = new ExternalDocumentMeta();
    when(productRepository.findById(PORTAL)).thenReturn(mockPortalProduct());
    var result = service.findExternalDocument(PORTAL, mockVersion);
    verify(productRepository, times(1)).findById(any());
    assertNull(result);

    mockProductDocumentMeta.setProductId(PORTAL);
    mockProductDocumentMeta.setVersion(mockVersion);
    mockProductDocumentMeta.setRelativeLink(RELATIVE_DOC_LOCATION);
    when(externalDocumentMetaRepository.findByProductId(PORTAL)).thenReturn(List.of(mockProductDocumentMeta));
    result = service.findExternalDocument(PORTAL, mockVersion);
    assertNotNull(result);
    assertTrue(result.getRelativeLink().contains("/index.html"));
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
    return Artifact.builder().artifactId("portal-guide").doc(true).groupId("portal")
        .name("Portal Guide").type("zip").build();
  }
}
