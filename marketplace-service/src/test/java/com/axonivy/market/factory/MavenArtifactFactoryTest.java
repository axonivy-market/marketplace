package com.axonivy.market.factory;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.github.model.ArchivedArtifact;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.github.service.GHAxonIvyProductRepoService;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import com.axonivy.market.repository.ProductJsonContentRepository;
import com.axonivy.market.repository.ProductModuleContentRepository;
import com.axonivy.market.repository.ProductRepository;
import com.axonivy.market.service.impl.VersionServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.MavenConstants.*;

@ExtendWith(MockitoExtension.class)
class MavenArtifactFactoryTest {
  private static final String IAR_TYPE = "iar";
  private static final String VERSION_10 = "10.0.10";
  private static final String ARTIFACT_ID_10 = "adobe-acrobat-sign-connector";
  private static final String GROUP_ID_10 = "com.axonivy.connector";
  private static final String VERSION_9 = "10.0.9";
  private static final String GROUP_ID_9 = "com.axonivy.adobe.connector";
  private static final String ARTIFACT_ID_9 = "adobe-connector";
  private static final String VERSION_8 = "10.0.8";
  private static final String GROUP_ID_8 = "com.axonivy.adobe.sign.connector";
  private static final String ARTIFACT_ID_8 = "adobe-sign-connector";

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

  @Test
  void testBuildDownloadUrlFromArtifactAndVersion() {
    // Set up artifact for testing
    var targetArtifact = MavenArtifact.builder().groupId(GROUP_ID_10).artifactId(ARTIFACT_ID_10).type(IAR_TYPE).build();
    var artifactFileName = String.format(ARTIFACT_FILE_NAME_FORMAT, ARTIFACT_ID_10, VERSION_10, IAR_TYPE);

    // Assert case without archived artifact
    var expectedResult = String.join(SLASH, DEFAULT_IVY_MAVEN_BASE_URL,
        GROUP_ID_10.replaceAll(MAIN_VERSION_REGEX, SLASH), ARTIFACT_ID_10, VERSION_10, artifactFileName);
    String result = MavenArtifactFactory.buildDownloadUrlByVersion(targetArtifact, VERSION_10);
    Assertions.assertEquals(expectedResult, result, "Assert case without archived artifact");

    // Assert case with artifact not match & use custom repo
    String customRepoUrl = "https://nexus.axonivy.com";
    targetArtifact.setRepoUrl(customRepoUrl);
    result = MavenArtifactFactory.buildDownloadUrlByVersion(targetArtifact, VERSION_10);
    artifactFileName = String.format(ARTIFACT_FILE_NAME_FORMAT, ARTIFACT_ID_10, VERSION_10, IAR_TYPE);
    expectedResult = String.join(SLASH, customRepoUrl, GROUP_ID_10.replaceAll(MAIN_VERSION_REGEX, SLASH),
        ARTIFACT_ID_10, VERSION_10, artifactFileName);
    Assertions.assertEquals(expectedResult, result);

    // Assert case with artifact got matching archived artifact & use custom file type
    mockArchivedArtifact(targetArtifact);
    String customType = "zip";
    targetArtifact.setType(customType);

    result = MavenArtifactFactory.buildDownloadUrlByVersion(targetArtifact, VERSION_9);
    artifactFileName = String.format(ARTIFACT_FILE_NAME_FORMAT, ARTIFACT_ID_9, VERSION_9, customType);
    expectedResult = String.join(CommonConstants.SLASH, customRepoUrl, GROUP_ID_9.replaceAll(MAIN_VERSION_REGEX, SLASH),
        ARTIFACT_ID_9, VERSION_9, artifactFileName);
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void testFindArchivedArtifactInfoBestMatchWithVersion() {
    ArchivedArtifact result = MavenArtifactFactory.findArchivedArtifactBestMatchVersion(VERSION_10,
        Collections.emptyList());
    Assertions.assertNull(result);

    // Assert case with target version higher than all of latest version from archived artifact list
    ArchivedArtifact adobeArchivedArtifactVersion8 = new ArchivedArtifact(VERSION_8, GROUP_ID_10, ARTIFACT_ID_8);
    ArchivedArtifact adobeArchivedArtifactVersion9 = new ArchivedArtifact(VERSION_9, GROUP_ID_10, ARTIFACT_ID_9);
    List<ArchivedArtifact> archivedArtifacts = new ArrayList<>();
    archivedArtifacts.add(adobeArchivedArtifactVersion8);
    archivedArtifacts.add(adobeArchivedArtifactVersion9);
    result = MavenArtifactFactory.findArchivedArtifactBestMatchVersion(VERSION_10, archivedArtifacts);
    Assertions.assertNull(result);

    // Assert case with target version less than all of latest version from archived artifact list
    result = MavenArtifactFactory.findArchivedArtifactBestMatchVersion("10.0.7", archivedArtifacts);
    Assertions.assertEquals(adobeArchivedArtifactVersion8, result);

    // Assert case with target version is in range of archived artifact list
    ArchivedArtifact adobeArchivedArtifactVersion10 = new ArchivedArtifact(VERSION_10, GROUP_ID_10, ARTIFACT_ID_8);
    archivedArtifacts.add(adobeArchivedArtifactVersion10);

    result = MavenArtifactFactory.findArchivedArtifactBestMatchVersion(VERSION_10, archivedArtifacts);
    Assertions.assertEquals(adobeArchivedArtifactVersion10, result);
  }

  private void mockArchivedArtifact(MavenArtifact mavenArtifact) {
    var adobeArchivedArtifactVersion9 = new ArchivedArtifact(VERSION_9, GROUP_ID_9, ARTIFACT_ID_9);
    var adobeArchivedArtifactVersion8 = new ArchivedArtifact(VERSION_8, GROUP_ID_8, ARTIFACT_ID_8);
    mavenArtifact.setArchivedArtifacts(List.of(adobeArchivedArtifactVersion8, adobeArchivedArtifactVersion9));
  }
}
