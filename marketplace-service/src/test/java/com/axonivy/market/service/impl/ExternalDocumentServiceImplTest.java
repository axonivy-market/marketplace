package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;
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
class ExternalDocumentServiceImplTest {

  private static final String RELATIVE_LOCATION = "/market-cache/portal/10.0.0/doc/index.html";

  private static final String PORTAL = "portal";

  @Mock
  ProductRepository productRepository;

  @Mock
  ExternalDocumentMetaRepository externalDocumentMetaRepository;

  @Mock
  FileDownloadService fileDownloadService;

  @InjectMocks
  ExternalDocumentServiceImpl service;

  @Test
  void testSyncDocumentForProduct() throws IOException {
    when(productRepository.findById(PORTAL)).thenReturn(mockPortalProductHasNoArtifact());
    service.syncDocumentForProduct(PORTAL, true);
    verify(productRepository, times(1)).findById(any());
    verify(externalDocumentMetaRepository, times(0)).findByProductIdAndVersion(any(), any());

    when(productRepository.findById(PORTAL)).thenReturn(mockPortalProduct());
    service.syncDocumentForProduct(PORTAL, false);
    verify(externalDocumentMetaRepository, times(2)).findByProductIdAndVersion(any(), any());

    when(fileDownloadService.downloadAndUnzipFile(any(), anyBoolean())).thenReturn("data" + RELATIVE_LOCATION);
    service.syncDocumentForProduct(PORTAL, true);
    verify(externalDocumentMetaRepository, times(2)).save(any());
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
