package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.Product;
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
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductDependencyServiceImplTest extends BaseSetup {

  static final String MOCK_GROUP_ID = "com.axonivy.portal";
  static final String MOCK_PRODUCT_ID = "portal";
  static final String MOCK_ARTIFACT_ID = "portal";
  static final String MOCK_VERSION = "10.0.0-SNAPSHOT";
  static final String MOCK_DEPENDENCY_ARTIFACT_ID = "portal-components";
  static final String PORTAL_MAVEN_URL = "https://maven.axonivy.com/com/axonivy/portal/";
  static final String PORTAL_NEXUS_URL = "https://nexus-mirror.axonivy.com/repository/maven/com/axonivy/portal/";
  static final String MOCK_DOWNLOAD_URL = PORTAL_MAVEN_URL + "portal/10.0.0-SNAPSHOT/portal10.0.0-SNAPSHOT.iar";
  static final String MOCK_DOWNLOAD_POM_URL = PORTAL_NEXUS_URL + "portal/10.0.0-SNAPSHOT/portal10.0.0-SNAPSHOT.pom";
  static final String MOCK_DOWNLOAD_POM_DEPENDENCY_URL = PORTAL_NEXUS_URL
      + "portal-components/10.0.0-SNAPSHOT/portal-components10.0.0-SNAPSHOT.pom";

  @Mock
  FileDownloadService fileDownloadService;
  @Mock
  ProductRepository productRepository;
  @Mock
  MavenArtifactVersionRepository mavenArtifactVersionRepository;
  @Mock
  MetadataRepository metadataRepository;
  @Mock
  ProductDependencyRepository productDependencyRepository;
  @InjectMocks
  ProductDependencyServiceImpl productDependencyService;

  @Test
  void testSyncIARDependencies() throws IOException {
    var mockProduct = Product.builder().id(MOCK_PRODUCT_ID).listed(true).build();
    when(productRepository.findAll()).thenReturn(List.of(mockProduct));

    prepareTestData(any());

    int totalSynced = productDependencyService.syncIARDependenciesForProducts(false, null);
    assertTrue(totalSynced > 0, "Expected at least one product was synced but service returned nothing");
  }

  @Test
  void testNothingToSync() {
    when(productRepository.findAll()).thenReturn(List.of());
    int totalSynced = productDependencyService.syncIARDependenciesForProducts(true, null);
    assertEquals(0, totalSynced, "Expected no product was synced but service returned something");
  }

  @Test
  void testSyncForProductId() throws IOException {
    prepareTestData(MOCK_PRODUCT_ID);
    var mockProductDependency = ProductDependency.builder()
        .productId(MOCK_PRODUCT_ID)
        .version(MOCK_VERSION)
        .artifactId(MOCK_ARTIFACT_ID)
        .downloadUrl(MOCK_DOWNLOAD_URL)
        .build();
    mockProductDependency.setDependencies(Set.of(mockProductDependency));
    when(productDependencyRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(List.of(mockProductDependency));

    int totalSynced = productDependencyService.syncIARDependenciesForProducts(true, MOCK_PRODUCT_ID);
    assertEquals(1, totalSynced, "Expected 1 product was synced but service returned nothing");
  }

  private void prepareTestData(String mockProductId) throws IOException {
    List<MavenArtifactVersion> mavenArtifactVersionMockList = new ArrayList<>();
    var mavenArtifactVersionMock = mockMavenArtifactVersion(MOCK_VERSION, MOCK_ARTIFACT_ID, MOCK_DOWNLOAD_URL);
    mavenArtifactVersionMock.setProductId(MOCK_PRODUCT_ID);
    mavenArtifactVersionMock.getId().setAdditionalVersion(false);
    mavenArtifactVersionMockList.add(mavenArtifactVersionMock);
    when(mavenArtifactVersionRepository.findByProductIdOrderByAdditionalVersion(mockProductId))
        .thenReturn(mavenArtifactVersionMockList);

    // Mock for main artifact
    when(fileDownloadService.downloadFile(MOCK_DOWNLOAD_POM_URL))
        .thenReturn(Files.readAllBytes(new File("src/test/resources/zip/test-pom.xml").toPath()));

    when(metadataRepository.findByGroupIdAndArtifactId(MOCK_GROUP_ID, MOCK_DEPENDENCY_ARTIFACT_ID))
        .thenReturn(List.of(Metadata.builder()
            .productId(MOCK_PRODUCT_ID)
            .artifactId(MOCK_DEPENDENCY_ARTIFACT_ID)
            .groupId(MOCK_GROUP_ID)
            .versions(Set.of(MOCK_VERSION)).build()));

    when(mavenArtifactVersionRepository.findByProductIdAndArtifactIdAndVersion(MOCK_PRODUCT_ID,
        MOCK_DEPENDENCY_ARTIFACT_ID, MOCK_VERSION))
        .thenReturn(List.of(
            mockMavenArtifactVersion(MOCK_VERSION, MOCK_DEPENDENCY_ARTIFACT_ID, MOCK_DOWNLOAD_POM_DEPENDENCY_URL)));

    // Mock for dependency artifact
    when(fileDownloadService.downloadFile(MOCK_DOWNLOAD_POM_DEPENDENCY_URL))
        .thenReturn(Files.readAllBytes(new File("src/test/resources/zip/test-empty-dependency-pom.xml").toPath()));
  }
}
