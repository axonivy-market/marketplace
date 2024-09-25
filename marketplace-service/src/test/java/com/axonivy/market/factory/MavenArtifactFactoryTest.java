package com.axonivy.market.factory;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.github.model.ArchivedArtifact;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.impl.VersionServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class MavenArtifactFactoryTest {
  private Map<String, List<ArchivedArtifact>> archivedArtifactsMap;
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
  }

  @Test
  void testBuildDownloadUrlFromArtifactAndVersion() {
    // Set up artifact for testing
    String targetArtifactId = "adobe-acrobat-sign-connector";
    String targetGroupId = "com.axonivy.connector";
    MavenArtifact targetArtifact = new MavenArtifact(null, null, targetGroupId, targetArtifactId, "iar", null, null,
        null, false);
    String targetVersion = "10.0.10";
    String artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, targetArtifactId, targetVersion,
        "iar");

    // Assert case without archived artifact
    String expectedResult = String.join(CommonConstants.SLASH,
        MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL, "com/axonivy/connector", targetArtifactId, targetVersion,
        artifactFileName);
    String result = MavenArtifactFactory.buildDownloadUrlByVersion(targetArtifact, targetVersion);
    Assertions.assertEquals(expectedResult, result);

    // Assert case with artifact not match & use custom repo
    ArchivedArtifact adobeArchivedArtifactVersion9 = new ArchivedArtifact("10.0.9", "com.axonivy.adobe.connector",
        "adobe-connector");
    ArchivedArtifact adobeArchivedArtifactVersion8 = new ArchivedArtifact("10.0.8", "com.axonivy.adobe.sign.connector",
        "adobe-sign-connector");
    String customRepoUrl = "https://nexus.axonivy.com";
    targetArtifact.setRepoUrl(customRepoUrl);
    result = MavenArtifactFactory.buildDownloadUrlByVersion(targetArtifact, targetVersion);
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
    result = MavenArtifactFactory.buildDownloadUrlByVersion(targetArtifact, "10.0.9");
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
    ArchivedArtifact result = MavenArtifactFactory.findArchivedArtifactBestMatchVersion(
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
    result = MavenArtifactFactory.findArchivedArtifactBestMatchVersion(targetVersion,
        archivedArtifacts);
    Assertions.assertNull(result);

    // Assert case with target version less than all of latest version from archived
    // artifact list
    result = MavenArtifactFactory.findArchivedArtifactBestMatchVersion("10.0.7",
        archivedArtifacts);
    Assertions.assertEquals(adobeArchivedArtifactVersion8, result);

    // Assert case with target version is in range of archived artifact list
    ArchivedArtifact adobeArchivedArtifactVersion10 = new ArchivedArtifact("10.0.10", "com.axonivy.connector",
        "adobe-sign-connector");

    archivedArtifactsMap.get(targetArtifactId).add(adobeArchivedArtifactVersion10);
    result = MavenArtifactFactory.findArchivedArtifactBestMatchVersion(targetVersion,
        archivedArtifacts);
    Assertions.assertEquals(adobeArchivedArtifactVersion10, result);
  }
}
