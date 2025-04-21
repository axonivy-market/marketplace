package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductDependency;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductDependencyRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

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
  @InjectMocks
  ProductDependencyServiceImpl mavenDependencyService;

  @Test
  void testSyncIARDependencies() {
    prepareDataForTest(true);
    int totalSynced = mavenDependencyService.syncIARDependenciesForProducts(false);
    assertTrue(totalSynced > 0, "Expected at least one product was synced but service returned nothing");
  }

  private void prepareDataForTest(boolean isProductArtifact) {
    ProductDependency mockProductDependency = ProductDependency.builder().productId(SAMPLE_PRODUCT_ID)
        .dependencies(List.of())
        .build();
    List<MavenArtifactVersion> mavenArtifactVersionMock = createMavenArtifactVersionMock(isProductArtifact);
    when(productRepository.findAll()).thenReturn(createPageProductsMock().getContent());
    when(productDependencyRepository.findAllWithDependencies()).thenReturn(List.of(mockProductDependency));
    List<Product> mockProducts = createPageProductsMock().getContent().stream()
        .filter(product -> Boolean.FALSE != product.getListed())
        .toList();
    when(productRepository.findAll()).thenReturn(mockProducts);
    when(productDependencyRepository.findAllWithDependencies()).thenReturn(List.of());
    when(mavenArtifactVersionRepository.findByProductId(any())).thenReturn(mavenArtifactVersionMock);
    when(productDependencyRepository.save(any())).thenReturn(
        ProductDependency.builder().productId(SAMPLE_PRODUCT_ID).build());
  }

  @Test
  void testSyncIARDependenciesWithAdditionArtifacts() throws IOException {
    prepareDataForTest(false);
    when(fileDownloadService.downloadAndUnzipFile(any(), any())).thenReturn(SAMPLE_PRODUCT_PATH);
    int totalSynced = mavenDependencyService.syncIARDependenciesForProducts(false);
    assertTrue(totalSynced > 0, "Expected at least one product was synced but service returned nothing");
  }

  private List<MavenArtifactVersion> createMavenArtifactVersionMock(boolean isProductArtifact) {
    List<MavenArtifactVersion> mavenArtifactVersionsMock = getMockMavenArtifactVersionWithData();
    for (MavenArtifactVersion mavenArtifactVersion : mavenArtifactVersionsMock) {
      mavenArtifactVersion.setProductId(SAMPLE_PRODUCT_ID);
    }

    if (isProductArtifact) {
      mavenArtifactVersionsMock.add(mockMavenArtifactVersion(MOCK_SNAPSHOT_VERSION, MOCK_ARTIFACT_ID));
    } else {
      mavenArtifactVersionsMock.add(mockAdditionalMavenArtifactVersion(MOCK_SNAPSHOT_VERSION, MOCK_ARTIFACT_ID));
    }
    return mavenArtifactVersionsMock;
  }

  @Test
  void testNothingToSync() {
    when(productRepository.findAll()).thenReturn(List.of());
    int totalSynced = mavenDependencyService.syncIARDependenciesForProducts(true);
    assertEquals(0, totalSynced, "Expected no product was synced but service returned something");
  }

}
