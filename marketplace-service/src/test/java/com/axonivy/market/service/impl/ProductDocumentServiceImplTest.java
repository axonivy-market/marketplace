package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductDocumentMeta;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.repository.ProductDocumentMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProductDocumentServiceImplTest {

  private static final String PORTAL = "portal";

  @Mock
  ProductRepository productRepository;

  @Mock
  ProductDocumentMetaRepository productDocumentMetaRepository;

  @Mock
  FileDownloadService fileDownloadService;

  @InjectMocks
  ProductDocumentServiceImpl service;

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
  void testFindViewDocURI() {
    var mockVersion = "10.0.0";
    var mockProductDocumentMeta = new ProductDocumentMeta();
    when(productRepository.findById(PORTAL)).thenReturn(mockPortalProduct());
    when(productDocumentMetaRepository.findAll()).thenReturn(List.of(mockProductDocumentMeta));
    var result = service.findViewDocURI(PORTAL, mockVersion);
    verify(productRepository, times(1)).findById(any());
    assertTrue(StringUtils.isEmpty(result));

    mockProductDocumentMeta.setProductId(PORTAL);
    mockProductDocumentMeta.setVersion(mockVersion);
    mockProductDocumentMeta.setViewDocUrl("/market-cache/portal/10.0.0/doc/index.html");
    when(productDocumentMetaRepository.findAll()).thenReturn(List.of(mockProductDocumentMeta));
    result = service.findViewDocURI(PORTAL, mockVersion);
    assertNotNull(result);
    assertTrue(result.contains("/index.html"));
  }

  private Optional<Product> mockPortalProduct() {
    var artifact = MavenArtifact.builder().artifactId("portal-guide").doc(true).groupId("portal")
        .name("Portal Guide").type("zip").build();
    var product = Product.builder().id(PORTAL).artifacts(List.of(artifact)).releasedVersions(
        List.of("8.0.0", "10.0.0")).build();
    return Optional.of(product);
  }
}
