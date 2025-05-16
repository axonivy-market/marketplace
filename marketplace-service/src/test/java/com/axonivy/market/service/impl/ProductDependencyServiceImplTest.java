package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.ProductDependency;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductDependencyRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductDependencyServiceImplTest extends BaseSetup {

  @Mock
  FileDownloadService fileDownloadService;
  @Mock
  ProductRepository productRepository;
  @Mock
  ProductDependencyRepository productDependencyRepository;
  @Mock
  MavenArtifactVersionRepository mavenArtifactVersionRepository;
  @Mock
  MetadataRepository metadataRepository;
  @InjectMocks
  ProductDependencyServiceImpl productDependencyService;

  @Test
  void testSyncIARDependencies() throws IOException {
    when(mavenArtifactVersionRepository.findByProductIdAndArtifactIdAndVersion(any(), any(), any()))
        .thenReturn(
            List.of(mockMavenArtifactVersion(MOCK_SNAPSHOT_VERSION, MOCK_ARTIFACT_ID, MOCK_SNAPSHOT_MAVEN_URL)));
    prepareDataForTest(true);
    when(fileDownloadService.downloadFile(MOCK_SNAPSHOT_MAVEN_URL)).thenReturn(Files.readAllBytes(
        new File("src/test/resources/zip/test-empty-dependency-pom.xml").toPath()));
    int totalSynced = productDependencyService.syncIARDependenciesForProducts(false, MOCK_PRODUCT_ID);
    assertTrue(totalSynced > 0, "Expected at least one product was synced but service returned nothing");
  }

  private void prepareDataForTest(boolean isProductArtifact) throws IOException {
    List<MavenArtifactVersion> mavenArtifactVersionsMock = getMockMavenArtifactVersion();
    var mavenArtifactVersionMock = mockMavenArtifactVersion(MOCK_SNAPSHOT_VERSION, MOCK_ARTIFACT_ID, MOCK_DOWNLOAD_URL);
    mavenArtifactVersionMock.setProductId(MOCK_PRODUCT_ID);
    mavenArtifactVersionMock.getId().setAdditionalVersion(true);
    if (isProductArtifact) {
      mavenArtifactVersionMock.getId().setAdditionalVersion(false);
    }
    mavenArtifactVersionsMock.add(mavenArtifactVersionMock);
    when(mavenArtifactVersionRepository.findByProductIdOrderByAdditionalVersion(any()))
        .thenReturn(mavenArtifactVersionsMock);
    var mockPomFile = new File("src/test/resources/zip/test-pom.xml");
    when(fileDownloadService.downloadFile(any())).thenReturn(Files.readAllBytes(mockPomFile.toPath()));
    when(metadataRepository.findByGroupIdAndArtifactId(any(), any())).thenReturn(List.of(Metadata.builder()
        .productId(MOCK_PRODUCT_ID).artifactId(MOCK_ARTIFACT_ID).groupId(MOCK_GROUP_ID)
        .versions(Set.of(MOCK_SNAPSHOT_VERSION)).build()));
  }

  @Test
  void testSyncIARDependenciesWithAdditionArtifacts() throws IOException {
    prepareDataForTest(false);
    when(productRepository.findAll()).thenReturn(createPageProductsMock().getContent());
    int totalSynced = productDependencyService.syncIARDependenciesForProducts(false, null);
    assertEquals(0, totalSynced);
  }

  @Test
  void testNothingToSync() {
    when(productRepository.findAll()).thenReturn(List.of());
    int totalSynced = productDependencyService.syncIARDependenciesForProducts(true, null);
    assertEquals(0, totalSynced, "Expected no product was synced but service returned something");
  }

  @Test
  void testSyncForProductId() {
    var productDependency = mockProductDependency();
    productDependency.setDependencies(new HashSet<>());
    productDependency.getDependencies().add(mockProductDependency());
    var mockProductDependencies = new ArrayList<ProductDependency>();
    mockProductDependencies.add(productDependency);
    when(productDependencyRepository.findByProductId("portal")).thenReturn(mockProductDependencies);
    int totalSynced = productDependencyService.syncIARDependenciesForProducts(true, "portal");
    assertEquals(0, totalSynced, "Expected no product was synced but service returned something");
  }
}
