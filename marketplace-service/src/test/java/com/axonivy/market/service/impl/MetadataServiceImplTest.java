package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.entity.key.MavenArtifactKey;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.util.MavenUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

  @Test
  void testUpdateArtifactAndMetaDataForProduct() {
    ProductJsonContent mockProductJsonContent = getMockProductJsonContent();
    mockProductJsonContent.setProductId(MOCK_PRODUCT_ID);

    Artifact mockArtifact = getMockArtifact();
    Metadata mockMetadata = buildMockMetadata();
    Product mockProduct = getMockProduct();
    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
      mockUtils.when(() -> MavenUtils.getMetadataContentFromUrl(ArgumentMatchers.anyString())).thenReturn(null);
      mockUtils.when(() -> MavenUtils.convertArtifactsToMetadataSet(any(), any())).thenReturn(Set.of(mockMetadata));

      metadataService.updateArtifactAndMetadata(mockProduct.getId(), List.of(MOCK_RELEASED_VERSION),
          List.of(mockArtifact));

      verify(mavenArtifactVersionRepo, times(1)).saveAll(any());
      verify(metadataRepo, times(1)).saveAll(any());
    }
  }

  @Test
  void testUpdateMavenArtifactVersionCacheWithModel() {
    List<MavenArtifactVersion> mockMavenArtifactVersion = new ArrayList<>();
    Metadata mockMetadata = buildMockMetadata();
    metadataService.updateMavenArtifactVersionWithModel(mockMavenArtifactVersion, MOCK_RELEASED_VERSION,
        mockMetadata);

    Assertions.assertEquals(MOCK_DOWNLOAD_URL, mockMavenArtifactVersion.get(0).getDownloadUrl());
    Assertions.assertEquals(1, mockMavenArtifactVersion.size());
    Assertions.assertTrue(mockMavenArtifactVersion.get(0).getId().isAdditionalVersion());

    // Simulate add one new duplicated artifact to the same version in additional list
    metadataService.updateMavenArtifactVersionWithModel(mockMavenArtifactVersion, MOCK_RELEASED_VERSION,
        mockMetadata);
    Assertions.assertEquals(1, mockMavenArtifactVersion.size());

    List<MavenArtifactVersion> productArtifacts = getProductArtifacts(mockMavenArtifactVersion);

    Assertions.assertTrue(CollectionUtils.isEmpty(productArtifacts));

    // Simulate add one new non-duplicated artifact to the same version in additional list
    mockMetadata.setArtifactId(MOCK_DEMO_ARTIFACT_ID);
    metadataService.updateMavenArtifactVersionWithModel(mockMavenArtifactVersion, MOCK_RELEASED_VERSION,
        mockMetadata);

    List<MavenArtifactVersion> additionalArtifacts = getAdditionalProductArtifacts(mockMavenArtifactVersion);

    Assertions.assertEquals(2, additionalArtifacts.size());

    productArtifacts = getProductArtifacts(mockMavenArtifactVersion);

    Assertions.assertTrue(CollectionUtils.isEmpty(productArtifacts));

    mockMetadata.setProductArtifact(true);
    metadataService.updateMavenArtifactVersionWithModel(mockMavenArtifactVersion, MOCK_RELEASED_VERSION, mockMetadata);

    productArtifacts = getProductArtifacts(mockMavenArtifactVersion);

    Assertions.assertEquals(1, productArtifacts.size());
  }

  @Test
  void testUpdateMavenArtifactVersionForNonReleaseDevVersion() {
    Metadata mockMetadata = getMockMetadata();
    List<MavenArtifactVersion> mockMavenArtifactVersion = getMockMavenArtifactVersion();
    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
      MavenArtifactKey mavenArtifactKey = MavenArtifactKey.builder().productVersion(MOCK_SNAPSHOT_VERSION).build();

      mockUtils.when(() -> MavenUtils.buildSnapShotMetadataFromVersion(mockMetadata, MOCK_SNAPSHOT_VERSION))
          .thenReturn(mockMetadata);

      mockUtils.when(() -> MavenUtils.buildMavenArtifactVersionFromMetadata(MOCK_SNAPSHOT_VERSION, mockMetadata))
          .thenReturn(MavenArtifactVersion.builder().id(mavenArtifactKey).build());

      metadataService.updateMavenArtifactVersionForNonReleaseDevVersion(mockMavenArtifactVersion, mockMetadata,
          MOCK_SNAPSHOT_VERSION);
      Assertions.assertEquals(1, mockMavenArtifactVersion.size());
    }
  }

  @Test
  void testUpdateMavenArtifactVersionFromMetadata() {
    Metadata mockMetadata = getMockMetadata();
    mockMetadata.setVersions(Set.of(MOCK_RELEASED_VERSION));
    List<MavenArtifactVersion> mockMavenArtifactVersion = getMockMavenArtifactVersion();
    metadataService.updateMavenArtifactVersionFromMetadata(mockMavenArtifactVersion, mockMetadata);

    Assertions.assertEquals(1, getProductArtifacts(mockMavenArtifactVersion).size());
    Assertions.assertTrue(CollectionUtils.isEmpty(getAdditionalProductArtifacts(mockMavenArtifactVersion)));

    String snapshotVersion = "2.0.0-SNAPSHOT";
    mockMetadata.setVersions(Set.of(snapshotVersion, MOCK_RELEASED_VERSION));
    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
      mockUtils.when(() -> MavenUtils.buildSnapShotMetadataFromVersion(mockMetadata, snapshotVersion)).thenReturn(
          mockMetadata);
      mockUtils.when(() -> MavenUtils.buildMavenArtifactVersionFromMetadata(anyString(), any()))
          .thenReturn(
              mockMavenArtifactVersion(MOCK_SNAPSHOT_VERSION, null),
              mockMavenArtifactVersion(MOCK_SNAPSHOT_VERSION, null),
              mockAdditionalMavenArtifactVersion(MOCK_SNAPSHOT_VERSION, null),
              mockAdditionalMavenArtifactVersion(MOCK_RELEASED_VERSION, null)
          );

      metadataService.updateMavenArtifactVersionFromMetadata(mockMavenArtifactVersion, mockMetadata);
      Assertions.assertEquals(2, getProductArtifacts(mockMavenArtifactVersion).size());
      Assertions.assertTrue(CollectionUtils.isEmpty(getAdditionalProductArtifacts(mockMavenArtifactVersion)));

      metadataService.updateMavenArtifactVersionFromMetadata(mockMavenArtifactVersion, mockMetadata);
      Assertions.assertEquals(2, getAdditionalProductArtifacts(mockMavenArtifactVersion).size());
    }
  }

  @Test
  void testUpdateMavenArtifactVersionData() {
    Metadata mockMetadata = getMockMetadata();
    mockMetadata.setVersions(new HashSet<>());
    mockMetadata.setUrl(MOCK_MAVEN_URL);
    Set<Metadata> mockMetadataSet = Set.of(mockMetadata);
    List<MavenArtifactVersion> mockMavenArtifactVersion = getMockMavenArtifactVersion();
    metadataService.updateMavenArtifactVersionData(mockMetadataSet, MOCK_PRODUCT_ID);
    Assertions.assertEquals(0, getProductArtifacts(mockMavenArtifactVersion).size());
    Assertions.assertEquals(0, getAdditionalProductArtifacts(mockMavenArtifactVersion).size());

    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
      mockUtils.when(() -> MavenUtils.getMetadataContentFromUrl(MOCK_MAVEN_URL)).thenReturn(getMockMetadataContent());
      mockUtils.when(() -> MavenUtils.buildMavenArtifactVersionFromMetadata(anyString(), any()))
          .thenReturn(mockMavenArtifactVersion(MOCK_SNAPSHOT_VERSION,null),
              mockMavenArtifactVersion(MOCK_RELEASED_VERSION,null));

      ArgumentCaptor<List<MavenArtifactVersion>> captor = ArgumentCaptor.forClass(List.class);


      metadataService.updateMavenArtifactVersionData(mockMetadataSet, MOCK_PRODUCT_ID);

      verify(mavenArtifactVersionRepo, times(2)).saveAll(captor.capture());
      List<MavenArtifactVersion> savedArtifactVersion = captor.getValue();
      assertNotNull(savedArtifactVersion);
      Assertions.assertEquals(2, getProductArtifacts(savedArtifactVersion).size());
    }
  }

  private List<MavenArtifactVersion> getProductArtifacts(List<MavenArtifactVersion> models) {
    return models.stream().filter(model -> !model.getId().isAdditionalVersion())
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private List<MavenArtifactVersion> getAdditionalProductArtifacts(List<MavenArtifactVersion> models) {
    return models.stream().filter(model -> model.getId().isAdditionalVersion())
        .collect(Collectors.toCollection(ArrayList::new));
  }
}
