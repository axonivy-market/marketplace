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
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.util.MavenUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.*;

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
  FileDownloadService fileDownloadService;

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

    Assertions.assertEquals(MOCK_DOWNLOAD_URL, mockMavenArtifactVersion.get(0).getDownloadUrl(),
        "Maven artifact download URL should match expected mock URL after update");
    Assertions.assertEquals(1, mockMavenArtifactVersion.size(),
        "Maven artifact version list size should match 1 after update");
    Assertions.assertTrue(mockMavenArtifactVersion.get(0).getId().isAdditionalVersion(),
        "Maven artifact ID should be additional version after update");

    // Simulate add one new duplicated artifact to the same version in additional list
    metadataService.updateMavenArtifactVersionWithModel(mockMavenArtifactVersion, MOCK_RELEASED_VERSION,
        mockMetadata);
    Assertions.assertEquals(1, mockMavenArtifactVersion.size(),
        "Maven artifact version list size should still match 1 after adding new duplicated artifact to the same version in additional list");

    List<MavenArtifactVersion> productArtifacts = getProductArtifacts(mockMavenArtifactVersion);

    Assertions.assertTrue(CollectionUtils.isEmpty(productArtifacts),
        "Maven artifact version list size should still match 1 after adding new duplicated artifact to the same version in additional list");

    // Simulate add one new non-duplicated artifact to the same version in additional list
    mockMetadata.setArtifactId(MOCK_DEMO_ARTIFACT_ID);
    metadataService.updateMavenArtifactVersionWithModel(mockMavenArtifactVersion, MOCK_RELEASED_VERSION,
        mockMetadata);

    List<MavenArtifactVersion> additionalArtifacts = getAdditionalProductArtifacts(mockMavenArtifactVersion);

    Assertions.assertEquals(2, additionalArtifacts.size(),
        "Maven artifact version list size should match 2 after adding new artifact to the same version in " +
            "additional list");

    productArtifacts = getProductArtifacts(mockMavenArtifactVersion);

    Assertions.assertTrue(CollectionUtils.isEmpty(productArtifacts),
        "Product artifact list should be empty");

    mockMetadata.setProductArtifact(true);
    metadataService.updateMavenArtifactVersionWithModel(mockMavenArtifactVersion, MOCK_RELEASED_VERSION, mockMetadata);

    productArtifacts = getProductArtifacts(mockMavenArtifactVersion);

    Assertions.assertEquals(1, productArtifacts.size(),
        "Product artifact list size should match 1");
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
      Assertions.assertEquals(1, mockMavenArtifactVersion.size(),
          "Maven artifact version list size should be 1 after update for non release dev version");
    }
  }

  @Test
  void testUpdateMavenArtifactVersionFromMetadata() {
    Metadata mockMetadata = getMockMetadata();
    mockMetadata.setVersions(Set.of(MOCK_RELEASED_VERSION));
    List<MavenArtifactVersion> mockMavenArtifactVersion = getMockMavenArtifactVersion();

    metadataService.updateMavenArtifactVersionFromMetadata(mockMavenArtifactVersion, mockMetadata);

    Assertions.assertEquals(1, getProductArtifacts(mockMavenArtifactVersion).size(),
        "Expected exactly one product artifact when only released version exists in metadata");
    Assertions.assertTrue(CollectionUtils.isEmpty(getAdditionalProductArtifacts(mockMavenArtifactVersion)),
        "Expected no additional product artifacts when only released version exists in metadata");

    String snapshotVersion = "2.0.0-SNAPSHOT";
    mockMetadata.setVersions(Set.of(snapshotVersion, MOCK_RELEASED_VERSION));

    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
      mockUtils.when(() -> MavenUtils.buildSnapShotMetadataFromVersion(mockMetadata, snapshotVersion))
          .thenReturn(mockMetadata);
      mockUtils.when(() -> MavenUtils.buildMavenArtifactVersionFromMetadata(anyString(), any()))
          .thenReturn(
              mockMavenArtifactVersion(MOCK_SNAPSHOT_VERSION, null),
              mockMavenArtifactVersion(MOCK_SNAPSHOT_VERSION, null),
              mockAdditionalMavenArtifactVersion(MOCK_SNAPSHOT_VERSION, null),
              mockAdditionalMavenArtifactVersion(MOCK_RELEASED_VERSION, null)
          );

      metadataService.updateMavenArtifactVersionFromMetadata(mockMavenArtifactVersion, mockMetadata);
      Assertions.assertEquals(2, getProductArtifacts(mockMavenArtifactVersion).size(),
          "Expected two product artifacts (released + snapshot) after adding snapshot version");
      Assertions.assertTrue(CollectionUtils.isEmpty(getAdditionalProductArtifacts(mockMavenArtifactVersion)),
          "Expected no additional product artifacts after first snapshot update");

      metadataService.updateMavenArtifactVersionFromMetadata(mockMavenArtifactVersion, mockMetadata);
      Assertions.assertEquals(2, getAdditionalProductArtifacts(mockMavenArtifactVersion).size(),
          "Expected two additional product artifacts to be created after second snapshot update");
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

    Assertions.assertEquals(0, getProductArtifacts(mockMavenArtifactVersion).size(),
        "Expected no product artifacts when metadata contains no versions");
    Assertions.assertEquals(0, getAdditionalProductArtifacts(mockMavenArtifactVersion).size(),
        "Expected no additional product artifacts when metadata contains no versions");

    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
      when(fileDownloadService.getFileAsString(MOCK_MAVEN_URL))
          .thenReturn(getMockMetadataContent());

      mockUtils.when(() -> MavenUtils.buildMavenArtifactVersionFromMetadata(anyString(), any()))
          .thenReturn(
              mockMavenArtifactVersion(MOCK_SNAPSHOT_VERSION, null),
              mockMavenArtifactVersion(MOCK_RELEASED_VERSION, null)
          );

      ArgumentCaptor<List<MavenArtifactVersion>> captor = ArgumentCaptor.forClass(List.class);

      metadataService.updateMavenArtifactVersionData(mockMetadataSet, MOCK_PRODUCT_ID);

      verify(mavenArtifactVersionRepo, times(2)).saveAll(captor.capture());
      List<MavenArtifactVersion> savedArtifactVersion = captor.getValue();

      assertNotNull(savedArtifactVersion,
          "Expected saved artifact versions list to be initialized after update");
      Assertions.assertEquals(2, getProductArtifacts(savedArtifactVersion).size(),
          "Expected two product artifacts (snapshot + released) to be saved from metadata");
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
