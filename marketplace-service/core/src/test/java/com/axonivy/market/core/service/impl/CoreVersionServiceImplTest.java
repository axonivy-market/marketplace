package com.axonivy.market.core.service.impl;

import com.axonivy.market.core.CoreBaseSetup;
import com.axonivy.market.core.builder.ProductJsonLinkBuilder;
import com.axonivy.market.core.constants.CoreMavenConstants;
import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.entity.Metadata;
import com.axonivy.market.core.entity.ProductJsonContent;
import com.axonivy.market.core.model.VersionAndUrlModel;
import com.axonivy.market.core.repository.CoreMavenArtifactVersionRepository;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import com.axonivy.market.core.repository.CoreProductJsonContentRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.*;

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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CoreVersionServiceImplTest extends CoreBaseSetup {
  @Mock
  private CoreMavenArtifactVersionRepository coreMavenArtifactVersionRepository;

  @Mock
  private CoreMetadataRepository coreMetadataRepository;

  @Mock
  private CoreProductJsonContentRepository coreProductJsonContentRepo;

  @Mock
  private ProductJsonLinkBuilder productJsonLinkBuilder;

  @Spy
  @InjectMocks
  private CoreVersionServiceImpl coreVersionService;

  @Test
  void testGetLatestInstallableVersion() {
    Metadata metadata = Metadata.builder()
        .artifactId("test-product")
        .isProductArtifact(true)
        .versions(Set.of("10.0.1", "10.0.2"))
        .build();
    when(coreMetadataRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(List.of(metadata));
    String latestVersion = coreVersionService.getLatestReleasedVersion(MOCK_PRODUCT_ID);
    assertEquals("10.0.2", latestVersion);
  }

  @Test
  void testUsesLatestVersionWhenVersionIsEmpty() {
    ProductJsonContent content = new ProductJsonContent();
    content.setContent("{}");
    content.setName(MOCK_PRODUCT_NAME);

//    when(coreMetadataRepository.findByProductId(MOCK_PRODUCT_ID))
//        .thenReturn(getMockMetadataWithVersions());

    when(coreProductJsonContentRepo.findByProductIdAndVersionIgnoreCase(
        eq(MOCK_PRODUCT_ID), anyString()))
        .thenReturn(List.of(content));

    Map<String, Object> result =
        coreVersionService.getProductJsonContentByIdAndVersion(MOCK_PRODUCT_ID, null);

    assertEquals(MOCK_PRODUCT_NAME, result.get("name"));
  }

  @Test
  void testGetArtifactsAndVersionToDisplay() {
    when(coreMavenArtifactVersionRepository.findByProductId(anyString())).thenReturn(List.of());
    when(coreMavenArtifactVersionRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(new ArrayList<>());

    Assertions.assertTrue(CollectionUtils.isEmpty(
            coreVersionService.getArtifactsAndVersionToDisplay(MOCK_PRODUCT_ID, false, MOCK_RELEASED_VERSION)),
        "Artifacts and version to be displayed should be empty");

    List<MavenArtifactVersion> proceededData = new ArrayList<>();

    MavenArtifactVersion mockModel = mockAdditionalMavenArtifactVersion(MOCK_RELEASED_VERSION, null);
    mockModel.setName(MOCK_PRODUCT_ID);
    mockModel.setDownloadUrl(CoreMavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    proceededData.add(mockModel);

    when(coreMavenArtifactVersionRepository.findByProductId(anyString())).thenReturn(proceededData);
    Assertions.assertTrue(ObjectUtils.isNotEmpty(
            coreVersionService.getArtifactsAndVersionToDisplay(MOCK_PRODUCT_ID, false, MOCK_RELEASED_VERSION)),
        "Artifacts and version to be displayed should not be empty");
  }

  @Test
  void testGetInstallableVersions() {
    List<String> mockVersions = List.of("11.3.0-SNAPSHOT", "11.1.1", "11.1.0", "10.0.2");
    Metadata mockMetadata = getMockMetadata();
    mockMetadata.setArtifactId(MOCK_PRODUCT_ARTIFACT_ID);
    mockMetadata.setVersions(new HashSet<>());
    mockMetadata.getVersions().addAll(mockVersions);

    when(productJsonLinkBuilder.buildProductJsonUrl(any(), any(), any()))
        .thenAnswer(invocation -> {
          String productId = invocation.getArgument(0);
          String version = invocation.getArgument(1);
          String designerVersion = invocation.getArgument(2);

          return "/api/product-details/" + productId + "/" + version +
              "/json?designerVersion=" + designerVersion;
        });

    List<VersionAndUrlModel> result = coreVersionService.getInstallableVersions(MOCK_PRODUCT_ID, true,
        MOCK_DESIGNER_VERSION);
    Assertions.assertTrue(CollectionUtils.isEmpty(result), "Installation version list should be empty");
    when(coreMetadataRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(List.of(mockMetadata));
    result = coreVersionService.getInstallableVersions(MOCK_PRODUCT_ID, true, MOCK_DESIGNER_VERSION);
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
}
