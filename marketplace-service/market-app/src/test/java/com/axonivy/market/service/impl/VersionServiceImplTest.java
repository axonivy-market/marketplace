package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.ArchivedArtifact;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.enums.DevelopmentVersion;
import com.axonivy.market.exceptions.model.NotFoundException;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.service.ProductMarketplaceDataService;
import com.axonivy.market.util.MavenUtils;
import com.axonivy.market.util.VersionUtils;
import org.apache.commons.lang3.ObjectUtils;
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

  @Mock
  private ProductMarketplaceDataService productMarketplaceDataService;

  @Test
  void testGetArtifactsAndVersionToDisplay() {
    when(mavenArtifactVersionRepository.findByProductId(Mockito.anyString())).thenReturn(List.of());
    when(mavenArtifactVersionRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(new ArrayList<>());

    Assertions.assertTrue(CollectionUtils.isEmpty(
            versionService.getArtifactsAndVersionToDisplay(MOCK_PRODUCT_ID, false, MOCK_RELEASED_VERSION)),
        "Artifacts and version to be displayed should be empty");

    List<MavenArtifactVersion> proceededData = new ArrayList<>();

    MavenArtifactVersion mockModel = mockAdditionalMavenArtifactVersion(MOCK_RELEASED_VERSION, null);
    mockModel.setName(MOCK_PRODUCT_ID);
    mockModel.setDownloadUrl(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    proceededData.add(mockModel);

    when(mavenArtifactVersionRepository.findByProductId(Mockito.anyString())).thenReturn(proceededData);
    Assertions.assertTrue(ObjectUtils.isNotEmpty(
            versionService.getArtifactsAndVersionToDisplay(MOCK_PRODUCT_ID, false, MOCK_RELEASED_VERSION)),
        "Artifacts and version to be displayed should not be empty");
  }

  @Test
  void testGetArtifactsAndVersionToSort() {
    List<MavenArtifactVersion> proceededData = new ArrayList<>();

    MavenArtifactVersion mockModel = mockAdditionalMavenArtifactVersion(MOCK_RELEASED_VERSION, "artifact-test");
    mockModel.setName(MOCK_PRODUCT_ID);
    mockModel.setDownloadUrl(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);

    MavenArtifactVersion mockModel2 = mockAdditionalMavenArtifactVersion(MOCK_RELEASED_VERSION, "artifact");
    mockModel2.setName("MOCK_PRODUCT_ID");

    MavenArtifactVersion mockModel3 = mockAdditionalMavenArtifactVersion(MOCK_RELEASED_VERSION, "artifact-demo");
    mockModel3.setName("MOCK_PRODUCT_ID_DEMO");

    proceededData.add(mockModel);
    proceededData.add(mockModel2);
    proceededData.add(mockModel3);

    when(mavenArtifactVersionRepository.findByProductId(Mockito.anyString())).thenReturn(proceededData);
    List<MavenArtifactVersionModel> result =
        versionService.getArtifactsAndVersionToDisplay(MOCK_PRODUCT_ID, false, MOCK_RELEASED_VERSION);

    List<MavenArtifactVersion> expectedResult = result.get(0).getArtifactsByVersion();
    Assertions.assertEquals(3, expectedResult.size(),
        "Artifacts and version to be displayed should be 3");
    Assertions.assertEquals("artifact", expectedResult.get(0).getId().getArtifactId(),
        "First artifacts ID should be 'artifact'");
    Assertions.assertEquals("artifact-demo", expectedResult.get(1).getId().getArtifactId(),
        "Second artifacts ID should be 'artifact-demo'");
    Assertions.assertEquals("artifact-test", expectedResult.get(2).getId().getArtifactId(),
        "Third artifacts ID should be 'artifact-test'");
  }

  @Test
  void testFindArchivedArtifactInfoBestMatchWithVersion() {
    ArchivedArtifact result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion(MOCK_RELEASED_VERSION,
        Collections.<ArchivedArtifact>emptySet());
    Assertions.assertNull(result, "Archived artifact info best match with version should be null if archive artifact " +
        "set is empty");
    ArchivedArtifact adobeArchivedArtifactVersion8 = new ArchivedArtifact("10.0.8", MOCK_GROUP_ID,
        "bpmn-connector");
    ArchivedArtifact adobeArchivedArtifactVersion9 = new ArchivedArtifact("10.0.9", MOCK_GROUP_ID,
        "process-mining-connector");
    List<ArchivedArtifact> archivedArtifacts = new ArrayList<>();
    archivedArtifacts.add(adobeArchivedArtifactVersion8);
    archivedArtifacts.add(adobeArchivedArtifactVersion9);
    result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion(MOCK_RELEASED_VERSION,
        new HashSet<>(archivedArtifacts));
    Assertions.assertNull(result, "Archived artifact info best match with version should be null if archive artifact " +
        "set is does not contain match versions");
    result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion("10.0.7",
        new HashSet<>(archivedArtifacts));
    Assertions.assertNotNull(result, "Archived artifact info best match with version should not be null");
    Assertions.assertEquals(adobeArchivedArtifactVersion8.getLastVersion(), result.getLastVersion(),
        "Archived artifact latest version should match input version");
    ArchivedArtifact adobeArchivedArtifactVersion10 = new ArchivedArtifact(MOCK_RELEASED_VERSION,
        MOCK_GROUP_ID, "adobe-sign-connector");
    archivedArtifacts.add(adobeArchivedArtifactVersion10);
    result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion(MOCK_RELEASED_VERSION,
        new HashSet<>(archivedArtifacts));
    Assertions.assertEquals(adobeArchivedArtifactVersion10.getArtifactId(), result.getArtifactId(),
        "Archived artifact ID should match input artifact ID");
  }

  @Test
  void testGetInstallableVersions() {
    List<String> mockVersions = List.of("11.3.0-SNAPSHOT", "11.1.1", "11.1.0", "10.0.2");
    Metadata mockMetadata = getMockMetadata();
    mockMetadata.setArtifactId(MOCK_PRODUCT_ARTIFACT_ID);
    mockMetadata.setVersions(new HashSet<>());
    mockMetadata.getVersions().addAll(mockVersions);
    List<VersionAndUrlModel> result = versionService.getInstallableVersions(MOCK_PRODUCT_ID, true,
        MOCK_DESIGNER_VERSION);
    Assertions.assertTrue(CollectionUtils.isEmpty(result), "Installation version list should be empty");
    when(metadataRepo.findByProductId(MOCK_PRODUCT_ID)).thenReturn(List.of(mockMetadata));
    result = versionService.getInstallableVersions(MOCK_PRODUCT_ID, true, MOCK_DESIGNER_VERSION);
    Assertions.assertEquals(result.stream().map(VersionAndUrlModel::getVersion).toList(), mockVersions,
        "Result version list should match mock version list");
    Assertions.assertTrue(result.get(0).getUrl().endsWith("/api/product-details/bpmn-statistic/11.3" +
            ".0-SNAPSHOT/json?designerVersion=12.0.4"),
        "First installable version should end with /api/product-details/bpmn-statistic/11.3" +
            ".0-SNAPSHOT/json?designerVersion=12.0.4");
    Assertions.assertTrue(
        result.get(1).getUrl().endsWith("/api/product-details/bpmn-statistic/11.1.1/json?designerVersion=12.0.4"),
        "Second installable version should end with /api/product-details/bpmn-statistic/11.1" +
            ".1/json?designerVersion=12.0.4");
    Assertions.assertTrue(
        result.get(2).getUrl().endsWith("/api/product-details/bpmn-statistic/11.1.0/json?designerVersion=12.0.4"),
        "Third installable version should end with /api/product-details/bpmn-statistic/11.1.0/json?designerVersion=12" +
            ".0.4");
    Assertions.assertTrue(
        result.get(3).getUrl().endsWith("/api/product-details/bpmn-statistic/10.0.2/json?designerVersion=12.0.4"),
        "Forth installable version should end with /api/product-details/bpmn-statistic/10.0.2/json?designerVersion=12" +
            ".0.4");
  }

  @Test
  void testGetProductJsonContentByIdAndVersion() {
    ProductJsonContent mockProductJsonContent = getMockProductJsonContent();
    mockProductJsonContent.setName(MOCK_PRODUCT_NAME);
    Mockito.when(productJsonContentRepository.findByProductIdAndVersion(anyString(), anyString()))
        .thenReturn(List.of(mockProductJsonContent));
    Map<String, Object> result = versionService.getProductJsonContentByIdAndVersion(MOCK_PRODUCT_ID,
        MOCK_RELEASED_VERSION, MOCK_DESIGNER_VERSION);
    Assertions.assertEquals(MOCK_PRODUCT_NAME, result.get("name"),
        "Product json content name should match mock product name");
  }

  @Test
  void testGetProductJsonContentByIdAndVersionNoResult() {
    Mockito.when(productJsonContentRepository.findByProductIdAndVersion(anyString(), anyString())).thenReturn(
        Collections.emptyList());
    Map<String, Object> result = versionService.getProductJsonContentByIdAndVersion(MOCK_PRODUCT_ID,
        MOCK_RELEASED_VERSION, MOCK_DESIGNER_VERSION);
    Assertions.assertEquals(new HashMap<>(), result,
        "Product json content should be empty");
  }

  @Test
  void testGetAllExistingVersions() {
    List<MavenArtifactVersion> mavenArtifactVersions = new ArrayList<>();
    Assertions.assertTrue(CollectionUtils.isEmpty(VersionUtils.extractAllVersions(mavenArtifactVersions, false)),
        "The extracted versions should be empty if the method param is empty list.");
    mavenArtifactVersions = getMockMavenArtifactVersionWithData();
    Assertions.assertTrue(ObjectUtils.isNotEmpty(VersionUtils.extractAllVersions(mavenArtifactVersions, true)), "The " +
        "extracted version should contain a snapshot.");
    Assertions.assertTrue(CollectionUtils.isEmpty(VersionUtils.extractAllVersions(mavenArtifactVersions, false)),
        "The extracted version should be empty if be filtered released versions.");
  }

  @Test
  void testGetLatestVersionArtifactDownloadUrl() {
    when(metadataRepo.findByProductIdAndArtifactId(MOCK_PRODUCT_ID, MOCK_ARTIFACT_ID))
        .thenReturn(List.of(getMockMetadataWithVersions()));
    when(mavenArtifactVersionRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(Collections.emptyList());

    String productId = MOCK_PRODUCT_ID;
    String versionCode = DevelopmentVersion.LATEST.getCode();
    String downloadFile = MOCK_ARTIFACT_DOWNLOAD_FILE;

    Assertions.assertThrows(NotFoundException.class,
        () -> versionService.getLatestVersionArtifactDownloadUrl(productId, versionCode, downloadFile),
        "Should throw NotFoundException when no artifacts found"
    );

    List<MavenArtifactVersion> mockMavenArtifactVersion = getMockMavenArtifactVersion();
    when(mavenArtifactVersionRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(mockMavenArtifactVersion);

    Assertions.assertThrows(NotFoundException.class,
        () -> versionService.getLatestVersionArtifactDownloadUrl(productId, versionCode, downloadFile),
        "Should throw NotFoundException when download URL not found"
    );

    List<MavenArtifactVersion> mockMavenArtifactVersion2 = getMockMavenArtifactVersion();
    MavenArtifactVersion mockMavenArtifactVersionWithDownloadUrl = getMockMavenArtifactVersionWithDownloadUrl();
    mockMavenArtifactVersionWithDownloadUrl.getId().setProductVersion("10.0.10");
    mockMavenArtifactVersion2.add(mockMavenArtifactVersionWithDownloadUrl);
    when(mavenArtifactVersionRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(mockMavenArtifactVersion2);

    String actualUrl = versionService.getLatestVersionArtifactDownloadUrl(
        MOCK_PRODUCT_ID, DevelopmentVersion.LATEST.getCode(), MOCK_ARTIFACT_DOWNLOAD_FILE);

    Assertions.assertEquals(MOCK_DOWNLOAD_URL, actualUrl,
        "Latest version artifact download URL should match mock download URL");
  }
}
