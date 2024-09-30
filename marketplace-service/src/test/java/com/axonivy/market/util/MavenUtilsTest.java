package com.axonivy.market.util;

import com.axonivy.market.bo.ArchivedArtifact;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.constants.ProductJsonConstants;
import com.axonivy.market.entity.Metadata;
import com.axonivy.market.model.MavenArtifactModel;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

class MavenUtilsTest {
  private static final String TEST_GROUP_ID = "com.axonivy.util";
  private static final String DEFAULT_TEST_MAVEN_URL = "https://maven.axonivy.com/com/axonivy/util/octopus/maven" +
      "-metadata.xml";
  private static Metadata buildMocKMetadata() {
    return Metadata.builder().url(
        DEFAULT_TEST_MAVEN_URL).repoUrl("https://maven.axonivy.com").groupId(TEST_GROUP_ID).artifactId("octopus").type(
        "zip").productId("octopus").build();
  }

  private static Artifact createMockArtifact() {
    Artifact artifact = new Artifact();
    artifact.setArtifactId("octopus");
    artifact.setGroupId(TEST_GROUP_ID);
    artifact.setRepoUrl(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL);
    return artifact;
  }

  @Test
  void testConvertArtifactsToModels() {
    // Assert case param is empty
    List<MavenArtifactModel> result = MavenUtils.convertArtifactsToModels(Collections.emptyList(), "10.0.21");
    Assertions.assertEquals(Collections.emptyList(), result);

    // Assert case param is null
    result = MavenUtils.convertArtifactsToModels(null, "10.0.21");
    Assertions.assertEquals(Collections.emptyList(), result);

    // Assert case param is a list with existed element
    Artifact targetArtifact = new Artifact(null, null, "com.axonivy.connector.adobe.acrobat.sign",
        "adobe-acrobat-sign-connector", null, null, null, null, false);
    result = MavenUtils.convertArtifactsToModels(List.of(targetArtifact), "10.0.21");
    Assertions.assertEquals(1, result.size());
  }

  @Test
  void testBuildDownloadUrlFromArtifactAndVersion() {
    // Set up artifact for testing
    String targetArtifactId = "adobe-acrobat-sign-connector";
    String targetGroupId = "com.axonivy.connector";
    Artifact targetArtifact = new Artifact(null, null, targetGroupId, targetArtifactId, "iar", null, null, null, false);
    String targetVersion = "10.0.10";
    String artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, targetArtifactId, targetVersion,
        "iar");

    // Assert case without archived artifact
    String expectedResult = String.join(CommonConstants.SLASH, MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL,
        "com/axonivy/connector", targetArtifactId, targetVersion, artifactFileName);
    String result = MavenUtils.buildDownloadUrl(targetArtifact, targetVersion);
    Assertions.assertEquals(expectedResult, result);

    // Assert case with artifact not match & use custom repo
    ArchivedArtifact adobeArchivedArtifactVersion9 = new ArchivedArtifact("10.0.9", "com.axonivy.adobe.connector",
        "adobe-connector");
    ArchivedArtifact adobeArchivedArtifactVersion8 = new ArchivedArtifact("10.0.8", "com.axonivy.adobe.sign.connector",
        "adobe-sign-connector");
    String customRepoUrl = "https://nexus.axonivy.com";
    targetArtifact.setRepoUrl(customRepoUrl);
    targetArtifact.setArchivedArtifacts(List.of(adobeArchivedArtifactVersion9, adobeArchivedArtifactVersion8));
    result = MavenUtils.buildDownloadUrl(targetArtifact, targetVersion);
    artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, targetArtifactId, targetVersion, "iar");
    expectedResult = String.join(CommonConstants.SLASH, customRepoUrl, "com/axonivy/connector", targetArtifactId,
        targetVersion, artifactFileName);
    Assertions.assertEquals(expectedResult, result);

    // Assert case with artifact got matching archived artifact & use custom file
    // type
    String customType = "zip";
    targetArtifact.setType(customType);
    targetVersion = "10.0.9";
    targetArtifact.setArchivedArtifacts(List.of(adobeArchivedArtifactVersion9, adobeArchivedArtifactVersion8));
    result = MavenUtils.buildDownloadUrl(targetArtifact, "10.0.9");
    artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, "adobe-connector", targetVersion,
        customType);
    expectedResult = String.join(CommonConstants.SLASH, customRepoUrl, "com/axonivy/adobe/connector", "adobe-connector",
        targetVersion, artifactFileName);
    Assertions.assertEquals(expectedResult, result);
  }

  @Test
  void testBuildSnapshotMetadataFromVersionUrlFromArtifactInfo() {
    Assertions.assertEquals(StringUtils.EMPTY,
        MavenUtils.buildSnapshotMetadataUrlFromArtifactInfo(null, null, null, null));
    Assertions.assertEquals(
        "https://maven.axonivy.com/com/axonivy/util/octopus-demo/1.0.0-SNAPSHOT/maven-metadata.xml",
        MavenUtils.buildSnapshotMetadataUrlFromArtifactInfo(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL,
            TEST_GROUP_ID, "octopus-demo", "1.0.0-SNAPSHOT"));
  }

  @Test
  void testBuildMetadataUrlFromArtifactInfo() {
    Assertions.assertEquals(StringUtils.EMPTY, MavenUtils.buildMetadataUrlFromArtifactInfo(null, null, null));

    Assertions.assertEquals("https://maven.axonivy.com/com/axonivy/util/octopus-demo/maven-metadata.xml",
        MavenUtils.buildMetadataUrlFromArtifactInfo(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL, TEST_GROUP_ID,
            "octopus-demo"));
  }

  @Test
  void testConvertArtifactToMetadata() {
    Artifact artifact = createMockArtifact();
    String metadataUrl = "https://maven.axonovy.com/com/axonivy/util/octopus-demo/maven-metadata.xml";
    Metadata result = MavenUtils.convertArtifactToMetadata("octopus", artifact,
        metadataUrl);
    Assertions.assertEquals(ProductJsonConstants.DEFAULT_PRODUCT_TYPE, result.getType());
    Assertions.assertEquals("Octopus (iar)", result.getName());
    Assertions.assertEquals(0, result.getVersions().size());
    Assertions.assertEquals(MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL, result.getRepoUrl());

    artifact.setName("octopus demo");
    result = MavenUtils.convertArtifactToMetadata("octopus", artifact,
        metadataUrl);
    Assertions.assertEquals("octopus demo (iar)", result.getName());
  }

  @Test
  void testBuildSnapShotMetadataFromVersion() {
    Metadata originalMetadata = buildMocKMetadata();
    Metadata snapShotMetadata = MavenUtils.buildSnapShotMetadataFromVersion(originalMetadata, "1.0.0-SNAPSHOT");
    Assertions.assertEquals(originalMetadata.getRepoUrl(), snapShotMetadata.getRepoUrl());
    Assertions.assertEquals(originalMetadata.getGroupId(), snapShotMetadata.getGroupId());
    Assertions.assertEquals(originalMetadata.getArtifactId(), snapShotMetadata.getArtifactId());
    Assertions.assertEquals(originalMetadata.getProductId(), snapShotMetadata.getProductId());
    Assertions.assertEquals(originalMetadata.getType(), snapShotMetadata.getType());
    Assertions.assertEquals("https://maven.axonivy.com/com/axonivy/util/octopus/1.0.0-SNAPSHOT/maven-metadata.xml",
        snapShotMetadata.getUrl());
  }

  @Test
  void testBuildMavenArtifactModelFromMetadata() {
    Metadata mocKMetadata = buildMocKMetadata();
    mocKMetadata.setSnapshotVersionValue("20241111-111111");
    MavenArtifactModel result = MavenUtils.buildMavenArtifactModelFromMetadata("1.0.0-SNAPSHOT", mocKMetadata);
    Assertions.assertEquals(
        "https://maven.axonivy.com/com/axonivy/util/octopus/1.0.0-SNAPSHOT/octopus-20241111-111111.zip",
        result.getDownloadUrl());
  }

  @Test
  void testConvertArtifactsToMetadataSet() {
    Artifact artifact = createMockArtifact();
    Set<Metadata> results = MavenUtils.convertArtifactsToMetadataSet(Set.of(artifact), "octopus");
    Assertions.assertEquals(1, results.size());
    Assertions.assertEquals(DEFAULT_TEST_MAVEN_URL, results.iterator().next().getUrl());
    results = MavenUtils.convertArtifactsToMetadataSet(Collections.emptySet(), "octopus");
    Assertions.assertEquals(0, results.size());

    ArchivedArtifact mockArchivedArtifact = new ArchivedArtifact();
    mockArchivedArtifact.setArtifactId("octopus-test");
    mockArchivedArtifact.setGroupId("com.octopus.util");
    artifact.setArchivedArtifacts(List.of(mockArchivedArtifact));

    results = MavenUtils.convertArtifactsToMetadataSet(Set.of(artifact), "octopus");
    Assertions.assertEquals(2, results.size());

  }

  @Test
  void testExtractMetaDataFromArchivedArtifacts() {
    Set<Metadata> results = MavenUtils.extractMetaDataFromArchivedArtifacts("octopus", new Artifact());
    Assertions.assertEquals(0, results.size());

    Artifact mockArtifact = createMockArtifact();
    ArchivedArtifact mockArchivedArtifact = new ArchivedArtifact();
    mockArchivedArtifact.setArtifactId("octopus-test");
    mockArchivedArtifact.setGroupId("com.octopus.util");
    mockArtifact.setArchivedArtifacts(List.of(mockArchivedArtifact));

    results = MavenUtils.extractMetaDataFromArchivedArtifacts("octopus", mockArtifact);

    Assertions.assertEquals(1, results.size());
    Assertions.assertEquals("https://maven.axonivy.com/com/octopus/util/octopus-test/maven-metadata.xml",
        results.iterator().next().getUrl());
  }

  @Test
  void testBuildDownloadUrl() {
    Metadata metadata = buildMocKMetadata();
    Assertions.assertEquals(
        "https://maven.axonivy.com/com/axonivy/util/octopus/1.0.0-SNAPSHOT/octopus-1.0.0-SNAPSHOT.zip",
        MavenUtils.buildDownloadUrl(metadata, "1.0.0-SNAPSHOT"));
  }

  @Test
  void testGetMetadataContent() {
    Assertions.assertEquals(StringUtils.EMPTY, MavenUtils.getMetadataContentFromUrl("octopus.com"));
  }

  @Test
  void testFFilterNonProductArtifactFromList() {
    Assertions.assertNull(MavenUtils.filterNonProductArtifactFromList(null));
    Assertions.assertEquals(0, MavenUtils.filterNonProductArtifactFromList(Collections.emptyList()).size());
  }
}
