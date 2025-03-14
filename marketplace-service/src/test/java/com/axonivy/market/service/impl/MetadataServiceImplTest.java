//package com.axonivy.market.service.impl;
//
//import com.axonivy.market.BaseSetup;
//import com.axonivy.market.entity.Artifact;
//import com.axonivy.market.entity.MavenArtifactModel;
//import com.axonivy.market.entity.MavenArtifactVersion;
//import com.axonivy.market.entity.Metadata;
//import com.axonivy.market.entity.Product;
//import com.axonivy.market.entity.ProductJsonContent;
//import com.axonivy.market.repository.MavenArtifactVersionRepository;
//import com.axonivy.market.repository.MetadataRepository;
//import com.axonivy.market.repository.ProductJsonContentRepository;
//import com.axonivy.market.repository.ProductRepository;
//import com.axonivy.market.util.MavenUtils;
//import lombok.extern.log4j.Log4j2;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.ArgumentMatchers;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.util.CollectionUtils;
//
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.times;
//import static org.mockito.Mockito.verify;
//
//@Log4j2
//@ExtendWith(MockitoExtension.class)
//class MetadataServiceImplTest extends BaseSetup {
//  @InjectMocks
//  MetadataServiceImpl metadataService;
//  @Mock
//  ProductRepository productRepo;
//  @Mock
//  ProductJsonContentRepository productJsonRepo;
//  @Mock
//  private MetadataRepository metadataRepo;
//  @Mock
//  private MavenArtifactVersionRepository mavenArtifactVersionRepo;
//
//  @Test
//  void testUpdateArtifactAndMetaDataForProduct() {
//    ProductJsonContent mockProductJsonContent = getMockProductJsonContent();
//    mockProductJsonContent.setProductId(MOCK_PRODUCT_ID);
//
//    Artifact mockArtifact = getMockArtifact();
//    Metadata mockMetadata = buildMockMetadata();
//    Product mockProduct = getMockProduct();
//    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
//      mockUtils.when(() -> MavenUtils.getMetadataContentFromUrl(ArgumentMatchers.anyString())).thenReturn(null);
//      mockUtils.when(() -> MavenUtils.convertArtifactsToMetadataSet(any(), any())).thenReturn(Set.of(mockMetadata));
//
//      metadataService.updateArtifactAndMetadata(mockProduct.getId(), List.of(MOCK_RELEASED_VERSION),
//          List.of(mockArtifact));
//
//      verify(mavenArtifactVersionRepo, times(1)).save(any());
//      verify(metadataRepo, times(1)).saveAll(any());
//    }
//  }
//
//  @Test
//  void testGetArtifactsFromNonSyncedVersion() {
//    Mockito.when(
//        productJsonRepo.findByProductIdAndVersionIn(MOCK_PRODUCT_ID, List.of(MOCK_RELEASED_VERSION))).thenReturn(
//        List.of(getMockProductJsonContent()));
//    Set<Artifact> artifacts = metadataService.getArtifactsFromNonSyncedVersion(MOCK_PRODUCT_ID,
//        Collections.emptyList());
//    Assertions.assertTrue(CollectionUtils.isEmpty(artifacts));
//    Mockito.verify(productJsonRepo, Mockito.never()).findByProductIdAndVersionIn(Mockito.anyString(), any());
//    artifacts = metadataService.getArtifactsFromNonSyncedVersion(MOCK_PRODUCT_ID, List.of(MOCK_RELEASED_VERSION));
//    Assertions.assertEquals(2, artifacts.size());
//    Assertions.assertEquals("bpmn-statistic-demo", artifacts.iterator().next().getArtifactId());
//    Assertions.assertEquals(2, artifacts.stream().filter(Artifact::getIsProductArtifact).toList().size());
//  }
//
//  @Test
//  void testUpdateMavenArtifactVersionCacheWithModel() {
//    MavenArtifactVersion mockMavenArtifactVersion = getMockMavenArtifactVersion();
//    Metadata mockMetadata = buildMockMetadata();
//    metadataService.updateMavenArtifactVersionWithModel(mockMavenArtifactVersion, MOCK_RELEASED_VERSION,
//        mockMetadata);
//
//    List<MavenArtifactModel> additionalArtifacts = mockMavenArtifactVersion.getAdditionalArtifactsByVersion();
//    List<MavenArtifactModel> productArtifacts = mockMavenArtifactVersion.getProductArtifactsByVersion();
//
//    Assertions.assertEquals(MOCK_DOWNLOAD_URL, additionalArtifacts.get(0).getDownloadUrl());
//    Assertions.assertEquals(1, additionalArtifacts.size());
//    Assertions.assertTrue(CollectionUtils.isEmpty(productArtifacts));
//
//    // Simulate add one new duplicated artifact to the same version in additional list
//    metadataService.updateMavenArtifactVersionWithModel(mockMavenArtifactVersion, MOCK_RELEASED_VERSION,
//        mockMetadata);
//    Assertions.assertEquals(1, additionalArtifacts.size());
//    productArtifacts = mockMavenArtifactVersion.getProductArtifactsByVersion();
//    Assertions.assertTrue(CollectionUtils.isEmpty(productArtifacts));
//
//    // Simulate add one new non-duplicated artifact to the same version in additional list
//    mockMetadata.setArtifactId(MOCK_DEMO_ARTIFACT_ID);
//    metadataService.updateMavenArtifactVersionWithModel(mockMavenArtifactVersion, MOCK_RELEASED_VERSION,
//        mockMetadata);
//    Assertions.assertEquals(2, additionalArtifacts.size());
//    productArtifacts = mockMavenArtifactVersion.getProductArtifactsByVersion();
//    Assertions.assertTrue(CollectionUtils.isEmpty(productArtifacts));
//
//    mockMetadata.setProductArtifact(true);
//    metadataService.updateMavenArtifactVersionWithModel(mockMavenArtifactVersion, MOCK_RELEASED_VERSION, mockMetadata);
//    productArtifacts = mockMavenArtifactVersion.getProductArtifactsByVersion();
//    Assertions.assertEquals(1, productArtifacts.size());
//  }
//
//  @Test
//  void testUpdateMavenArtifactVersionForNonReleaseDevVersion() {
//    Metadata mockMetadata = getMockMetadata();
//    MavenArtifactVersion mockMavenArtifactVersion = getMockMavenArtifactVersion();
//    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
//      mockUtils.when(
//              () -> MavenUtils.buildSnapShotMetadataFromVersion(mockMetadata, MOCK_SNAPSHOT_VERSION))
//          .thenReturn(mockMetadata);
//      mockUtils.when(() -> MavenUtils.buildMavenArtifactModelFromMetadata(MOCK_SNAPSHOT_VERSION, mockMetadata))
//          .thenReturn(MavenArtifactModel.builder().productVersion(MOCK_SNAPSHOT_VERSION).build());
//
//      metadataService.updateMavenArtifactVersionForNonReleaseDevVersion(mockMavenArtifactVersion, mockMetadata,
//          MOCK_SNAPSHOT_VERSION);
//      Assertions.assertEquals(1, mockMavenArtifactVersion.getProductArtifactsByVersion().size());
//    }
//  }
//
//  @Test
//  void testUpdateMavenArtifactVersionFromMetadata() {
//    Metadata mockMetadata = getMockMetadata();
//    mockMetadata.setVersions(Set.of(MOCK_RELEASED_VERSION));
//    MavenArtifactVersion mockMavenArtifactVersion = getMockMavenArtifactVersion();
//    metadataService.updateMavenArtifactVersionFromMetadata(mockMavenArtifactVersion, mockMetadata);
//    Assertions.assertEquals(1, mockMavenArtifactVersion.getProductArtifactsByVersion().size());
//    Assertions.assertTrue(
//        CollectionUtils.isEmpty(mockMavenArtifactVersion.getAdditionalArtifactsByVersion()));
//
//    String snapshotVersion = "2.0.0-SNAPSHOT";
//    mockMetadata.setVersions(Set.of(snapshotVersion, MOCK_RELEASED_VERSION));
//    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
//      mockUtils.when(() -> MavenUtils.buildSnapShotMetadataFromVersion(mockMetadata, snapshotVersion)).thenReturn(
//          mockMetadata);
//
//      mockUtils.when(() -> MavenUtils.buildMavenArtifactModelFromMetadata(anyString(), any()))
//          .thenReturn(MavenArtifactModel.builder().productVersion(MOCK_SNAPSHOT_VERSION).build());
//
//      metadataService.updateMavenArtifactVersionFromMetadata(mockMavenArtifactVersion, mockMetadata);
//      Assertions.assertEquals(2, mockMavenArtifactVersion.getProductArtifactsByVersion().size());
//      Assertions.assertTrue(
//          CollectionUtils.isEmpty(
//              Collections.unmodifiableList(mockMavenArtifactVersion.getAdditionalArtifactsByVersion())));
//
//      mockMetadata.setProductArtifact(false);
//      metadataService.updateMavenArtifactVersionFromMetadata(mockMavenArtifactVersion, mockMetadata);
//      Assertions.assertEquals(2, mockMavenArtifactVersion.getAdditionalArtifactsByVersion().size());
//    }
//  }
//
//  @Test
//  void testUpdateMavenArtifactVersionData() {
//    Metadata mockMetadata = getMockMetadata();
//    mockMetadata.setVersions(new HashSet<>());
//    mockMetadata.setUrl(MOCK_MAVEN_URL);
//    Set<Metadata> mockMetadataSet = Set.of(mockMetadata);
//    MavenArtifactVersion mockMavenArtifactVersion = getMockMavenArtifactVersion();
//    metadataService.updateMavenArtifactVersionData(mockMetadataSet, MOCK_PRODUCT_ID);
//    Assertions.assertEquals(0, mockMavenArtifactVersion.getAdditionalArtifactsByVersion().size());
//    Assertions.assertEquals(0, mockMavenArtifactVersion.getProductArtifactsByVersion().size());
//
//    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
//      mockUtils.when(() -> MavenUtils.getMetadataContentFromUrl(MOCK_MAVEN_URL)).thenReturn(getMockMetadataContent());
//      mockUtils.when(() -> MavenUtils.buildMavenArtifactModelFromMetadata(anyString(), any()))
//          .thenReturn(MavenArtifactModel.builder().productVersion(MOCK_SNAPSHOT_VERSION).build());
//
//      ArgumentCaptor<MavenArtifactVersion> captor = ArgumentCaptor.forClass(MavenArtifactVersion.class);
//
//
//      metadataService.updateMavenArtifactVersionData(mockMetadataSet, MOCK_PRODUCT_ID);
//
//      verify(mavenArtifactVersionRepo, times(2)).save(captor.capture());
//      MavenArtifactVersion savedArtifactVersion = captor.getValue();
//      assertNotNull(savedArtifactVersion);
//      Assertions.assertEquals(2, savedArtifactVersion.getProductArtifactsByVersion().size());
//    }
//  }
//}
