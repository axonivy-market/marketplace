package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.MetadataSyncRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.util.MavenUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Log4j2
@ExtendWith(MockitoExtension.class)
class MetadataServiceImplTest extends BaseSetup {
  @InjectMocks
  MetadataServiceImpl metadataService;
  @Mock
  ProductRepository productRepo;
  @Mock
  ProductJsonContentRepository productJsonRepo;
  @Mock
  private MetadataRepository metadataRepo;
  @Mock
  private MavenArtifactVersionRepository mavenArtifactVersionRepo;
  @Mock
  private MetadataSyncRepository metadataSyncRepo;

  @Test
  void testUpdateArtifactAndMetaDataForProduct() {
    ProductJsonContent mockProductJsonContent = getMockProductJsonContent();
    mockProductJsonContent.setProductId(MOCK_PRODUCT_ID);

    Artifact mockArtifact = getMockArtifact();
    Metadata mockMetadata = buildMocKMetadata();
    Product mockProduct = getMockProduct();
    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
      mockUtils.when(() -> MavenUtils.getMetadataContentFromUrl(ArgumentMatchers.anyString())).thenReturn(null);
      mockUtils.when(() -> MavenUtils.convertArtifactsToMetadataSet(any(), any())).thenReturn(Set.of(mockMetadata));

      metadataService.updateArtifactAndMetadata(mockProduct.getId(), List.of(MOCK_RELEASED_VERSION) ,List.of(mockArtifact));

      verify(mavenArtifactVersionRepo, times(1)).save(any());
      verify(metadataRepo, times(1)).saveAll(any());
    }
  }

  @Test
  void testGetArtifactsFromNonSyncedVersion() {
    Mockito.when(productJsonRepo.findByProductIdAndVersionIn(MOCK_PRODUCT_ID, List.of(MOCK_RELEASED_VERSION))).thenReturn(
        List.of(getMockProductJsonContent()));
    Set<Artifact> artifacts = metadataService.getArtifactsFromNonSyncedVersion(MOCK_PRODUCT_ID,
        Collections.emptyList());
    Assertions.assertTrue(CollectionUtils.isEmpty(artifacts));
    Mockito.verify(productJsonRepo, Mockito.never()).findByProductIdAndVersionIn(Mockito.anyString(), any());
    artifacts = metadataService.getArtifactsFromNonSyncedVersion(MOCK_PRODUCT_ID, List.of(MOCK_RELEASED_VERSION));
    Assertions.assertEquals(2, artifacts.size());
    Assertions.assertEquals("bpmn-statistic-demo", artifacts.iterator().next().getArtifactId());
    Assertions.assertEquals(2, artifacts.stream().filter(Artifact::getIsProductArtifact).toList().size());
  }

  @Test
  void testUpdateMavenArtifactVersionCacheWithModel() {
    MavenArtifactVersion mockMavenArtifactVersion = getMockMavenArtifactVersion();
    Metadata mockMetadata = buildMocKMetadata();
    metadataService.updateMavenArtifactVersionCacheWithModel(mockMavenArtifactVersion, MOCK_RELEASED_VERSION,
        mockMetadata);
    List<MavenArtifactModel> additionalArtifacts = mockMavenArtifactVersion.getAdditionalArtifactsByVersion().get(
        MOCK_RELEASED_VERSION);
    List<MavenArtifactModel> productArtifacts = mockMavenArtifactVersion.getProductArtifactsByVersion().get(
        MOCK_RELEASED_VERSION);

    Assertions.assertEquals(MOCK_DOWNLOAD_URL, additionalArtifacts.get(0).getDownloadUrl());
    Assertions.assertEquals(1, additionalArtifacts.size());
    Assertions.assertTrue(CollectionUtils.isEmpty(productArtifacts));

    // Simulate add new artifact to the same version in additional list
    metadataService.updateMavenArtifactVersionCacheWithModel(mockMavenArtifactVersion, MOCK_RELEASED_VERSION,
        mockMetadata);
    Assertions.assertEquals(2, additionalArtifacts.size());
    Assertions.assertTrue(CollectionUtils.isEmpty(productArtifacts));

    mockMetadata.setProductArtifact(true);
    metadataService.updateMavenArtifactVersionCacheWithModel(mockMavenArtifactVersion, MOCK_RELEASED_VERSION,
        mockMetadata);
    Assertions.assertEquals(1, productArtifacts.size());
  }

  @Test
  void testUpdateMavenArtifactVersionForNonReleaseDevVersion() {
    Metadata mockMetadata = getMockMetadata();
    MavenArtifactVersion mockMavenArtifactVersion = getMockMavenArtifactVersion();
    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
      mockUtils.when(
          () -> MavenUtils.buildSnapShotMetadataFromVersion(mockMetadata, MOCK_SNAPSHOT_VERSION)).thenReturn(
          mockMetadata);
      metadataService.updateMavenArtifactVersionForNonReleaseDevVersion(mockMavenArtifactVersion, mockMetadata,
          MOCK_SNAPSHOT_VERSION);
      Assertions.assertEquals(1, mockMavenArtifactVersion.getProductArtifactsByVersion().entrySet().size());
      Assertions.assertEquals(1,
          mockMavenArtifactVersion.getProductArtifactsByVersion().get(MOCK_SNAPSHOT_VERSION).size());
    }
  }

  @Test
  void testUpdateMavenArtifactVersionFromMetadata() {
    Metadata mockMetadata = getMockMetadata();
    mockMetadata.setVersions(Set.of(MOCK_RELEASED_VERSION));
    MavenArtifactVersion mockMavenArtifactVersion = getMockMavenArtifactVersion();
    metadataService.updateMavenArtifactVersionFromMetadata(mockMavenArtifactVersion, mockMetadata);
    Assertions.assertEquals(1, mockMavenArtifactVersion.getProductArtifactsByVersion().entrySet().size());
    Assertions.assertTrue(
        CollectionUtils.isEmpty(mockMavenArtifactVersion.getAdditionalArtifactsByVersion().entrySet()));
    String snapshotVersion = "2.0.0-SNAPSHOT";
    mockMetadata.setVersions(Set.of(snapshotVersion, MOCK_RELEASED_VERSION));
    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
      mockUtils.when(() -> MavenUtils.buildSnapShotMetadataFromVersion(mockMetadata, snapshotVersion)).thenReturn(
          mockMetadata);
      metadataService.updateMavenArtifactVersionFromMetadata(mockMavenArtifactVersion, mockMetadata);
      Assertions.assertEquals(2, mockMavenArtifactVersion.getProductArtifactsByVersion().entrySet().size());
      Assertions.assertTrue(
          CollectionUtils.isEmpty(mockMavenArtifactVersion.getAdditionalArtifactsByVersion().entrySet()));

      mockMetadata.setProductArtifact(false);
      metadataService.updateMavenArtifactVersionFromMetadata(mockMavenArtifactVersion, mockMetadata);
      Assertions.assertEquals(2, mockMavenArtifactVersion.getAdditionalArtifactsByVersion().entrySet().size());
    }
  }

  @Test
  void testUpdateMavenArtifactVersionData() {
    Metadata mockMetadata = getMockMetadata();
    mockMetadata.setVersions(new HashSet<>());
    mockMetadata.setUrl(MOCK_MAVEN_URL);
    Set<Metadata> mockMetadataSet = Set.of(mockMetadata);
    MavenArtifactVersion mockMavenArtifactVersion = getMockMavenArtifactVersion();
    metadataService.updateMavenArtifactVersionData(mockMetadataSet,
        mockMavenArtifactVersion);
    Assertions.assertEquals(0, mockMavenArtifactVersion.getAdditionalArtifactsByVersion().size());
    Assertions.assertEquals(0, mockMavenArtifactVersion.getProductArtifactsByVersion().size());
    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
      mockUtils.when(() -> MavenUtils.getMetadataContentFromUrl(MOCK_MAVEN_URL)).thenReturn(getMockMetadataContent());
      metadataService.updateMavenArtifactVersionData(mockMetadataSet,
          mockMavenArtifactVersion);
      Assertions.assertEquals(2, mockMavenArtifactVersion.getProductArtifactsByVersion().size());
    }
  }
}
