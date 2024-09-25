package com.axonivy.market.service.impl;

import com.axonivy.market.bo.ArchivedArtifact;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.model.VersionAndUrlModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.util.MavenUtils;
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
  private List<Artifact> artifactsFromMeta;
  private Artifact metaProductArtifact;
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
    artifactsFromMeta = new ArrayList<>();
    metaProductArtifact = new Artifact();
  }

  private void setUpArtifactFromMeta() {
    String repoUrl = "https://maven.axonivy.com";
    String groupId = "com.axonivy.connector.adobe.acrobat.sign";
    String artifactId = "adobe-acrobat-sign-connector";
    metaProductArtifact.setGroupId(groupId);
    metaProductArtifact.setArtifactId(artifactId);
    metaProductArtifact.setIsProductArtifact(true);
    Artifact additionalMavenArtifact = new Artifact(repoUrl, "", groupId, artifactId, "", null, null, null,true);
    artifactsFromMeta.add(metaProductArtifact);
    artifactsFromMeta.add(additionalMavenArtifact);
  }

  @Test
  void testGetArtifactsAndVersionToDisplay() {
    String productId = "adobe-acrobat-sign-connector";
    String targetVersion = "10.0.10";
    setUpArtifactFromMeta();
    when(versionService.getArtifactsFromMeta(Mockito.anyString())).thenReturn(artifactsFromMeta);
    when(mavenArtifactVersionRepository.findById(Mockito.anyString())).thenReturn(Optional.empty());
    ArrayList<MavenArtifactModel> artifactsInVersion = new ArrayList<>();
    artifactsInVersion.add(new MavenArtifactModel());
    when(mavenArtifactVersionRepository.findById("adobe-acrobat-sign-connector")).thenReturn(
        Optional.ofNullable(
            MavenArtifactVersion.builder().productId(productId).productArtifactsByVersion(new HashMap<>()).build()));
    Assertions.assertEquals(0, versionService.getArtifactsAndVersionToDisplay(productId, false, targetVersion).size());

    MavenArtifactVersion proceededData =
        MavenArtifactVersion.builder().productArtifactsByVersion(new HashMap<>()).build();
    proceededData.getProductArtifactsByVersion().put(targetVersion, new ArrayList<>());
    when(mavenArtifactVersionRepository.findById(Mockito.anyString())).thenReturn(Optional.of(proceededData));
    Assertions.assertEquals(1, versionService.getArtifactsAndVersionToDisplay(productId, false, targetVersion).size());
  }

  @Test
  void testGetArtifactsFromMeta() {
    Product product = new Product();
    Artifact artifact1 = new Artifact();
    Artifact artifact2 = new Artifact();
    List<Artifact> artifacts = List.of(artifact1, artifact2);
    product.setArtifacts(artifacts);
    when(productRepository.findById(Mockito.anyString())).thenReturn(Optional.of(product));
    List<Artifact> result = versionService.getArtifactsFromMeta("portal");
    Assertions.assertEquals(artifacts, result);
  }

  @Test
  void testGetMavenArtifactsFromProductJsonByVersion() throws IOException {
    when(productJsonContentRepository.findByProductIdAndVersion("adobe-acrobat-connector", "10.0.20")).thenReturn(null);

    Assertions.assertEquals(0,
        versionService.getMavenArtifactsFromProductJsonByTag("10.0.20", "adobe-acrobat-connector").size());

    String jsonContent = "{ \"installers\": [{ \"id\": \"maven-import\", \"data\": { \"repositories\": [{ \"url\": " +
        "\"http://repo.url\" }], \"projects\": [], \"dependencies\": [] } }] }";
    ProductJsonContent productJson = new ProductJsonContent();
    productJson.setContent(jsonContent);
    when(productJsonContentRepository.findByProductIdAndVersion("adobe-acrobat-connector", "10.0.20")).thenReturn(
        productJson);
    List<Artifact> results = versionService.getMavenArtifactsFromProductJsonByTag("10.0.20", "adobe-acrobat-connector");
  }





  @Test
  void testFindArchivedArtifactInfoBestMatchWithVersion() {
    String targetArtifactId = "adobe-acrobat-sign-connector";
    String targetVersion = "10.0.10";
    ArchivedArtifact result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion(
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
    result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion(targetVersion,
        archivedArtifacts);
    Assertions.assertNull(result);

    // Assert case with target version less than all of latest version from archived
    // artifact list
    result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion("10.0.7",
        archivedArtifacts);
    Assertions.assertEquals(adobeArchivedArtifactVersion8, result);

    // Assert case with target version is in range of archived artifact list
    ArchivedArtifact adobeArchivedArtifactVersion10 = new ArchivedArtifact("10.0.10", "com.axonivy.connector",
        "adobe-sign-connector");
    archivedArtifacts.add(adobeArchivedArtifactVersion10);
    result = MavenUtils.findArchivedArtifactInfoBestMatchWithVersion(targetVersion,
        archivedArtifacts);
    Assertions.assertEquals(adobeArchivedArtifactVersion10.getArtifactId(), result.getArtifactId());
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

    Map<String, Object> result = versionService.getProductJsonContentByIdAndTag("amazon-comprehend", "11.3.1");

    Assertions.assertEquals("Amazon Comprehend", result.get("name"));
  }

  @Test
  void testGetProductJsonContentByIdAndVersion_noResult() {
    Mockito.when(productJsonContentRepository.findByProductIdAndVersion(anyString(), anyString())).thenReturn(null);

    Map<String, Object> result = versionService.getProductJsonContentByIdAndTag("amazon-comprehend", "11.3.1");

    Assertions.assertEquals(new HashMap<>(), result);
  }

  @Test
  void testgetPersistedVersions() {
    String mockProductId = "portal";
    Assertions.assertEquals(0, versionService.getPersistedVersions(mockProductId).size());
    String mockVersion = "10.0.1";
    Product mocProduct = new Product();
    mocProduct.setId(mockProductId);
    mocProduct.setReleasedVersions(List.of(mockVersion));
    when(productRepository.findById(mockProductId)).thenReturn(Optional.of(mocProduct));
    Assertions.assertEquals(1, versionService.getPersistedVersions(mockProductId).size());
    Assertions.assertEquals(mockVersion, versionService.getPersistedVersions(mockProductId).get(0));
  }

  @Test
  void testClearAllProductVersions() {
    versionService.clearAllProductVersions();
    verify(mavenArtifactVersionRepository).deleteAll();
  }
}
