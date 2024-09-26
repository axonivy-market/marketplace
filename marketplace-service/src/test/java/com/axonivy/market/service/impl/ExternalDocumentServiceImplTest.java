package com.axonivy.market.service.impl;

import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.entity.Product;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.repository.ExternalDocumentMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExternalDocumentServiceImplTest {

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
  void testSyncDocumentForProduct() {
    when(productRepository.findById(PORTAL)).thenReturn(mockPortalProduct());
    service.syncDocumentForProduct(PORTAL, true);
    verify(productRepository, times(1)).findById(any());
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
    when(externalDocumentMetaRepository.findAll()).thenReturn(List.of(mockProductDocumentMeta));
    var result = service.findExternalDocumentURI(PORTAL, mockVersion);
    verify(productRepository, times(1)).findById(any());
    assertTrue(StringUtils.isEmpty(result));

    mockProductDocumentMeta.setProductId(PORTAL);
    mockProductDocumentMeta.setVersion(mockVersion);
    mockProductDocumentMeta.setRelativeLink("/market-cache/portal/10.0.0/doc/index.html");
    when(externalDocumentMetaRepository.findAll()).thenReturn(List.of(mockProductDocumentMeta));
    result = service.findExternalDocumentURI(PORTAL, mockVersion);
    assertNotNull(result);
    assertTrue(result.contains("/index.html"));
  }

  private Optional<Product> mockPortalProduct() {
    var artifact = MavenArtifact.builder().artifactId("portal-guide").doc(true).groupId("portal").name(
        "Portal Guide").type("zip").build();
    var product = Product.builder().id(PORTAL).artifacts(List.of(artifact)).releasedVersions(
        List.of("8.0.0", "10.0.0")).build();
    return Optional.of(product);
  }
}
