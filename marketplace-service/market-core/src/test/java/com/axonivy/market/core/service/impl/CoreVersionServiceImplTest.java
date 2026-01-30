package com.axonivy.market.core.service.impl;

import com.axonivy.market.core.CoreBaseSetup;
import com.axonivy.market.core.constants.CoreMavenConstants;
import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.entity.Metadata;
import com.axonivy.market.core.entity.ProductJsonContent;
import com.axonivy.market.core.repository.CoreMavenArtifactVersionRepository;
import com.axonivy.market.core.repository.CoreMetadataRepository;
import com.axonivy.market.core.repository.CoreProductJsonContentRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CoreVersionServiceImplTest extends CoreBaseSetup {
  @Spy
  @InjectMocks
  private CoreVersionServiceImpl coreVersionService;
  @Mock
  private CoreMavenArtifactVersionRepository coreMavenArtifactVersionRepository;
  @Mock
  private CoreMetadataRepository coreMetadataRepository;
  @Mock
  private CoreProductJsonContentRepository coreProductJsonRepo;

  @Test
  void testGetProductJsonContentByIdAndVersion() {
    ProductJsonContent content = new ProductJsonContent();
    content.setContent("{\"name\":\"Test Product\"}");
    content.setName("Test Product");
    when(coreProductJsonRepo.findByProductIdAndVersion(Mockito.anyString(), Mockito.anyString()))
        .thenReturn(List.of(content));

    Map<String, Object> result = coreVersionService.getProductJsonContentByIdAndVersion(MOCK_PRODUCT_ID, MOCK_RELEASED_VERSION);
    assertNotNull(result);
    assertEquals("Test Product", result.get("name"));
  }

  @Test
  void testGetLatestInstallableVersion() {
    Metadata metadata = Metadata.builder()
        .isProductArtifact(true)
        .versions(Set.of("10.0.1", "10.0.2"))
        .build();
    when(coreMetadataRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(List.of(metadata));

    String latestVersion = coreVersionService.getLatestInstallableVersion(MOCK_PRODUCT_ID);
    assertEquals("10.0.2", latestVersion);
  }

  @Test
  void testGetArtifactsAndVersionToDisplay() {
    when(coreMavenArtifactVersionRepository.findByProductId(Mockito.anyString())).thenReturn(List.of());
    when(coreMavenArtifactVersionRepository.findByProductId(MOCK_PRODUCT_ID)).thenReturn(new ArrayList<>());

    Assertions.assertTrue(CollectionUtils.isEmpty(
            coreVersionService.getArtifactsAndVersionToDisplay(MOCK_PRODUCT_ID, false, MOCK_RELEASED_VERSION)),
        "Artifacts and version to be displayed should be empty");

    List<MavenArtifactVersion> proceededData = new ArrayList<>();

    MavenArtifactVersion mockModel = mockAdditionalMavenArtifactVersion(MOCK_RELEASED_VERSION, null);
    mockModel.setName(MOCK_PRODUCT_ID);
    mockModel.setDownloadUrl(CoreMavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    proceededData.add(mockModel);

    when(coreMavenArtifactVersionRepository.findByProductId(Mockito.anyString())).thenReturn(proceededData);
    Assertions.assertTrue(ObjectUtils.isNotEmpty(
            coreVersionService.getArtifactsAndVersionToDisplay(MOCK_PRODUCT_ID, false, MOCK_RELEASED_VERSION)),
        "Artifacts and version to be displayed should not be empty");
  }
}
