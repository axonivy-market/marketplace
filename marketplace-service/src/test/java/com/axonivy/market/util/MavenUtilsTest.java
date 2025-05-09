package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.ArchivedArtifact;
import com.axonivy.market.entity.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.entity.Metadata;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.axonivy.market.constants.MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL;

class MavenUtilsTest extends BaseSetup {

  @Test
  void testBuildDownloadUrlFromArtifactAndVersion() {
    Artifact targetArtifact = getMockArtifact();
    String artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, MOCK_ARTIFACT_ID,
        MOCK_RELEASED_VERSION, MavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE);

    // Assert case without archived artifact
    String expectedResult = String.join(CommonConstants.SLASH, MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL,
        "com/axonivy/util", MOCK_ARTIFACT_ID, MOCK_RELEASED_VERSION, artifactFileName);
    String result = MavenUtils.buildDownloadUrl(getMockArtifact(), MOCK_RELEASED_VERSION);
    Assertions.assertEquals(expectedResult, result);

    // Assert case with artifact not match & use custom repo
    ArchivedArtifact adobeArchivedArtifactVersion9 = new ArchivedArtifact("10.0.9",
        "com.axonivy.adobe.connector", "adobe-connector");
    ArchivedArtifact adobeArchivedArtifactVersion8 = new ArchivedArtifact("10.0.8",
        "com.axonivy.adobe.sign.connector", "adobe-sign-connector");
    String customRepoUrl = "https://nexus.axonivy.com";
    targetArtifact.setRepoUrl(customRepoUrl);
    targetArtifact.setArchivedArtifacts(Set.of(adobeArchivedArtifactVersion9, adobeArchivedArtifactVersion8));
    result = MavenUtils.buildDownloadUrl(targetArtifact, MOCK_RELEASED_VERSION);
    artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, MOCK_ARTIFACT_ID, MOCK_RELEASED_VERSION
        , MavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE);
    expectedResult = String.join(CommonConstants.SLASH, customRepoUrl, "com/axonivy/util", MOCK_ARTIFACT_ID,
        MOCK_RELEASED_VERSION, artifactFileName);
    Assertions.assertEquals(expectedResult, result);

    // Assert case with artifact got matching archived artifact & use custom file type
    targetArtifact.setArchivedArtifacts(Set.of(adobeArchivedArtifactVersion9, adobeArchivedArtifactVersion8));
    result = MavenUtils.buildDownloadUrl(targetArtifact, "10.0.9");
    artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, "adobe-connector", "10.0.9",
        MavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE);
    expectedResult = String.join(CommonConstants.SLASH, customRepoUrl, "com/axonivy/adobe/connector",
        "adobe-connector", "10.0.9", artifactFileName);
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void testBuildSnapshotMetadataFromVersionUrlFromArtifactInfo() {
    Assertions.assertEquals(StringUtils.EMPTY,
        MavenUtils.buildSnapshotMetadataUrlFromArtifactInfo(null, null, null, null));
    Assertions.assertEquals(MOCK_SNAPSHOT_MAVEN_URL,
        MavenUtils.buildSnapshotMetadataUrlFromArtifactInfo(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL, MOCK_GROUP_ID,
            MOCK_ARTIFACT_ID, MOCK_SNAPSHOT_VERSION));
  }

  @Test
  void testExtractMavenArtifactsFromContentStream() throws IOException {
    InputStream mockInputStream = getMockInputStream();
    List<Artifact> result = MavenUtils.extractMavenArtifactsFromContentStream(mockInputStream);
    for (Artifact artifact : result) {
      Assertions.assertEquals(DEFAULT_IVY_MAVEN_BASE_URL, artifact.getRepoUrl());
    }

    mockInputStream = getMockProductJsonNodeContentInputStream();
    result = MavenUtils.extractMavenArtifactsFromContentStream(mockInputStream);
    for (Artifact artifact : result) {
      Assertions.assertEquals(DEFAULT_IVY_MAVEN_BASE_URL, artifact.getRepoUrl());
    }
  }

  @Test
  void testBuildMetadataUrlFromArtifactInfo() {
    Assertions.assertTrue(StringUtils.isEmpty(MavenUtils.buildMetadataUrlFromArtifactInfo(null, null, null)));
    Assertions.assertEquals(MOCK_MAVEN_URL,
        MavenUtils.buildMetadataUrlFromArtifactInfo(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL, MOCK_GROUP_ID,
            MOCK_ARTIFACT_ID));
  }

  @Test
  void testConvertArtifactToMetadata() {
    Artifact artifact = getMockArtifact();
    Metadata result = MavenUtils.convertArtifactToMetadata(MOCK_PRODUCT_ID, artifact,
        MOCK_MAVEN_URL);
    Assertions.assertEquals(MavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE, result.getType());
    Assertions.assertEquals(MOCK_ARTIFACT_NAME, result.getName());
    Assertions.assertTrue(CollectionUtils.isEmpty(result.getVersions()));
    Assertions.assertEquals(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL, result.getRepoUrl());

    artifact.setName("octopus demo");
    artifact.setType(ProductJsonConstants.DEFAULT_PRODUCT_TYPE);
    result = MavenUtils.convertArtifactToMetadata("octopus", artifact,
        MOCK_MAVEN_URL);
    Assertions.assertEquals("octopus demo (iar)", result.getName());
  }

  @Test
  void testBuildSnapShotMetadataFromVersion() {
    Metadata originalMetadata = buildMockMetadata();
    Metadata snapShotMetadata = MavenUtils.buildSnapShotMetadataFromVersion(originalMetadata,
        MOCK_SNAPSHOT_VERSION);
    Assertions.assertEquals(originalMetadata.getRepoUrl(), snapShotMetadata.getRepoUrl());
    Assertions.assertEquals(originalMetadata.getGroupId(), snapShotMetadata.getGroupId());
    Assertions.assertEquals(originalMetadata.getArtifactId(), snapShotMetadata.getArtifactId());
    Assertions.assertEquals(originalMetadata.getProductId(), snapShotMetadata.getProductId());
    Assertions.assertEquals(originalMetadata.getType(), snapShotMetadata.getType());
    Assertions.assertEquals(MOCK_SNAPSHOT_MAVEN_URL, snapShotMetadata.getUrl());
  }

  @Test
  void testBuildMavenArtifactVersionFromMetadata() {
    Metadata mocKMetadata = buildMockMetadata();
    mocKMetadata.setSnapshotVersionValue("20241111-111111");
    MavenArtifactVersion result = MavenUtils.buildMavenArtifactVersionFromMetadata(MOCK_SNAPSHOT_VERSION, mocKMetadata);
    Assertions.assertEquals(
        "https://maven.axonivy.com/com/axonivy/util/bpmn-statistic/10.0.10-SNAPSHOT/bpmn-statistic-20241111-111111.zip",
        result.getDownloadUrl());
  }

  @Test
  void testConvertArtifactsToMetadataSet() {
    Artifact artifact = getMockArtifact();
    Set<Metadata> results = MavenUtils.convertArtifactsToMetadataSet(Set.of(artifact), MOCK_PRODUCT_ID);
    Assertions.assertEquals(1, results.size());
    Assertions.assertEquals(MOCK_MAVEN_URL, results.iterator().next().getUrl());
    results = MavenUtils.convertArtifactsToMetadataSet(Collections.emptySet(), MOCK_PRODUCT_ID);
    Assertions.assertEquals(0, results.size());

    ArchivedArtifact mockArchivedArtifact = new ArchivedArtifact();
    mockArchivedArtifact.setArtifactId("octopus-test");
    mockArchivedArtifact.setGroupId("com.octopus.util");
    artifact.setArchivedArtifacts(Set.of(mockArchivedArtifact));

    results = MavenUtils.convertArtifactsToMetadataSet(Set.of(artifact), MOCK_PRODUCT_ID);
    Assertions.assertEquals(2, results.size());

  }

  @Test
  void testExtractMetaDataFromArchivedArtifacts() {
    Set<Metadata> results = MavenUtils.extractMetaDataFromArchivedArtifacts(MOCK_PRODUCT_ID, new Artifact());
    Assertions.assertTrue(CollectionUtils.isEmpty(results));
    Artifact mockArtifact = getMockArtifact();
    ArchivedArtifact mockArchivedArtifact = new ArchivedArtifact();
    mockArchivedArtifact.setArtifactId("octopus-test");
    mockArchivedArtifact.setGroupId("com.octopus.util");
    mockArtifact.setArchivedArtifacts(Set.of(mockArchivedArtifact));
    results = MavenUtils.extractMetaDataFromArchivedArtifacts(MOCK_PRODUCT_ID, mockArtifact);
    Assertions.assertTrue(ObjectUtils.isNotEmpty(results));
    Assertions.assertEquals("https://nexus-mirror.axonivy.com/repository/maven/com/octopus/util/octopus-test/maven-metadata.xml",
        results.iterator().next().getUrl());
  }

  @Test
  void testGetMetadataContent() {
    Assertions.assertEquals(StringUtils.EMPTY, MavenUtils.getMetadataContentFromUrl("octopus.com"));
  }

  @Test
  void testFFilterNonProductArtifactFromList() {
    Assertions.assertNull(MavenUtils.filterNonProductArtifactFromList(null));
    Assertions.assertTrue(
        CollectionUtils.isEmpty(MavenUtils.filterNonProductArtifactFromList(Collections.emptyList())));
  }

  @Test
  void testIsProductArtifactId() {
    Assertions.assertTrue(MavenUtils.isProductArtifactId(MOCK_PRODUCT_ARTIFACT_ID));
    Assertions.assertFalse(MavenUtils.isProductArtifactId(MOCK_PRODUCT_ID));
  }

  @Test
  void testFilterNonProductArtifactFromList() {
    Artifact productArtifact = getMockArtifact();
    productArtifact.setArtifactId(MOCK_PRODUCT_ARTIFACT_ID);
    List<Artifact> artifacts = List.of(productArtifact, getMockArtifact());
    List<Artifact> results = MavenUtils.filterNonProductArtifactFromList(artifacts);
    Assertions.assertEquals(1, results.size());
    Assertions.assertEquals(MOCK_PRODUCT_ID, results.get(0).getArtifactId());
  }

  @Test
  void testExtractedContentStream() throws IOException {
    Assertions.assertNull(MavenUtils.extractedContentStream(Path.of(INVALID_FILE_PATH)));
    InputStream expectedResult = IOUtils.toInputStream(getMockSnapShotMetadataContent(), StandardCharsets.UTF_8);
    InputStream result = MavenUtils.extractedContentStream(Path.of(MOCK_SNAPSHOT_METADATA_FILE_PATH));
    Assertions.assertNotNull(result);
    Assertions.assertTrue(IOUtils.contentEquals(expectedResult, result));
  }

  @Test
  void testConvertProductJsonToMavenProductInfo() {
    try {
      List<Artifact> result = MavenUtils.convertProductJsonToMavenProductInfo(Path.of(INVALID_FILE_PATH));
      Assertions.assertTrue(CollectionUtils.isEmpty(result));
      result = MavenUtils.convertProductJsonToMavenProductInfo(Path.of(MOCK_PRODUCT_JSON_FILE_PATH));
      Assertions.assertTrue(CollectionUtils.isEmpty(result));
      result = MavenUtils.convertProductJsonToMavenProductInfo(Path.of(MOCK_PRODUCT_JSON_DIR_PATH));
      Assertions.assertEquals(2, result.size());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testConvertArtifactIdToName() {
    String result = MavenUtils.convertArtifactIdToName(MOCK_ARTIFACT_ID);
    Assertions.assertEquals("Bpmn Statistic", result);

    result = MavenUtils.convertArtifactIdToName(null);
    Assertions.assertEquals(StringUtils.EMPTY, result);

    result = MavenUtils.convertArtifactIdToName(StringUtils.EMPTY);
    Assertions.assertEquals(StringUtils.EMPTY, result);

    result = MavenUtils.convertArtifactIdToName(" ");
    Assertions.assertEquals(StringUtils.EMPTY, result);
  }

  @Test
  void testGetDefaultMirrorMavenRepo() {
    String result = MavenUtils.getDefaultMirrorMavenRepo(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    Assertions.assertEquals(MavenConstants.DEFAULT_IVY_MIRROR_MAVEN_BASE_URL, result);
    final String ivyRepoUrl = "https://oss.sonatype.org/content/repositories/releases";
    result = MavenUtils.getDefaultMirrorMavenRepo(ivyRepoUrl);
    Assertions.assertEquals(ivyRepoUrl, result);
  }
}
