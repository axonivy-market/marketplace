package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.bo.ArchivedArtifact;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.util.MavenUtils;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VersionServiceImplTest extends BaseSetup {
  @Spy
  @InjectMocks
  private VersionServiceImpl versionService;

  @Mock
  private GHAxonIvyProductRepoService gitHubService;

  @Mock
  private MavenArtifactVersionRepository mavenArtifactVersionRepository;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductJsonContentRepository productJsonContentRepository;

  @Mock
  private ProductModuleContentRepository productModuleContentRepository;

  @Mock
  private MetadataRepository metadataRepo;

  @Test
  void testGetArtifactsAndVersionToDisplay() {
    when(mavenArtifactVersionRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
    when(mavenArtifactVersionRepository.findById(MOCK_PRODUCT_ID)).thenReturn(
        Optional.ofNullable(MavenArtifactVersion.builder().productId(MOCK_PRODUCT_ID).productArtifactsByVersion(
            new HashMap<>()).additionalArtifactsByVersion(new HashMap<>()).build()));
    Assertions.assertTrue(CollectionUtils.isEmpty(
        versionService.getArtifactsAndVersionToDisplay(MOCK_PRODUCT_ID, false, MOCK_RELEASED_VERSION)));
    MavenArtifactVersion proceededData =
        MavenArtifactVersion.builder().productArtifactsByVersion(new HashMap<>()).additionalArtifactsByVersion(
            new HashMap<>()).build();
    proceededData.getProductArtifactsByVersion().put(MOCK_RELEASED_VERSION, new ArrayList<>());
    proceededData.getAdditionalArtifactsByVersion().put(MOCK_RELEASED_VERSION, new ArrayList<>());
    MavenArtifactModel mockModel = new MavenArtifactModel();
    mockModel.setName(MOCK_PRODUCT_ID);
    mockModel.setDownloadUrl(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    proceededData.getAdditionalArtifactsByVersion().put(MOCK_RELEASED_VERSION, List.of(mockModel));
    when(mavenArtifactVersionRepository.findById(Mockito.anyString())).thenReturn(Optional.of(proceededData));
    Assertions.assertTrue(ObjectUtils.isNotEmpty(
        versionService.getArtifactsAndVersionToDisplay(MOCK_PRODUCT_ID, false, MOCK_RELEASED_VERSION)));
  }


  @Test
  void testGetMavenArtifactsFromProductJsonByVersion() {
    when(productJsonContentRepository.findByProductIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION)).thenReturn(
        Collections.emptyList());
    List<Artifact> results = versionService.getMavenArtifactsFromProductJsonByTag(MOCK_RELEASED_VERSION,
        MOCK_PRODUCT_ID);
    Assertions.assertTrue(CollectionUtils.isEmpty(results));
    when(productJsonContentRepository.findByProductIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION)).thenReturn(
        List.of(getMockProductJsonContent()));
    results = versionService.getMavenArtifactsFromProductJsonByTag(MOCK_RELEASED_VERSION, MOCK_PRODUCT_ID);
    Assertions.assertEquals(2, results.size());
  }


  @Test
  void testFindArchivedArtifactInfoBestMatchWithVersion() {
    ArchivedArtifact result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion(MOCK_RELEASED_VERSION,
        Collections.emptyList());
    Assertions.assertNull(result);
    ArchivedArtifact adobeArchivedArtifactVersion8 = new ArchivedArtifact("10.0.8", MOCK_GROUP_ID,
        "bpmn-connector");
    ArchivedArtifact adobeArchivedArtifactVersion9 = new ArchivedArtifact("10.0.9", MOCK_GROUP_ID,
        "process-mining-connector");
    List<ArchivedArtifact> archivedArtifacts = new ArrayList<>();
    archivedArtifacts.add(adobeArchivedArtifactVersion8);
    archivedArtifacts.add(adobeArchivedArtifactVersion9);
    result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion(MOCK_RELEASED_VERSION,
        archivedArtifacts);
    Assertions.assertNull(result);
    result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion("10.0.7",
        archivedArtifacts);
    Assertions.assertEquals(adobeArchivedArtifactVersion8, result);
    ArchivedArtifact adobeArchivedArtifactVersion10 = new ArchivedArtifact(MOCK_RELEASED_VERSION, MOCK_GROUP_ID,
        "adobe-sign-connector");
    archivedArtifacts.add(adobeArchivedArtifactVersion10);
    result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion(MOCK_RELEASED_VERSION,
        archivedArtifacts);
    Assertions.assertEquals(adobeArchivedArtifactVersion10.getArtifactId(), result.getArtifactId());
  }

  @Test
  void testGetVersionsForDesigner() {
    MavenArtifactVersion mockMavenArtifactVersion = new MavenArtifactVersion();
    List<String> mockVersions = List.of("11.3.0-SNAPSHOT", "11.1.1", "11.1.0", "10.0.2");
    for (String version : mockVersions) {
      mockMavenArtifactVersion.getProductArtifactsByVersion().put(version, new ArrayList<>());
    }
    when(mavenArtifactVersionRepository.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(mockMavenArtifactVersion));
    List<VersionAndUrlModel> result = versionService.getVersionsForDesigner(MOCK_PRODUCT_ID);
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
    Map<String, Object> result = versionService.getProductJsonContentByIdAndTag(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
    Assertions.assertEquals(MOCK_PRODUCT_NAME, result.get("name"));
  }

  @Test
  void testGetProductJsonContentByIdAndVersion_noResult() {
    Mockito.when(productJsonContentRepository.findByProductIdAndVersion(anyString(), anyString())).thenReturn(
        Collections.emptyList());
    Map<String, Object> result = versionService.getProductJsonContentByIdAndTag(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
    Assertions.assertEquals(new HashMap<>(), result);
  }

  @Test
  void testGetPersistedVersions() {
    Assertions.assertTrue(CollectionUtils.isEmpty(versionService.getPersistedVersions(MOCK_PRODUCT_ID)));
    Product mocProduct = new Product();
    mocProduct.setId(MOCK_PRODUCT_ID);
    mocProduct.setReleasedVersions(List.of(MOCK_RELEASED_VERSION));
    when(productRepository.findById(MOCK_PRODUCT_ID)).thenReturn(Optional.of(mocProduct));
    Assertions.assertTrue(ObjectUtils.isNotEmpty(versionService.getPersistedVersions(MOCK_PRODUCT_ID)));
    Assertions.assertEquals(MOCK_RELEASED_VERSION, versionService.getPersistedVersions(MOCK_PRODUCT_ID).get(0));
  }

  @Test
  void testGetAllExistingVersions() {
    MavenArtifactVersion mockMavenArtifactVersion = new MavenArtifactVersion();
    Assertions.assertTrue(CollectionUtils.isEmpty(MavenUtils.getAllExistingVersions(mockMavenArtifactVersion, false,
        StringUtils.EMPTY)));
    Map<String, List<MavenArtifactModel>> mockArtifactModelsByVersion = new HashMap<>();
    mockArtifactModelsByVersion.put(MOCK_SNAPSHOT_VERSION, new ArrayList<>());
    mockMavenArtifactVersion.setProductArtifactsByVersion(mockArtifactModelsByVersion);
    Assertions.assertTrue(ObjectUtils.isNotEmpty(MavenUtils.getAllExistingVersions(mockMavenArtifactVersion, true,
        StringUtils.EMPTY)));
    Assertions.assertTrue(CollectionUtils.isEmpty(MavenUtils.getAllExistingVersions(mockMavenArtifactVersion, false,
        StringUtils.EMPTY)));
  }

  @Test
  void testGetLatestVersionOfArtifactByVersionRequest() {
    Metadata artifactMetadata = getMockMetadataWithVersions();
    Assertions.assertEquals(MOCK_SPRINT_RELEASED_VERSION,
        versionService.getLatestVersionOfArtifactByVersionRequest(artifactMetadata, "dev"));
    Assertions.assertEquals(MOCK_SPRINT_RELEASED_VERSION,
        versionService.getLatestVersionOfArtifactByVersionRequest(artifactMetadata, "sprint"));
    Assertions.assertEquals(MOCK_SPRINT_RELEASED_VERSION,
        versionService.getLatestVersionOfArtifactByVersionRequest(artifactMetadata, "nightly"));
    Assertions.assertEquals(MOCK_RELEASED_VERSION,
        versionService.getLatestVersionOfArtifactByVersionRequest(artifactMetadata, "latest"));

    artifactMetadata.setVersions(null);
    String result = versionService.getLatestVersionOfArtifactByVersionRequest(null, MOCK_RELEASED_VERSION);
    Assertions.assertTrue(StringUtils.isEmpty(result));
    Assertions.assertTrue(StringUtils.isEmpty(
        versionService.getLatestVersionOfArtifactByVersionRequest(artifactMetadata, MOCK_RELEASED_VERSION)));

    artifactMetadata.setVersions(
        Set.of("1.0.0", "1.0.0-SNAPSHOT", "2.0.0", "2.0.0-m123", "3.0.0-SNAPSHOT", "3.0.0-m123", "3.0.0-m234"));

    Assertions.assertEquals("1.0.0",
        versionService.getLatestVersionOfArtifactByVersionRequest(artifactMetadata, "1.0-dev"));
    Assertions.assertEquals("1.0.0",
        versionService.getLatestVersionOfArtifactByVersionRequest(artifactMetadata, "1.0.0-dev"));
    Assertions.assertEquals("2.0.0",
        versionService.getLatestVersionOfArtifactByVersionRequest(artifactMetadata, "2-dev"));
    Assertions.assertEquals("3.0.0-m234",
        versionService.getLatestVersionOfArtifactByVersionRequest(artifactMetadata, "3-dev"));

    Assertions.assertEquals("1.0.0",
        versionService.getLatestVersionOfArtifactByVersionRequest(artifactMetadata, "1.0"));
    Assertions.assertEquals("2.0.0", versionService.getLatestVersionOfArtifactByVersionRequest(artifactMetadata, "2"));
    Assertions.assertEquals("3.0.0-m234",
        versionService.getLatestVersionOfArtifactByVersionRequest(artifactMetadata, "3.0.0"));

    Assertions.assertEquals("3.0.0-SNAPSHOT",
        versionService.getLatestVersionOfArtifactByVersionRequest(artifactMetadata, "3.0.0-SNAPSHOT"));
  }

  @Test
  void testGetLatestVersionArtifactDownloadUrl() {
    Metadata artifactMetadata = getMockMetadataWithVersions();
    Assertions.assertTrue(StringUtils.isBlank(
        versionService.getLatestVersionArtifactDownloadUrl(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION, MOCK_ARTIFACT_ID)));
    Mockito.when(metadataRepo.findByProductIdAndArtifactId(Mockito.anyString(), Mockito.anyString())).thenReturn(
        artifactMetadata);
    Assertions.assertTrue(StringUtils.isBlank(
        versionService.getLatestVersionArtifactDownloadUrl(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION, MOCK_ARTIFACT_ID)));
    MavenArtifactVersion artifactVersions = getMockMavenArtifactVersion();
    artifactVersions.getAdditionalArtifactsByVersion().put(MOCK_RELEASED_VERSION, List.of(getMockMavenArtifactModel()));
    when(mavenArtifactVersionRepository.findById(Mockito.anyString())).thenReturn(Optional.of(artifactVersions));
    String result = versionService.getLatestVersionArtifactDownloadUrl(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION,
        MOCK_ARTIFACT_ID);
    Assertions.assertEquals(MOCK_DOWNLOAD_URL, result);
  }
}
