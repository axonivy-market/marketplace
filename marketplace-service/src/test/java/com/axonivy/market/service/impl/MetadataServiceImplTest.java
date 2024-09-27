package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.MetadataSyncRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.util.MavenUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class MetadataServiceImplTest {
  @InjectMocks
  MetadataServiceImpl metadataService;
  @Mock
  ProductRepository productRepo;
  @Mock
  MetadataSyncRepository metadataSyncRepo;
  @Mock
  ProductJsonContentRepository productJsonRepo;
  @Mock
  MavenArtifactVersionRepository mavenArtifactVersionRepo;
  @Mock
  MetadataRepository metadataRepo;

  public final String MOCK_SNAPSHOT = """
      <metadata modelVersion="1.1.0">
        <groupId>com.axonivy.utils.bpmnstatistic</groupId>
        <artifactId>bpmn-statistic</artifactId>
        <version>8.0.5-SNAPSHOT</version>
        <versioning>
          <snapshot>
            <timestamp>20221011.124215</timestamp>
            <buildNumber>170</buildNumber>
          </snapshot>
        <lastUpdated>20221011130000</lastUpdated>
          <snapshotVersions>
             <snapshotVersion>
               <extension>iar</extension>
               <value>8.0.5-20221011.124215-170</value>
               <updated>20221011124215</updated>
             </snapshotVersion>
          </snapshotVersions>
        </versioning>
      </metadata>
       """;
  private final String MOCK_METADATA = """
      <metadata>
          <latest>1.0.2</latest>
          <release>1.0.1</release>
          <lastUpdated>20230924010101</lastUpdated>
          <versions>
              <version>1.0.0</version>
              <version>1.0.1</version>
              <version>1.0.2</version>
          </versions>
      </metadata>
      """;

  private ProductJsonContent getMockProductJson() {
    ProductJsonContent result = new ProductJsonContent();
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
                     "version": "${version}",
                     "type": "iar"
                   }
                 ],
                 "repositories": [
                   {
                     "id": "maven.axonivy.com",
                     "url": "https://maven.axonivy.com",
                     "snapshots": {
                       "enabled": "true"
                     }
                   }
                 ]
               }
             },
             {
               "id": "maven-dependency",
               "data": {
                 "dependencies": [
                   {
                     "groupId": "com.axonivy.utils.bpmnstatistic",
                     "artifactId": "bpmn-statistic",
                     "version": "${version}",
                     "type": "iar"
                   }
                 ],
                 "repositories": [
                   {
                     "id": "maven.axonivy.com",
                     "url": "https://maven.axonivy.com",
                     "snapshots": {
                       "enabled": "true"
                     }
                   }
                 ]
               }
             }
           ]
         }
        """;
    result.setContent(mockContent);
    return result;
  }

  private Artifact getMockArtifact() {
    Artifact mockArtifact = new Artifact();
    return mockArtifact;
  }

  private Metadata getMockMetadata() {
    return Metadata.builder().productId("bpmn-statistic").artifactId("bpmn-statistic").groupId(
        "com.axonivvy.util").isProductArtifact(true).repoUrl("https://maven.axonivy.com").type("iar").name("bpmn " +
        "statistic (iar)").build();
  }


  @Test
  void testGetArtifactsFromNonSyncedVersion() {
    Mockito.when(productJsonRepo.findByProductIdAndVersion("bpmn-statistic", "1.0.0")).thenReturn(
        getMockProductJson());
    Set<Artifact> artifacts = metadataService.getArtifactsFromNonSyncedVersion("bpmn-statistic",
        Collections.emptyList());
    Assertions.assertEquals(0, artifacts.size());
    Mockito.verify(productJsonRepo, Mockito.never()).findByProductIdAndVersion(Mockito.anyString(),
        Mockito.anyString());
    artifacts = metadataService.getArtifactsFromNonSyncedVersion("bpmn-statistic", List.of("1.0.0"));
    Assertions.assertEquals(2, artifacts.size());
    Assertions.assertEquals("bpmn-statistic-demo", artifacts.iterator().next().getArtifactId());
    Assertions.assertEquals(2, artifacts.stream().filter(Artifact::getIsProductArtifact).toList().size());
  }

  @Test
  void testUpdateMavenArtifactVersionCacheWithModel() {
    MavenArtifactVersion mockMavenArtifactVersion = new MavenArtifactVersion(StringUtils.EMPTY, new HashMap<>(),
        new HashMap<>());
    String version = "1.0.0";
    Metadata mockMetadata = getMockMetadata();
    metadataService.updateMavenArtifactVersionCacheWithModel(mockMavenArtifactVersion, version, mockMetadata);
    List<MavenArtifactModel> artifacts = mockMavenArtifactVersion.getProductArtifactsByVersion().get(version);
    Assertions.assertEquals(
        "https://maven.axonivy.com/com/axonivvy/util/bpmn-statistic/1.0.0/bpmn-statistic-1.0.0.iar",
        artifacts.get(0).getDownloadUrl());
    Assertions.assertEquals(1, artifacts.size());
    metadataService.updateMavenArtifactVersionCacheWithModel(mockMavenArtifactVersion, version, mockMetadata);
    Assertions.assertEquals(1, artifacts.size());

    Assertions.assertEquals(0, mockMavenArtifactVersion.getAdditionalArtifactsByVersion().entrySet().size());
    mockMetadata.setProductArtifact(false);
    metadataService.updateMavenArtifactVersionCacheWithModel(mockMavenArtifactVersion, version, mockMetadata);
    Assertions.assertEquals(1, mockMavenArtifactVersion.getAdditionalArtifactsByVersion().entrySet().size());
  }

  @Test
  void testUpdateMavenArtifactVersionForNonReleaseDeVersion() {
    Metadata mockMetadata = getMockMetadata();
    String version = "1.0.0-SNAPSHOT";
    MavenArtifactVersion mockMavenArtifactVersion = new MavenArtifactVersion(StringUtils.EMPTY, new HashMap<>(),
        new HashMap<>());
    try (MockedStatic<MavenUtils> mockUtils = Mockito.mockStatic(MavenUtils.class)) {
      mockUtils.when(() -> MavenUtils.buildSnapShotMetadataFromVersion(mockMetadata, version)).thenReturn(mockMetadata);
      mockUtils.when(() -> MavenUtils.getMetadataContentFromUrl(
          "https://maven.axonivy.com/com/axonivvy/util/bpmn-statistic/1.0.0-SNAPSHOT/maven-metadata.xml")).thenReturn(
          MOCK_SNAPSHOT);
      mockUtils.when(() -> MavenUtils.buildSnapShotMetadataFromVersion(mockMetadata, version)).thenReturn(mockMetadata);
      metadataService.updateMavenArtifactVersionForNonReleaseDeVersion(mockMavenArtifactVersion, mockMetadata, version);
      Assertions.assertEquals(1, mockMavenArtifactVersion.getProductArtifactsByVersion().entrySet().size());
      Assertions.assertEquals(1, mockMavenArtifactVersion.getProductArtifactsByVersion().get(version).size());
    }
  }
}
