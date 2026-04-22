package com.axonivy.market.core;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.entity.key.MavenArtifactKey;
import com.axonivy.market.core.enums.SortOption;

public class CoreBaseSetup {
  protected static final String MOCK_DOWNLOAD_URL = "https://maven.axonivy.com/com/axonivy/util/bpmn-statistic/10.0" +
      ".10/bpmn-statistic-10.0.10.zip";
  protected static final String MOCK_PRODUCT_ID = "bpmn-statistic";
  protected static final String MOCK_RELEASED_VERSION = "10.0.10";
  protected static final String MOCK_FIRST_RELEASED_VERSION_FOR_TEN = "10.0.0";
  protected static final String MOCK_SNAPSHOT_VERSION = "10.0.10-SNAPSHOT";
  protected static final String MOCK_BUGFIX_VERSION = "10.0.10.1";
  protected static final String MOCK_SPRINT_RELEASED_VERSION = "10.0.10-m123";
  protected static final String MOCK_ALPHA_VERSION = "10.0.10-a123";
  protected static final String SAMPLE_PRODUCT_ID = "amazon-comprehend";
  protected static final String SAMPLE_PRODUCT_NAME = "prody Comprehend";
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
}
