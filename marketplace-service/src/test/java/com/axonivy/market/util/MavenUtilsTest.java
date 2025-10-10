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
    Assertions.assertEquals(expectedResult, result,
        "Released version download URL should match expected URL when there is no archived artifact");

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
    Assertions.assertEquals(expectedResult, result,
        "Released version download URL should match expected URL when artifact versions do not match and use custom " +
            "repo");

    // Assert case with artifact got matching archived artifact & use custom file type
    targetArtifact.setArchivedArtifacts(Set.of(adobeArchivedArtifactVersion9, adobeArchivedArtifactVersion8));
    result = MavenUtils.buildDownloadUrl(targetArtifact, "10.0.9");
    artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, "adobe-connector", "10.0.9",
        MavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE);
    expectedResult = String.join(CommonConstants.SLASH, customRepoUrl, "com/axonivy/adobe/connector",
        "adobe-connector", "10.0.9", artifactFileName);
    Assertions.assertEquals(expectedResult, result,
        "Released version download URL should match expected URL when archived artifact versions match and use " +
            "custom file type");
  }

  @Test
  void testBuildSnapshotMetadataFromVersionUrlFromArtifactInfo() {
    Assertions.assertEquals(StringUtils.EMPTY,
        MavenUtils.buildSnapshotMetadataUrlFromArtifactInfo(null, null, null, null),
        "Snapshot metadata URL should be empty");
    Assertions.assertEquals(MOCK_SNAPSHOT_MAVEN_URL,
        MavenUtils.buildSnapshotMetadataUrlFromArtifactInfo(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL, MOCK_GROUP_ID,
            MOCK_ARTIFACT_ID, MOCK_SNAPSHOT_VERSION),
        "Snapshot metadata URL should match expected snapshot maven URL");
  }

  @Test
  void testExtractMavenArtifactsFromContentStream() throws IOException {
    InputStream mockInputStream = getMockInputStream();
    List<Artifact> result = MavenUtils.extractMavenArtifactsFromContentStream(mockInputStream);
    for (Artifact artifact : result) {
      Assertions.assertEquals(DEFAULT_IVY_MAVEN_BASE_URL, artifact.getRepoUrl(),
          "Artifact repo URL should match default Ivy Maven base URL");
    }

    mockInputStream = getMockProductJsonNodeContentInputStream();
    result = MavenUtils.extractMavenArtifactsFromContentStream(mockInputStream);
    for (Artifact artifact : result) {
      Assertions.assertEquals(DEFAULT_IVY_MAVEN_BASE_URL, artifact.getRepoUrl(),
          "Artifact repo URL should match default Ivy Maven base URL");
    }
  }

  @Test
  void testBuildMetadataUrlFromArtifactInfo() {
    Assertions.assertTrue(StringUtils.isEmpty(MavenUtils.buildMetadataUrlFromArtifactInfo(null, null, null)),
        "Snapshot metadata URL built from artifact info should be null");
    Assertions.assertEquals(MOCK_MAVEN_URL,
        MavenUtils.buildMetadataUrlFromArtifactInfo(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL, MOCK_GROUP_ID,
            MOCK_ARTIFACT_ID), "Snapshot metadata URL built from artifact info should match expected maven URL");
  }

  @Test
  void testConvertArtifactToMetadata() {
    Artifact artifact = getMockArtifact();
    Metadata result = MavenUtils.convertArtifactToMetadata(MOCK_PRODUCT_ID, artifact,
        MOCK_MAVEN_URL);
    Assertions.assertEquals(MavenConstants.DEFAULT_PRODUCT_FOLDER_TYPE, result.getType(),
        "Metadata type should match default product folder type");
    Assertions.assertEquals(MOCK_ARTIFACT_NAME, result.getName(),
        "Metadata name should match artifact name");
    Assertions.assertTrue(CollectionUtils.isEmpty(result.getVersions()),
        "Metadata versions should be empty");
    Assertions.assertEquals(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL, result.getRepoUrl(),
        "Metadata repo URL should match default Ivy maven base URL");

    artifact.setName("octopus demo");
    artifact.setType(ProductJsonConstants.DEFAULT_PRODUCT_TYPE);
    result = MavenUtils.convertArtifactToMetadata("octopus", artifact,
        MOCK_MAVEN_URL);
    Assertions.assertEquals("octopus demo (iar)", result.getName(),
        "Metadata name should be 'octopus demo (iar)'");
  }

  @Test
  void testBuildSnapShotMetadataFromVersion() {
    Metadata originalMetadata = buildMockMetadata();
    Metadata snapShotMetadata = MavenUtils.buildSnapShotMetadataFromVersion(originalMetadata,
        MOCK_SNAPSHOT_VERSION);
    Assertions.assertEquals(originalMetadata.getRepoUrl(), snapShotMetadata.getRepoUrl(),
        "Snapshot metadata repo URL should match original metadata repo URL");
    Assertions.assertEquals(originalMetadata.getGroupId(), snapShotMetadata.getGroupId(),
        "Snapshot metadata group ID should match original metadata group ID");
    Assertions.assertEquals(originalMetadata.getArtifactId(), snapShotMetadata.getArtifactId(),
        "Snapshot metadata artifact ID should match original metadata artifact ID");
    Assertions.assertEquals(originalMetadata.getProductId(), snapShotMetadata.getProductId(),
        "Snapshot metadata product ID should match original metadata product ID");
    Assertions.assertEquals(originalMetadata.getType(), snapShotMetadata.getType(),
        "Snapshot metadata type should match original metadata type");
    Assertions.assertEquals(MOCK_SNAPSHOT_MAVEN_URL, snapShotMetadata.getUrl(),
        "Snapshot metadata URL should match snapshot maven URL");
  }

  @Test
  void testBuildMavenArtifactVersionFromMetadata() {
    Metadata mocKMetadata = buildMockMetadata();
    mocKMetadata.setSnapshotVersionValue("20241111-111111");
    MavenArtifactVersion result = MavenUtils.buildMavenArtifactVersionFromMetadata(MOCK_SNAPSHOT_VERSION, mocKMetadata);
    Assertions.assertEquals(
        "https://maven.axonivy.com/com/axonivy/util/bpmn-statistic/10.0.10-SNAPSHOT/bpmn-statistic-20241111-111111.zip",
        result.getDownloadUrl(),
        "Maven artifact version built from metadata should match input");
  }

  @Test
  void testConvertArtifactsToMetadataSet() {
    Artifact artifact = getMockArtifact();
    Set<Metadata> results = MavenUtils.convertArtifactsToMetadataSet(Set.of(artifact), MOCK_PRODUCT_ID);
    Assertions.assertEquals(1, results.size(),
        "Metadata set size should be 1");
    Assertions.assertEquals(MOCK_MAVEN_URL, results.iterator().next().getUrl(),
        "Metadata URL should match maven URL");
    results = MavenUtils.convertArtifactsToMetadataSet(Collections.emptySet(), MOCK_PRODUCT_ID);
    Assertions.assertEquals(0, results.size(),
        "Metadata set size should be empty");

    ArchivedArtifact mockArchivedArtifact = new ArchivedArtifact();
    mockArchivedArtifact.setArtifactId("octopus-test");
    mockArchivedArtifact.setGroupId("com.octopus.util");
    artifact.setArchivedArtifacts(Set.of(mockArchivedArtifact));

    results = MavenUtils.convertArtifactsToMetadataSet(Set.of(artifact), MOCK_PRODUCT_ID);
    Assertions.assertEquals(2, results.size(),
        "Metadata set size should be 2");

  }

  @Test
  void testExtractMetaDataFromArchivedArtifacts() {
    Set<Metadata> results = MavenUtils.extractMetaDataFromArchivedArtifacts(MOCK_PRODUCT_ID, new Artifact());
    Assertions.assertTrue(CollectionUtils.isEmpty(results),
        "Metadata from archived artifacts should be empty");
    Artifact mockArtifact = getMockArtifact();
    ArchivedArtifact mockArchivedArtifact = new ArchivedArtifact();
    mockArchivedArtifact.setArtifactId("octopus-test");
    mockArchivedArtifact.setGroupId("com.octopus.util");
    mockArtifact.setArchivedArtifacts(Set.of(mockArchivedArtifact));
    results = MavenUtils.extractMetaDataFromArchivedArtifacts(MOCK_PRODUCT_ID, mockArtifact);
    Assertions.assertTrue(ObjectUtils.isNotEmpty(results),
        "Metadata from archived artifacts should not be empty");
    Assertions.assertEquals(
        "https://nexus-mirror.axonivy.com/repository/maven/com/octopus/util/octopus-test/maven-metadata.xml",
        results.iterator().next().getUrl(),
        "Metadata URL should match expected input");
  }

  @Test
  void testFFilterNonProductArtifactFromList() {
    Assertions.assertNull(MavenUtils.filterNonProductArtifactFromList(null),
        "Non product artifact list should be null if artifacts from metadata is null");
    Assertions.assertTrue(
        CollectionUtils.isEmpty(MavenUtils.filterNonProductArtifactFromList(Collections.emptyList())),
        "Non product artifact list should be empty");
  }

  @Test
  void testIsProductArtifactId() {
    Assertions.assertTrue(MavenUtils.isProductArtifactId(MOCK_PRODUCT_ARTIFACT_ID),
        "Expected true for a valid product artifact ID");
    Assertions.assertFalse(MavenUtils.isProductArtifactId(MOCK_PRODUCT_ID),
        "Expected false for a non-artifact product ID");
  }

  @Test
  void testFilterNonProductArtifactFromList() {
    Artifact productArtifact = getMockArtifact();
    productArtifact.setArtifactId(MOCK_PRODUCT_ARTIFACT_ID);
    List<Artifact> artifacts = List.of(productArtifact, getMockArtifact());
    List<Artifact> results = MavenUtils.filterNonProductArtifactFromList(artifacts);
    Assertions.assertEquals(1, results.size(),
        "Non product artifact list size should be 1");
    Assertions.assertEquals(MOCK_PRODUCT_ID, results.get(0).getArtifactId(),
        "Non product artifact ID should match mock product ID");
  }

  @Test
  void testExtractedContentStream() throws IOException {
    Assertions.assertNull(MavenUtils.extractedContentStream(Path.of(INVALID_FILE_PATH)),
        "Content stream should be null if file path is invalid");
    InputStream expectedResult = IOUtils.toInputStream(getMockSnapShotMetadataContent(), StandardCharsets.UTF_8);
    InputStream result = MavenUtils.extractedContentStream(Path.of(MOCK_SNAPSHOT_METADATA_FILE_PATH));
    Assertions.assertNotNull(result,
        "Content stream from snapshot metadata should not be null");
    Assertions.assertTrue(IOUtils.contentEquals(expectedResult, result),
        "Content stream from snapshot metadata should match content stream of mock snapshot metadata");
  }

  @Test
  void testConvertProductJsonToMavenProductInfo() {
    try {
      List<Artifact> result = MavenUtils.convertProductJsonToMavenProductInfo(Path.of(INVALID_FILE_PATH));
      Assertions.assertTrue(CollectionUtils.isEmpty(result),
          "Artifact list should be empty if folder path is invalid");
      result = MavenUtils.convertProductJsonToMavenProductInfo(Path.of(MOCK_PRODUCT_JSON_FILE_PATH));
      Assertions.assertTrue(CollectionUtils.isEmpty(result),
          "Artifact list should be empty product json does not exist");
      result = MavenUtils.convertProductJsonToMavenProductInfo(Path.of(MOCK_PRODUCT_JSON_DIR_PATH));
      Assertions.assertEquals(2, result.size(),
          "Artifact list should be 2");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testConvertArtifactIdToName() {
    String result = MavenUtils.convertArtifactIdToName(MOCK_ARTIFACT_ID);
    Assertions.assertEquals("Bpmn Statistic", result,
        "Artifact name should match input name");

    result = MavenUtils.convertArtifactIdToName(null);
    Assertions.assertEquals(StringUtils.EMPTY, result,
        "Artifact name should be empty if artifact ID is null");

    result = MavenUtils.convertArtifactIdToName(StringUtils.EMPTY);
    Assertions.assertEquals(StringUtils.EMPTY, result,
        "Artifact name should be empty if artifact ID is empty");

    result = MavenUtils.convertArtifactIdToName(" ");
    Assertions.assertEquals(StringUtils.EMPTY, result,
        "Artifact name should be empty if artifact ID is blank");
  }

  @Test
  void testGetDefaultMirrorMavenRepo() {
    String result = MavenUtils.getDefaultMirrorMavenRepo(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    Assertions.assertEquals(MavenConstants.DEFAULT_IVY_MIRROR_MAVEN_BASE_URL, result,
        "Default mirror maven repo should match default ivy mirror maven base URL");
    final String ivyRepoUrl = "https://oss.sonatype.org/content/repositories/releases";
    result = MavenUtils.getDefaultMirrorMavenRepo(ivyRepoUrl);
    Assertions.assertEquals(ivyRepoUrl, result,
        "Default mirror maven repo should match input ivy repo URL");
  }
}
