package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.ArtifactRepository;
import com.axonivy.market.repository.ExternalDocumentMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalDocumentServiceImplTest extends BaseSetup {

  private static final String RELATIVE_LOCATION = "/market-cache/portal/10.0.0/doc/index.html";

  private static final String PORTAL = "portal";

  @Mock
  ProductRepository productRepository;

  @Mock
  ExternalDocumentMetaRepository externalDocumentMetaRepository;

  @Mock
  FileDownloadService fileDownloadService;

  @Mock
  ArtifactRepository artifactRepository;

  @InjectMocks
  ExternalDocumentServiceImpl service;

  @Test
  void testSyncDocumentForProduct() throws IOException {
    when(productRepository.findProductByIdAndRelatedData(PORTAL)).thenReturn(mockPortalProductHasNoArtifact().get());
    service.syncDocumentForProduct(PORTAL, true, null);
    verify(productRepository, times(1)).findProductByIdAndRelatedData(any());

    when(artifactRepository.findAllByIdInAndFetchArchivedArtifacts(any())).thenReturn(mockPortalProduct().get().getArtifacts());
    when(productRepository.findProductByIdAndRelatedData(PORTAL)).thenReturn(mockPortalProduct().get());
    service.syncDocumentForProduct(PORTAL, false, null);
    verify(externalDocumentMetaRepository, times(1)).findByProductIdAndVersionIn(any(), any());

    when(fileDownloadService.downloadAndUnzipFile(any(), any())).thenReturn("data" + RELATIVE_LOCATION);
    service.syncDocumentForProduct(PORTAL, true, null);
    verify(externalDocumentMetaRepository, times(2)).save(any());

    when(artifactRepository.findAllByIdInAndFetchArchivedArtifacts(any())).thenReturn(mockPortalProduct().get().getArtifacts());
    when(productRepository.findProductByIdAndRelatedData(PORTAL)).thenReturn(mockPortalProduct().get());
    when(externalDocumentMetaRepository.findByProductIdAndVersionIn(any(),any())).thenReturn(List.of(createExternalDocumentMock()));
    service.syncDocumentForProduct(PORTAL, false, null);
    verify(externalDocumentMetaRepository, times(6)).findByProductIdAndVersionIn(any(), any());
  }

  @Test
  void testSyncDocumentForProductIdAndVersion() throws IOException {
    when(artifactRepository.findAllByIdInAndFetchArchivedArtifacts(any()))
        .thenReturn(mockPortalProduct().map(Product::getArtifacts).orElse(null));
    when(productRepository.findProductByIdAndRelatedData(PORTAL)).thenReturn(mockPortalProduct().orElse(null));
    when(externalDocumentMetaRepository.findByProductIdAndVersionIn(any(),any()))
        .thenReturn(List.of(createExternalDocumentMock()));
    when(fileDownloadService.downloadAndUnzipFile(any(), any())).thenReturn("data" + RELATIVE_LOCATION);

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
  void testFindExternalDocumentURI() {
    var mockVersion = "10.0.0";
    var mockProductDocumentMeta = new ExternalDocumentMeta();
    when(productRepository.findById(PORTAL)).thenReturn(mockPortalProduct());
    var result = service.findExternalDocument(PORTAL, mockVersion);
    verify(productRepository, times(1)).findById(any());
    assertNull(result);

    mockProductDocumentMeta.setProductId(PORTAL);
    mockProductDocumentMeta.setVersion(mockVersion);
    mockProductDocumentMeta.setRelativeLink(RELATIVE_LOCATION);
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
