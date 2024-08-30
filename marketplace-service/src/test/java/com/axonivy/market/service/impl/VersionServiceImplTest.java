package com.axonivy.market.service.impl;

import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.entity.MavenArtifactModel;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.productjsonfilecontent.ProductJsonContent;
import com.axonivy.market.github.model.ArchivedArtifact;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.model.MavenArtifactVersionModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.util.XmlReaderUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Fail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VersionServiceImplTest {
  private String repoName;
  private Map<String, List<ArchivedArtifact>> archivedArtifactsMap;
  private List<MavenArtifact> artifactsFromMeta;
  private MavenArtifactVersion proceedDataCache;
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

  @BeforeEach()
  void prepareBeforeTest() {
    archivedArtifactsMap = new HashMap<>();
    artifactsFromMeta = new ArrayList<>();
    metaProductArtifact = new MavenArtifact();
    proceedDataCache = new MavenArtifactVersion();
    repoName = StringUtils.EMPTY;
    ReflectionTestUtils.setField(versionService, "archivedArtifactsMap", archivedArtifactsMap);
    ReflectionTestUtils.setField(versionService, "artifactsFromMeta", artifactsFromMeta);
    ReflectionTestUtils.setField(versionService, "proceedDataCache", proceedDataCache);
    ReflectionTestUtils.setField(versionService, "metaProductArtifact", metaProductArtifact);
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
    when(versionService.getProductMetaArtifacts(Mockito.anyString())).thenReturn(artifactsFromMeta);
    when(versionService.getVersionsFromMavenArtifacts()).thenReturn(
        List.of(targetVersion));
    when(mavenArtifactVersionRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
    ArrayList<MavenArtifactModel> artifactsInVersion = new ArrayList<>();
    artifactsInVersion.add(new MavenArtifactModel());
    when(versionService.convertMavenArtifactsToModels(Mockito.anyList(), Mockito.anyString())).thenReturn(
        artifactsInVersion);
    Assertions.assertEquals(1, versionService.getArtifactsAndVersionToDisplay(productId, false, targetVersion).size());

    MavenArtifactVersion proceededData = new MavenArtifactVersion();
    proceededData.getProductArtifactWithVersionReleased().put(targetVersion, new ArrayList<>());
    when(mavenArtifactVersionRepository.findById(Mockito.anyString())).thenReturn(Optional.of(proceededData));
    Assertions.assertEquals(1, versionService.getArtifactsAndVersionToDisplay(productId, false, targetVersion).size());
  }

  @Test
  void testHandleArtifactForVersionToDisplay() {
    String newVersionDetected = "10.0.10";
    List<MavenArtifactVersionModel> result = new ArrayList<>();
    List<String> versionsToDisplay = List.of(newVersionDetected);
    ReflectionTestUtils.setField(versionService, "productId", "adobe-acrobat-connector");
    Assertions.assertTrue(versionService.handleArtifactForVersionToDisplay(versionsToDisplay, result));
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals(newVersionDetected, result.get(0).getVersion());
    Assertions.assertEquals(0, result.get(0).getArtifactsByVersion().size());

    result = new ArrayList<>();
    ArrayList<MavenArtifactModel> artifactsInVersion = new ArrayList<>();
    artifactsInVersion.add(new MavenArtifactModel());
    when(versionService.convertMavenArtifactsToModels(Mockito.anyList(), Mockito.anyString())).thenReturn(
        artifactsInVersion);
    Assertions.assertFalse(versionService.handleArtifactForVersionToDisplay(versionsToDisplay, result));
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals(1, result.get(0).getArtifactsByVersion().size());
  }

  @Test
  void testGetProductMetaArtifacts() {
    Product product = new Product();
    MavenArtifact artifact1 = new MavenArtifact();
    MavenArtifact artifact2 = new MavenArtifact();
    List<MavenArtifact> artifacts = List.of(artifact1, artifact2);
    product.setArtifacts(artifacts);
    when(productRepository.findById(Mockito.anyString())).thenReturn(Optional.of(product));
    List<MavenArtifact> result = versionService.getProductMetaArtifacts("portal");
    Assertions.assertEquals(artifacts, result);
    Assertions.assertNull(versionService.getRepoName());

    product.setRepositoryName("/market/portal");
    versionService.getProductMetaArtifacts("portal");
    Assertions.assertEquals("portal", versionService.getRepoName());
  }

  @Test
  void testUpdateArtifactsInVersionWithProductArtifact() {
    String version = "10.0.10";
    ReflectionTestUtils.setField(versionService, "productId", "adobe-acrobat-connector");
    MavenArtifactModel artifactModel = new MavenArtifactModel();
    List<MavenArtifactModel> mockMavenArtifactModels = List.of(artifactModel);
    when(versionService.getProductJsonByVersion(Mockito.anyString())).thenReturn(List.of(new MavenArtifact()));
    when(versionService.convertMavenArtifactsToModels(Mockito.anyList(), Mockito.anyString())).thenReturn(
        mockMavenArtifactModels);
    Assertions.assertEquals(mockMavenArtifactModels,
        versionService.updateArtifactsInVersionWithProductArtifact(version));
    Assertions.assertEquals(1, proceedDataCache.getVersions().size());
    Assertions.assertEquals(1, proceedDataCache.getProductArtifactWithVersionReleased().size());
    Assertions.assertEquals(version, proceedDataCache.getVersions().get(0));
  }

  @Test
  void testSanitizeMetaArtifactBeforeHandle() {
    setUpArtifactFromMeta();
    String groupId = "com.axonivy.connector.adobe.acrobat.sign";
    String archivedArtifactId1 = "adobe-acrobat-sign-connector";
    String archivedArtifactId2 = "adobe-acrobat-sign-connector";
    ArchivedArtifact archivedArtifact1 = new ArchivedArtifact("10.0.10", groupId, archivedArtifactId1);
    ArchivedArtifact archivedArtifact2 = new ArchivedArtifact("10.0.20", groupId, archivedArtifactId2);
    artifactsFromMeta.get(1).setArchivedArtifacts(List.of(archivedArtifact2, archivedArtifact1));

    versionService.sanitizeMetaArtifactBeforeHandle();
    String artifactId = "adobe-acrobat-sign-connector";

    Assertions.assertEquals(1, artifactsFromMeta.size());
    Assertions.assertEquals(1, archivedArtifactsMap.size());
    Assertions.assertEquals(2, archivedArtifactsMap.get(artifactId).size());
    Assertions.assertEquals(archivedArtifact1, archivedArtifactsMap.get(artifactId).get(0));
  }


  @Test
  void getVersionsFromMavenArtifacts() {
    String repoUrl = "https://maven.axonivy.com";
    String groupId = "com.axonivy.connector.adobe.acrobat.sign";
    String artifactId = "adobe-acrobat-sign-connector";
    String archivedArtifactId = "adobe-sign-connector";
    artifactsFromMeta.add(new MavenArtifact(repoUrl, null, groupId, artifactId, null, null, null, null));
    ArrayList<String> versionFromArtifact = new ArrayList<>();
    versionFromArtifact.add("10.0.6");
    versionFromArtifact.add("10.0.5");
    versionFromArtifact.add("10.0.4");

    when(versionService.getVersionsFromArtifactDetails(repoUrl, groupId, artifactId)).thenReturn(versionFromArtifact);
    Assertions.assertEquals(versionService.getVersionsFromMavenArtifacts(), versionFromArtifact);

    List<ArchivedArtifact> archivedArtifacts = List.of(new ArchivedArtifact("10.0.9", groupId, archivedArtifactId));
    ArrayList<String> versionFromArchivedArtifact = new ArrayList<>();
    versionFromArchivedArtifact.add("10.0.3");
    versionFromArchivedArtifact.add("10.0.2");
    versionFromArchivedArtifact.add("10.0.1");
    artifactsFromMeta.get(0).setArchivedArtifacts(archivedArtifacts);
    when(versionService.getVersionsFromArtifactDetails(repoUrl, groupId, archivedArtifactId)).thenReturn(
        versionFromArchivedArtifact);
    versionFromArtifact.addAll(versionFromArchivedArtifact);
    Assertions.assertEquals(versionService.getVersionsFromMavenArtifacts(), versionFromArtifact);
  }

  @Test
  void testGetVersionsFromArtifactDetails() {

    String repoUrl = "https://maven.axonivy.com";
    String groupId = "com.axonivy.connector.adobe.acrobat.sign";
    String artifactId = "adobe-acrobat-sign-connector";

    ArrayList<String> versionFromArtifact = new ArrayList<>();
    versionFromArtifact.add("10.0.16");
    versionFromArtifact.add("10.0.18");
    versionFromArtifact.add("10.0.19");
    versionFromArtifact.add("10.0.20");
    versionFromArtifact.add("10.0.21");
    versionFromArtifact.add("10.0.22");
    versionFromArtifact.add("10.0.23");
    versionFromArtifact.add("10.0.24");
    versionFromArtifact.add("10.0.25");

    try (MockedStatic<XmlReaderUtils> xmlUtils = Mockito.mockStatic(XmlReaderUtils.class)) {
      xmlUtils.when(() -> XmlReaderUtils.readXMLFromUrl(Mockito.anyString())).thenReturn(versionFromArtifact);
      Assertions.assertEquals(versionService.getVersionsFromArtifactDetails(repoUrl, null, null), new ArrayList<>());
      Assertions.assertEquals(versionService.getVersionsFromArtifactDetails(repoUrl, groupId, artifactId),
          versionFromArtifact);
    }
  }

  @Test
  void testBuildMavenMetadataUrlFromArtifact() {
    String repoUrl = "https://maven.axonivy.com";
    String groupId = "com.axonivy.connector.adobe.acrobat.sign";
    String artifactId = "adobe-acrobat-sign-connector";
    String metadataUrl = "https://maven.axonivy.com/com/axonivy/connector/adobe/acrobat/sign/adobe-acrobat-sign-connector/maven-metadata.xml";
    Assertions.assertEquals(StringUtils.EMPTY,
        versionService.buildMavenMetadataUrlFromArtifact(repoUrl, null, artifactId));
    Assertions.assertEquals(StringUtils.EMPTY, versionService.buildMavenMetadataUrlFromArtifact(repoUrl, groupId, null),
        StringUtils.EMPTY);
    Assertions.assertEquals(metadataUrl,
        versionService.buildMavenMetadataUrlFromArtifact(repoUrl, groupId, artifactId));
  }

  @Test
  void testGetProductJsonByVersion() {
    String targetArtifactId = "adobe-acrobat-sign-connector";
    String targetGroupId = "com.axonivy.connector.adobe.acrobat";
    GHContent mockContent = mock(GHContent.class);
    repoName = "adobe-acrobat-sign-connector";
    ReflectionTestUtils.setField(versionService, "repoName", repoName);
    ReflectionTestUtils.setField(versionService, "productId", "adobe-acrobat-connector");
    MavenArtifact productArtifact = new MavenArtifact("https://maven.axonivy.com", null, targetGroupId,
        targetArtifactId, "iar", null, true, null);

    metaProductArtifact.setRepoUrl("https://maven.axonivy.com");
    metaProductArtifact.setGroupId(targetGroupId);
    metaProductArtifact.setArtifactId(targetArtifactId);
    when(gitHubService.getContentFromGHRepoAndTag(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(null);
    Assertions.assertEquals(0, versionService.getProductJsonByVersion("10.0.20").size());

    metaProductArtifact.setGroupId("com.axonivy.connector.adobe.acrobat.connector");
    when(gitHubService.getContentFromGHRepoAndTag(Mockito.anyString(), Mockito.anyString(),
        Mockito.anyString())).thenReturn(mockContent);

    try {
      when(gitHubService.convertProductJsonToMavenProductInfo(mockContent)).thenReturn(List.of(productArtifact));
      Assertions.assertEquals(1, versionService.getProductJsonByVersion("10.0.20").size());

      when(gitHubService.convertProductJsonToMavenProductInfo(mockContent)).thenThrow(
          new IOException("Mock IO Exception"));
      Assertions.assertEquals(0, versionService.getProductJsonByVersion("10.0.20").size());
    } catch (IOException e) {
      Fail.fail("Mock setup should not throw an exception");
    }
  }

  @Test
  void testConvertMavenArtifactToModel() {
    String downloadUrl = "https://maven.axonivy.com/com/axonivy/connector/adobe/acrobat/sign/adobe-acrobat-sign-connector/10.0.21/adobe-acrobat-sign-connector-10.0.21.iar";
    String artifactName = "Adobe Acrobat Sign Connector (iar)";

    MavenArtifact targetArtifact = new MavenArtifact(null, null, "com.axonivy.connector.adobe.acrobat.sign",
        "adobe-acrobat-sign-connector", null, null, null, null);

    // Assert case handle artifact without name
    MavenArtifactModel result = versionService.convertMavenArtifactToModel(targetArtifact, "10.0.21");
    MavenArtifactModel expectedResult = new MavenArtifactModel(artifactName, downloadUrl, null);
    Assertions.assertEquals(expectedResult.getName(), result.getName());
    Assertions.assertEquals(expectedResult.getDownloadUrl(), result.getDownloadUrl());

    // Assert case handle artifact with name
    artifactName = "Adobe Connector";
    String expectedArtifactName = "Adobe Connector (iar)";
    targetArtifact.setName(artifactName);
    result = versionService.convertMavenArtifactToModel(targetArtifact, "10.0.21");
    expectedResult = new MavenArtifactModel(artifactName, downloadUrl, null);
    Assertions.assertEquals(expectedArtifactName, result.getName());
    Assertions.assertEquals(expectedResult.getDownloadUrl(), result.getDownloadUrl());
  }

  @Test
  void testConvertMavenArtifactsToModels() {
    // Assert case param is empty
    List<MavenArtifactModel> result = versionService.convertMavenArtifactsToModels(Collections.emptyList(), "10.0.21");
    Assertions.assertEquals(Collections.emptyList(), result);

    // Assert case param is null
    result = versionService.convertMavenArtifactsToModels(null, "10.0.21");
    Assertions.assertEquals(Collections.emptyList(), result);

    // Assert case param is a list with existed element
    MavenArtifact targetArtifact = new MavenArtifact(null, null, "com.axonivy.connector.adobe.acrobat.sign",
        "adobe-acrobat-sign-connector", null, null, null, null);
    result = versionService.convertMavenArtifactsToModels(List.of(targetArtifact), "10.0.21");
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

    // Assert case without archived artifact
    String expectedResult = String.format(MavenConstants.ARTIFACT_DOWNLOAD_URL_FORMAT,
        MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL, "com/axonivy/connector", targetArtifactId, targetVersion,
        targetArtifactId, targetVersion, "iar");
    String result = versionService.buildDownloadUrlFromArtifactAndVersion(targetArtifact, targetVersion);
    Assertions.assertEquals(expectedResult, result);

    // Assert case with artifact not match & use custom repo
    ArchivedArtifact adobeArchivedArtifactVersion9 = new ArchivedArtifact("10.0.9", "com.axonivy.adobe.connector",
        "adobe-connector");
    ArchivedArtifact adobeArchivedArtifactVersion8 = new ArchivedArtifact("10.0.8", "com.axonivy.adobe.sign.connector",
        "adobe-sign-connector");
    archivedArtifactsMap.put(targetArtifactId, List.of(adobeArchivedArtifactVersion9, adobeArchivedArtifactVersion8));
    String customRepoUrl = "https://nexus.axonivy.com";
    targetArtifact.setRepoUrl(customRepoUrl);
    result = versionService.buildDownloadUrlFromArtifactAndVersion(targetArtifact, targetVersion);
    expectedResult = String.format(MavenConstants.ARTIFACT_DOWNLOAD_URL_FORMAT, customRepoUrl, "com/axonivy/connector",
        targetArtifactId, targetVersion, targetArtifactId, targetVersion, "iar");
    Assertions.assertEquals(expectedResult, result);

    // Assert case with artifact got matching archived artifact & use custom file
    // type
    String customType = "zip";
    targetArtifact.setType(customType);
    targetVersion = "10.0.9";
    result = versionService.buildDownloadUrlFromArtifactAndVersion(targetArtifact, "10.0.9");
    expectedResult = String.format(MavenConstants.ARTIFACT_DOWNLOAD_URL_FORMAT, customRepoUrl,
        "com/axonivy/adobe/connector", "adobe-connector", targetVersion, "adobe-connector", targetVersion, customType);
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void testFindArchivedArtifactInfoBestMatchWithVersion() {
    String targetArtifactId = "adobe-acrobat-sign-connector";
    String targetVersion = "10.0.10";
    ArchivedArtifact result = versionService.findArchivedArtifactInfoBestMatchWithVersion(targetArtifactId,
        targetVersion);
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
    result = versionService.findArchivedArtifactInfoBestMatchWithVersion(targetArtifactId, targetVersion);
    Assertions.assertNull(result);

    // Assert case with target version less than all of latest version from archived
    // artifact list
    result = versionService.findArchivedArtifactInfoBestMatchWithVersion(targetArtifactId, "10.0.7");
    Assertions.assertEquals(adobeArchivedArtifactVersion8, result);

    // Assert case with target version is in range of archived artifact list
    ArchivedArtifact adobeArchivedArtifactVersion10 = new ArchivedArtifact("10.0.10", "com.axonivy.connector",
        "adobe-sign-connector");

    archivedArtifactsMap.get(targetArtifactId).add(adobeArchivedArtifactVersion10);
    result = versionService.findArchivedArtifactInfoBestMatchWithVersion(targetArtifactId, targetVersion);
    Assertions.assertEquals(adobeArchivedArtifactVersion10, result);
  }

  @Test
  void testGetRepoNameFromMarketRepo() {
    String defaultRepositoryName = "market/adobe-acrobat-connector";
    String expectedRepoName = "adobe-acrobat-connector";
    String result = versionService.getRepoNameFromMarketRepo(defaultRepositoryName);
    Assertions.assertEquals(expectedRepoName, result);

    defaultRepositoryName = "market/utils/adobe-acrobat-connector";
    result = versionService.getRepoNameFromMarketRepo(defaultRepositoryName);
    Assertions.assertEquals(expectedRepoName, result);

    defaultRepositoryName = "adobe-acrobat-connector";
    result = versionService.getRepoNameFromMarketRepo(defaultRepositoryName);
    Assertions.assertEquals(expectedRepoName, result);
  }

//  @Test
//  void testGetVersionsForDesigner() {
//    Mockito.when(productRepository.getReleasedVersionsById(anyString()))
//        .thenReturn(List.of("11.3.0", "11.1.1", "11.1.0", "10.0.2"));
//
//    List<String> result = versionService.getVersionsForDesigner("11.3.0");
//
//    Assertions.assertEquals(result, List.of("11.3.0", "11.1.1", "11.1.0", "10.0.2"));
//  }

  @Test
  void testGetProductJsonContentByIdAndVersion() throws JsonProcessingException {
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
}
