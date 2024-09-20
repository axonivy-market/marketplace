package com.axonivy.market.service.impl;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.MavenArtifactModel;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.github.model.ArchivedArtifact;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VersionServiceImplTest {
  private Map<String, List<ArchivedArtifact>> archivedArtifactsMap;
  private List<MavenArtifact> artifactsFromMeta;
  private MavenArtifact metaProductArtifact;
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

  @BeforeEach()
  void prepareBeforeTest() {
    archivedArtifactsMap = new HashMap<>();
    artifactsFromMeta = new ArrayList<>();
    metaProductArtifact = new MavenArtifact();
  }

  private void setUpArtifactFromMeta() {
    String repoUrl = "https://maven.axonivy.com";
    String groupId = "com.axonivy.connector.adobe.acrobat.sign";
    String artifactId = "adobe-acrobat-sign-connector";
    metaProductArtifact.setGroupId(groupId);
    metaProductArtifact.setArtifactId(artifactId);
    metaProductArtifact.setIsProductArtifact(true);
    MavenArtifact additionalMavenArtifact = new MavenArtifact(repoUrl, "", groupId, artifactId, "", null, null, null);
    artifactsFromMeta.add(metaProductArtifact);
    artifactsFromMeta.add(additionalMavenArtifact);
  }

  @Test
  void testGetArtifactsAndVersionToDisplay() {
    String productId = "adobe-acrobat-sign-connector";
    String targetVersion = "10.0.10";
    setUpArtifactFromMeta();
    when(versionService.getArtifactsFromMeta(Mockito.anyString())).thenReturn(artifactsFromMeta);
    when(productModuleContentRepository.findTagsByProductId(productId)).thenReturn(List.of("v10.0.10"));
    when(mavenArtifactVersionRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
    ArrayList<MavenArtifactModel> artifactsInVersion = new ArrayList<>();
    artifactsInVersion.add(new MavenArtifactModel());
    when(versionService.convertArtifactsToModels(Mockito.anyList(), Mockito.anyString(), Mockito.any())).thenReturn(
        artifactsInVersion);
    Assertions.assertEquals(1, versionService.getArtifactsAndVersionToDisplay(productId, false, targetVersion).size());

    MavenArtifactVersion proceededData = new MavenArtifactVersion();
    proceededData.getProductArtifactWithVersionReleased().put(targetVersion, new ArrayList<>());
    when(mavenArtifactVersionRepository.findById(Mockito.anyString())).thenReturn(Optional.of(proceededData));
    Assertions.assertEquals(1, versionService.getArtifactsAndVersionToDisplay(productId, false, targetVersion).size());
  }

  @Test
  void testGetArtifactsFromMeta() {
    Product product = new Product();
    MavenArtifact artifact1 = new MavenArtifact();
    MavenArtifact artifact2 = new MavenArtifact();
    List<MavenArtifact> artifacts = List.of(artifact1, artifact2);
    product.setArtifacts(artifacts);
    when(productRepository.findById(Mockito.anyString())).thenReturn(Optional.of(product));
    List<MavenArtifact> result = versionService.getArtifactsFromMeta("portal");
    Assertions.assertEquals(artifacts, result);
  }

  @Test
  void testGetMavenArtifactsFromProductJsonByVersion() throws IOException {
    when(productJsonContentRepository.findByProductIdAndVersion("adobe-acrobat-connector", "10.0.20")).thenReturn(null);

    Assertions.assertEquals(0,
        versionService.getMavenArtifactsFromProductJsonByVersion("10.0.20", "adobe-acrobat-connector").size());

    String jsonContent = "{ \"installers\": [{ \"id\": \"maven-import\", \"data\": { \"repositories\": [{ \"url\": " +
        "\"http://repo.url\" }], \"projects\": [], \"dependencies\": [] } }] }";
    ProductJsonContent productJson = new ProductJsonContent();
    productJson.setContent(jsonContent);
    when(productJsonContentRepository.findByProductIdAndVersion("adobe-acrobat-connector", "10.0.20")).thenReturn(
        productJson);

    List<MavenArtifact> expectedArtifacts = new ArrayList<>();
    when(gitHubService.extractMavenArtifactsFromContentStream(Mockito.any())).thenReturn(expectedArtifacts);

    List<MavenArtifact> actualArtifacts = versionService.getMavenArtifactsFromProductJsonByVersion("10.0.20",
        "adobe-acrobat-connector");

    Assertions.assertEquals(expectedArtifacts, actualArtifacts);
    verify(productJsonContentRepository, Mockito.times(2)).findByProductIdAndVersion("adobe-acrobat-connector",
        "10.0.20");
    verify(gitHubService).extractMavenArtifactsFromContentStream(Mockito.any());

  }

  @Test
  void testConvertArtifactsToModels() {
    // Assert case param is empty
    List<MavenArtifactModel> result = versionService.convertArtifactsToModels(Collections.emptyList(), "10.0.21",
        new HashMap<>());
    Assertions.assertEquals(Collections.emptyList(), result);

    // Assert case param is null
    result = versionService.convertArtifactsToModels(null, "10.0.21", new HashMap<>());
    Assertions.assertEquals(Collections.emptyList(), result);

    // Assert case param is a list with existed element
    MavenArtifact targetArtifact = new MavenArtifact(null, null, "com.axonivy.connector.adobe.acrobat.sign",
        "adobe-acrobat-sign-connector", null, null, null, null);
    result = versionService.convertArtifactsToModels(List.of(targetArtifact), "10.0.21", new HashMap<>());
    Assertions.assertEquals(1, result.size());
  }

  @Test
  void testBuildDownloadUrlFromArtifactAndVersion() {
    // Set up artifact for testing
    String targetArtifactId = "adobe-acrobat-sign-connector";
    String targetGroupId = "com.axonivy.connector";
    MavenArtifact targetArtifact = new MavenArtifact(null, null, targetGroupId, targetArtifactId, "iar", null, null,
        null);
    String targetVersion = "10.0.10";
    String artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, targetArtifactId, targetVersion,
        "iar");

    // Assert case without archived artifact
    String expectedResult = String.join(CommonConstants.SLASH,
        MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL, "com/axonivy/connector", targetArtifactId, targetVersion,
        artifactFileName);
    String result = versionService.buildDownloadUrlFromArtifactAndVersion(targetArtifact, targetVersion,
        Collections.emptyList());
    Assertions.assertEquals(expectedResult, result);

    // Assert case with artifact not match & use custom repo
    ArchivedArtifact adobeArchivedArtifactVersion9 = new ArchivedArtifact("10.0.9", "com.axonivy.adobe.connector",
        "adobe-connector");
    ArchivedArtifact adobeArchivedArtifactVersion8 = new ArchivedArtifact("10.0.8", "com.axonivy.adobe.sign.connector",
        "adobe-sign-connector");
    String customRepoUrl = "https://nexus.axonivy.com";
    targetArtifact.setRepoUrl(customRepoUrl);
    result = versionService.buildDownloadUrlFromArtifactAndVersion(targetArtifact, targetVersion,
        List.of(adobeArchivedArtifactVersion9, adobeArchivedArtifactVersion8));
    artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, targetArtifactId, targetVersion,
        "iar");
    expectedResult = String.join(CommonConstants.SLASH,
        customRepoUrl, "com/axonivy/connector", targetArtifactId, targetVersion,
        artifactFileName);
    Assertions.assertEquals(expectedResult, result);

    // Assert case with artifact got matching archived artifact & use custom file
    // type
    String customType = "zip";
    targetArtifact.setType(customType);
    targetVersion = "10.0.9";
    result = versionService.buildDownloadUrlFromArtifactAndVersion(targetArtifact, "10.0.9",
        List.of(adobeArchivedArtifactVersion9, adobeArchivedArtifactVersion8));
    artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, "adobe-connector", targetVersion,
        customType);
    expectedResult = String.join(CommonConstants.SLASH,
        customRepoUrl, "com/axonivy/adobe/connector", "adobe-connector", targetVersion,
        artifactFileName);
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void testFindArchivedArtifactInfoBestMatchWithVersion() {
    String targetArtifactId = "adobe-acrobat-sign-connector";
    String targetVersion = "10.0.10";
    ArchivedArtifact result = versionService.findArchivedArtifactInfoBestMatchWithVersion(
        targetVersion, Collections.emptyList());
    Assertions.assertNull(result);

    // Assert case with target version higher than all of latest version from
    // archived artifact list
    ArchivedArtifact adobeArchivedArtifactVersion8 = new ArchivedArtifact("10.0.8", "com.axonivy.connector",
        "adobe-sign-connector");
    ArchivedArtifact adobeArchivedArtifactVersion9 = new ArchivedArtifact("10.0.9", "com.axonivy.connector",
        "adobe-acrobat-sign-connector");
    List<ArchivedArtifact> archivedArtifacts = new ArrayList<>();
    archivedArtifacts.add(adobeArchivedArtifactVersion8);
    archivedArtifacts.add(adobeArchivedArtifactVersion9);
    archivedArtifactsMap.put(targetArtifactId, archivedArtifacts);
    result = versionService.findArchivedArtifactInfoBestMatchWithVersion(targetVersion,
        archivedArtifacts);
    Assertions.assertNull(result);

    // Assert case with target version less than all of latest version from archived
    // artifact list
    result = versionService.findArchivedArtifactInfoBestMatchWithVersion("10.0.7",
        archivedArtifacts);
    Assertions.assertEquals(adobeArchivedArtifactVersion8, result);

    // Assert case with target version is in range of archived artifact list
    ArchivedArtifact adobeArchivedArtifactVersion10 = new ArchivedArtifact("10.0.10", "com.axonivy.connector",
        "adobe-sign-connector");

    archivedArtifactsMap.get(targetArtifactId).add(adobeArchivedArtifactVersion10);
    result = versionService.findArchivedArtifactInfoBestMatchWithVersion(targetVersion,
        archivedArtifacts);
    Assertions.assertEquals(adobeArchivedArtifactVersion10, result);
  }

  @Test
  void testGetVersionsForDesigner() {
    Mockito.when(productRepository.getReleasedVersionsById(anyString()))
        .thenReturn(List.of("11.3.0", "11.1.1", "11.1.0", "10.0.2"));

    List<VersionAndUrlModel> result = versionService.getVersionsForDesigner("11.3.0");

    Assertions.assertEquals(result.stream().map(VersionAndUrlModel::getVersion).toList(),
        List.of("11.3.0", "11.1.1", "11.1.0", "10.0.2"));
    Assertions.assertTrue(result.get(0).getUrl().endsWith("/api/product-details/11.3.0/11.3.0/json"));
    Assertions.assertTrue(result.get(1).getUrl().endsWith("/api/product-details/11.3.0/11.1.1/json"));
    Assertions.assertTrue(result.get(2).getUrl().endsWith("/api/product-details/11.3.0/11.1.0/json"));
    Assertions.assertTrue(result.get(3).getUrl().endsWith("/api/product-details/11.3.0/10.0.2/json"));
  }

  @Test
  void testGetProductJsonContentByIdAndVersion() {
    ProductJsonContent mockProductJsonContent = new ProductJsonContent();
    String mockContent = """
        {
           "$schema": "https://json-schema.axonivy.com/market/10.0.0/product.json",
           "installers": [
             {
               "id": "maven-import",
               "data": {
                 "projects": [
                   {
                     "groupId": "com.axonivy.utils.bpmnstatistic",
                     "artifactId": "bpmn-statistic-demo",
                     "version": "11.3.1",
                     "type": "iar"
                   }
                 ]
               }
             }
           ]
         }
        """;
    mockProductJsonContent.setProductId("amazon-comprehend");
    mockProductJsonContent.setVersion("11.3.1");
    mockProductJsonContent.setName("Amazon Comprehend");
    mockProductJsonContent.setContent(mockContent);

    Mockito.when(productJsonContentRepository.findByProductIdAndVersion(anyString(), anyString()))
        .thenReturn(mockProductJsonContent);

    Map<String, Object> result = versionService.getProductJsonContentByIdAndVersion("amazon-comprehend", "11.3.1");

    Assertions.assertEquals("Amazon Comprehend", result.get("name"));
  }

  @Test
  void testGetProductJsonContentByIdAndVersion_noResult() {
    Mockito.when(productJsonContentRepository.findByProductIdAndVersion(anyString(), anyString())).thenReturn(null);

    Map<String, Object> result = versionService.getProductJsonContentByIdAndVersion("amazon-comprehend", "11.3.1");

    Assertions.assertEquals(new HashMap<>(), result);
  }

  @Test
  void testgetPersistedVersions() {
    String mockProductId = "portal";
    String mockVersion = "10.0.1";
    Product mocProduct = new Product();
    mocProduct.setId(mockProductId);
    mocProduct.setReleasedVersions(List.of(mockVersion));
    when(productRepository.findById(mockProductId)).thenReturn(Optional.of(mocProduct));
    Assertions.assertEquals(1, versionService.getPersistedVersions(mockProductId).size());
    Assertions.assertEquals(mockVersion, versionService.getPersistedVersions(mockProductId).get(0));

  }
}
