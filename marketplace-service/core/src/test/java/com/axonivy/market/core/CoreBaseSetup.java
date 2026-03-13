package com.axonivy.market.core;

import com.axonivy.market.core.constants.CoreMavenConstants;
import com.axonivy.market.core.entity.Metadata;
import com.axonivy.market.core.entity.ProductMarketplaceData;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.entity.key.MavenArtifactKey;
import com.axonivy.market.core.enums.SortOption;

import java.util.Set;

public class CoreBaseSetup {
  protected static final String MOCK_DOWNLOAD_URL = "https://maven.axonivy.com/com/axonivy/util/bpmn-statistic/10.0" +
      ".10/bpmn-statistic-10.0.10.zip";
  protected static final String MOCK_PRODUCT_ID = "bpmn-statistic";
  protected static final String MOCK_RELEASED_VERSION = "10.0.10";
  protected static final String SAMPLE_PRODUCT_ID = "amazon-comprehend";
  protected static final String SAMPLE_PRODUCT_NAME = "prody Comprehend";
  protected static final String MOCK_SPRINT_RELEASED_VERSION = "10.0.10-m123";
  protected static final String MOCK_SNAPSHOT_VERSION = "10.0.10-SNAPSHOT";
  protected static final String MOCK_BUGFIX_VERSION = "10.0.10.1";
  protected static final String MOCK_PRODUCT_ARTIFACT_ID = "bpmn-statistic-product";
  protected static final String MOCK_ARTIFACT_ID = "bpmn-statistic";
  protected static final String MOCK_GROUP_ID = "com.axonivy.util";
  protected static final String MOCK_ARTIFACT_NAME = "bpmn statistic (zip)";
  protected static final String INSTALLATION_FILE_PATH = "src/test/resources/installationCount.json";
  protected static final String LEGACY_INSTALLATION_COUNT_PATH_FIELD_NAME = "legacyInstallationCountPath";
  protected static final String MOCK_DESIGNER_VERSION = "12.0.4";
  protected static final Pageable PAGEABLE = PageRequest.of(0, 20,
    Sort.by(SortOption.ALPHABETICALLY.getOption()).descending());

  protected MavenArtifactVersion mockAdditionalMavenArtifactVersion(String version, String artifactId) {
    MavenArtifactKey mavenArtifactKey = MavenArtifactKey.builder()
        .productVersion(version)
        .artifactId(artifactId)
        .isAdditionalVersion(true)
        .build();
    return MavenArtifactVersion.builder().id(mavenArtifactKey).downloadUrl(MOCK_DOWNLOAD_URL).build();
  }

  protected Metadata getMockMetadataWithVersions() {
    Metadata mockMetadata = getMockMetadata();
    mockMetadata.setRelease(MOCK_RELEASED_VERSION);
    mockMetadata.setLatest(MOCK_SPRINT_RELEASED_VERSION);
    mockMetadata.setVersions(
        Set.of(MOCK_SNAPSHOT_VERSION, MOCK_RELEASED_VERSION, MOCK_SPRINT_RELEASED_VERSION));
    return mockMetadata;
  }

  protected Metadata getMockMetadata() {
    return Metadata.builder().productId(MOCK_PRODUCT_ID).artifactId(MOCK_ARTIFACT_ID).groupId(
            MOCK_GROUP_ID).isProductArtifact(true).repoUrl(CoreMavenConstants.DEFAULT_IVY_MAVEN_BASE_URL).type(
            CoreMavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE)
        .name(MOCK_ARTIFACT_NAME).build();
  }

  protected ProductMarketplaceData getMockProductMarketplaceData() {
    return ProductMarketplaceData.builder().id(MOCK_PRODUCT_ID).installationCount(3).build();
  }
}
