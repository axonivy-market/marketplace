package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.ProductDependency;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductDependencyRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.FileDownloadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MavenDependencyServiceImplTest extends BaseSetup {

  @Mock
  FileDownloadService fileDownloadService;
  @Mock
  ProductRepository productRepository;
  @Mock
  ProductDependencyRepository productDependencyRepository;
  @Mock
  MavenArtifactVersionRepository mavenArtifactVersionRepository;
  @InjectMocks
  MavenDependencyServiceImpl mavenDependencyService;

  @Test
  void testSyncIARDependencies() {
    prepareDataForTest(true);
    int totalSynced = mavenDependencyService.syncIARDependenciesForProducts();
    assertTrue(totalSynced > 0, "Expected at least one product was synced but service returned nothing");
  }

  private void prepareDataForTest(boolean isProductArtifact) {
    var mavenArtifactVersionMock = createMavenArtifactVersionMock(isProductArtifact);
    when(productRepository.searchByCriteria(any(), any(Pageable.class))).thenReturn(createPageProductsMock());
    when(productDependencyRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
    when(mavenArtifactVersionRepository.findById(any())).thenReturn(Optional.of(mavenArtifactVersionMock));
    when(productDependencyRepository.save(any())).thenReturn(
        ProductDependency.builder().productId(SAMPLE_PRODUCT_ID).build());
  }

  @Test
  void testSyncIARDependenciesWithAdditionArtifacts() throws IOException {
    prepareDataForTest(false);
    when(fileDownloadService.downloadAndUnzipFile(any(), any())).thenReturn(SAMPLE_PRODUCT_PATH);
    int totalSynced = mavenDependencyService.syncIARDependenciesForProducts();
    assertTrue(totalSynced > 0, "Expected at least one product was synced but service returned nothing");
  }

  private MavenArtifactVersion createMavenArtifactVersionMock(boolean isProductArtifact) {
    var mavenArtifactVersionMock = getMockMavenArtifactVersionWithData();
    mavenArtifactVersionMock.setProductId(SAMPLE_PRODUCT_ID);
    List<MavenArtifactModel> mockArtifactModels = new ArrayList<>();
    mockArtifactModels.add(MavenArtifactModel.builder().artifactId(MOCK_ARTIFACT_ID).build());
    if (isProductArtifact) {
      mavenArtifactVersionMock.getProductArtifactsByVersion().put(MOCK_SNAPSHOT_VERSION, mockArtifactModels);
    } else {
      mavenArtifactVersionMock.getAdditionalArtifactsByVersion().put(MOCK_SNAPSHOT_VERSION, mockArtifactModels);
    }
    return mavenArtifactVersionMock;
  }

  @Test
  void testNothingToSync() {
    when(productRepository.searchByCriteria(any(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
    when(productDependencyRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));
    int totalSynced = mavenDependencyService.syncIARDependenciesForProducts();
    assertEquals(0, totalSynced, "Expected no product was synced but service returned something");
  }

}
