package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.ArchivedArtifact;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.enums.DevelopmentVersion;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.VersionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VersionServiceImplTest extends BaseSetup {
  @Spy
  @InjectMocks
  private VersionServiceImpl versionService;

  @Mock
  private MavenArtifactVersionRepository mavenArtifactVersionRepository;

  @Mock
  private ProductJsonContentRepository productJsonContentRepository;

  @Mock
  private MetadataRepository metadataRepo;

  @Test
  void testGetArtifactsAndVersionToDisplay() {
    when(mavenArtifactVersionRepository.findByProductId(Mockito.anyString())).thenReturn(List.of());
    when(mavenArtifactVersionRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(new ArrayList<>());

    Assertions.assertTrue(CollectionUtils.isEmpty(
        versionService.getArtifactsAndVersionToDisplay(MOCK_PRODUCT_ID, false, MOCK_RELEASED_VERSION)));

    List<MavenArtifactVersion> proceededData = new ArrayList<>();

    MavenArtifactVersion mockModel = mockAdditionalMavenArtifactVersion(MOCK_RELEASED_VERSION,null);
    mockModel.setName(MOCK_PRODUCT_ID);
    mockModel.setDownloadUrl(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    proceededData.add(mockModel);

    when(mavenArtifactVersionRepository.findByProductId(Mockito.anyString())).thenReturn(proceededData);
    Assertions.assertTrue(ObjectUtils.isNotEmpty(
        versionService.getArtifactsAndVersionToDisplay(MOCK_PRODUCT_ID, false, MOCK_RELEASED_VERSION)));
  }

  @Test
  void testFindArchivedArtifactInfoBestMatchWithVersion() {
    ArchivedArtifact result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion(MOCK_RELEASED_VERSION,
        Collections.emptyList());
    Assertions.assertNull(result);
    ArchivedArtifact adobeArchivedArtifactVersion8 = new ArchivedArtifact("id","10.0.8", MOCK_GROUP_ID,
        "bpmn-connector",null);
    ArchivedArtifact adobeArchivedArtifactVersion9 = new ArchivedArtifact("id2","10.0.9", MOCK_GROUP_ID,
        "process-mining-connector", null);
    List<ArchivedArtifact> archivedArtifacts = new ArrayList<>();
    archivedArtifacts.add(adobeArchivedArtifactVersion8);
    archivedArtifacts.add(adobeArchivedArtifactVersion9);
    result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion(MOCK_RELEASED_VERSION,
        archivedArtifacts);
    Assertions.assertNull(result);
    result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion("10.0.7",
        archivedArtifacts);
    Assertions.assertEquals(adobeArchivedArtifactVersion8, result);
    ArchivedArtifact adobeArchivedArtifactVersion10 = new ArchivedArtifact("testId",MOCK_RELEASED_VERSION,
        MOCK_GROUP_ID,
        "adobe-sign-connector",null);
    archivedArtifacts.add(adobeArchivedArtifactVersion10);
    result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion(MOCK_RELEASED_VERSION,
        archivedArtifacts);
    Assertions.assertEquals(adobeArchivedArtifactVersion10.getArtifactId(), result.getArtifactId());
  }

  @Test
  void testGetVersionsForDesigner() {
    List<String> mockVersions = List.of("11.3.0-SNAPSHOT", "11.1.1", "11.1.0", "10.0.2");
    Metadata mockMetadata = getMockMetadata();
    mockMetadata.setArtifactId(MOCK_PRODUCT_ARTIFACT_ID);
    mockMetadata.setVersions(new HashSet<>());
    mockMetadata.getVersions().addAll(mockVersions);
    List<VersionAndUrlModel> result = versionService.getVersionsForDesigner(MOCK_PRODUCT_ID);
    Assertions.assertTrue(CollectionUtils.isEmpty(result));
    when(metadataRepo.findByProductId(MOCK_PRODUCT_ID)).thenReturn(List.of(mockMetadata));
    result = versionService.getVersionsForDesigner(MOCK_PRODUCT_ID);
    Assertions.assertEquals(result.stream().map(VersionAndUrlModel::getVersion).toList(), mockVersions);
    Assertions.assertTrue(result.get(0).getUrl().endsWith("/api/product-details/bpmn-statistic/11.3.0-SNAPSHOT/json"));
    Assertions.assertTrue(result.get(1).getUrl().endsWith("/api/product-details/bpmn-statistic/11.1.1/json"));
    Assertions.assertTrue(result.get(2).getUrl().endsWith("/api/product-details/bpmn-statistic/11.1.0/json"));
    Assertions.assertTrue(result.get(3).getUrl().endsWith("/api/product-details/bpmn-statistic/10.0.2/json"));
  }

  @Test
  void testGetProductJsonContentByIdAndVersion() {
    ProductJsonContent mockProductJsonContent = getMockProductJsonContent();
    mockProductJsonContent.setName(MOCK_PRODUCT_NAME);
    Mockito.when(productJsonContentRepository.findByProductIdAndVersion(anyString(), anyString()))
        .thenReturn(List.of(mockProductJsonContent));
    Map<String, Object> result = versionService.getProductJsonContentByIdAndVersion(MOCK_PRODUCT_ID,
        MOCK_RELEASED_VERSION);
    Assertions.assertEquals(MOCK_PRODUCT_NAME, result.get("name"));
  }

  @Test
  void testGetProductJsonContentByIdAndVersion_noResult() {
    Mockito.when(productJsonContentRepository.findByProductIdAndVersion(anyString(), anyString())).thenReturn(
        Collections.emptyList());
    Map<String, Object> result = versionService.getProductJsonContentByIdAndVersion(MOCK_PRODUCT_ID,
        MOCK_RELEASED_VERSION);
    Assertions.assertEquals(new HashMap<>(), result);
  }

  @Test
  void testGetAllExistingVersions() {
    List<MavenArtifactVersion> mavenArtifactVersions = new ArrayList<>();
    Assertions.assertTrue(CollectionUtils.isEmpty(VersionUtils.extractAllVersions(mavenArtifactVersions, false,
        StringUtils.EMPTY)));
    mavenArtifactVersions = getMockMavenArtifactVersionWithData();
    Assertions.assertTrue(ObjectUtils.isNotEmpty(VersionUtils.extractAllVersions(mavenArtifactVersions, true,
        StringUtils.EMPTY)));
    Assertions.assertTrue(CollectionUtils.isEmpty(VersionUtils.extractAllVersions(mavenArtifactVersions, false,
        StringUtils.EMPTY)));
  }

  @Test
  void testGetDownloadUrlFromExistingDataByArtifactIdAndVersion() {
    List<MavenArtifactVersion> existingData = getMockMavenArtifactVersion();

    existingData.add(getMockMavenArtifactVersionWithDownloadUrl());
    Assertions.assertNull(versionService.getDownloadUrlFromExistingDataByArtifactIdAndVersion(existingData,
        MOCK_SNAPSHOT_VERSION, List.of(MOCK_ARTIFACT_ID)));

    MavenArtifactVersion mockMavenArtifactVersion = getMockMavenArtifactVersionWithDownloadUrl();
    mockMavenArtifactVersion.getId().setProductVersion(MOCK_RELEASED_VERSION);

    existingData.clear();
    existingData.add(mockMavenArtifactVersion);
    Assertions.assertEquals(MOCK_DOWNLOAD_URL, versionService.getDownloadUrlFromExistingDataByArtifactIdAndVersion(
        existingData, MOCK_RELEASED_VERSION, List.of(MOCK_ARTIFACT_ID)));
  }

  @Test
  void testGetLatestVersionArtifactDownloadUrl() {
    Assertions.assertEquals(StringUtils.EMPTY, versionService.getLatestVersionArtifactDownloadUrl(MOCK_PRODUCT_ID,
        DevelopmentVersion.LATEST.getCode(), MOCK_ARTIFACT_DOWNLOAD_FILE));

    when(metadataRepo.findByProductIdAndArtifactId(MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID)).thenReturn(
        List.of(getMockMetadataWithVersions()));
    Assertions.assertEquals(StringUtils.EMPTY, versionService.getLatestVersionArtifactDownloadUrl(MOCK_PRODUCT_ID,
        DevelopmentVersion.LATEST.getCode(), MOCK_ARTIFACT_DOWNLOAD_FILE));

    List<MavenArtifactVersion> mockMavenArtifactVersion = getMockMavenArtifactVersion();
    List<MavenArtifactVersion> mockMavenArtifactVersion2 = getMockMavenArtifactVersion();

    MavenArtifactVersion mockMavenArtifactVersionWithDownloadUrl = getMockMavenArtifactVersionWithDownloadUrl();
    mockMavenArtifactVersionWithDownloadUrl.getId().setProductVersion("10.0.10");
    mockMavenArtifactVersion2.add(mockMavenArtifactVersionWithDownloadUrl);

    when(mavenArtifactVersionRepository.findByProductId(anyString())).thenReturn(mockMavenArtifactVersion,
        mockMavenArtifactVersion2);

    Assertions.assertEquals(StringUtils.EMPTY, versionService.getLatestVersionArtifactDownloadUrl(MOCK_PRODUCT_ID,
        DevelopmentVersion.LATEST.getCode(), MOCK_ARTIFACT_DOWNLOAD_FILE));

    Assertions.assertEquals(MOCK_DOWNLOAD_URL, versionService.getLatestVersionArtifactDownloadUrl(MOCK_PRODUCT_ID,
        DevelopmentVersion.LATEST.getCode(), MOCK_ARTIFACT_DOWNLOAD_FILE));
  }
}
