package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.MetadataSync;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.github.model.Meta;
import com.axonivy.market.model.MavenArtifactModel;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.MetadataSyncRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
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
  void testUpdateArtifactsFromNonSyncedVersion() {
    Mockito.when(productJsonRepo.findByProductIdAndVersion("bpmn-statistic", "1.0.0")).thenReturn(
        getMockProductJson());
    Set<Artifact> artifacts = new HashSet<>();
    metadataService.updateArtifactsFromNonSyncedVersion("bpmn-statistic", Collections.emptyList(), artifacts);
    Assertions.assertEquals(0, artifacts.size());
    Mockito.verify(productJsonRepo, Mockito.never()).findByProductIdAndVersion(Mockito.anyString(),
        Mockito.anyString());
    metadataService.updateArtifactsFromNonSyncedVersion("bpmn-statistic", List.of("1.0.0"), artifacts);
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


}
