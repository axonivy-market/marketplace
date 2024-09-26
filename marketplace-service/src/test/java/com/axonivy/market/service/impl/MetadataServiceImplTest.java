package com.axonivy.market.service.impl;

import com.axonivy.market.bo.Artifact;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.entity.MetadataSync;
import com.axonivy.market.entity.ProductJsonContent;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.MetadataRepository;
import com.axonivy.market.repository.MetadataSyncRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
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

//  private Metadata getMockMetadata() {
//    return Metadata.builder().
//  }

  private Artifact getMockArtifact() {
    Artifact mockArtifact = new Artifact();
    return mockArtifact;
  }


  @Test
  void testUpdateArtifactsFromNonSyncedVersion() {
    Mockito.when(productJsonRepo.findByProductIdAndVersion("bpmn-statistic", "1.0.0")).thenReturn(
        getMockProductJson());
    Set<Artifact> artifacts = new HashSet<>();
    metadataService.updateArtifactsFromNonSyncedVersion("bpmn-statistic", List.of("1.0.0"), artifacts);
    Assertions.assertEquals(2, artifacts.size());
    Assertions.assertEquals("bpmn-statistic-demo", artifacts.iterator().next().getArtifactId());
    Assertions.assertEquals(2, artifacts.stream().filter(Artifact::getIsProductArtifact).toList().size());
  }


}
