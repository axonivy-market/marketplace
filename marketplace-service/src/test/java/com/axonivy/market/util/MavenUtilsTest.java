package com.axonivy.market.util;

import com.axonivy.market.bo.ArchivedArtifact;
import com.axonivy.market.bo.Artifact;
import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.MavenConstants;
import com.axonivy.market.model.MavenArtifactModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

public class MavenUtilsTest {
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
    Artifact targetArtifact = new Artifact(null, null, targetGroupId, targetArtifactId, "iar", null, null,
        null, false);
    String targetVersion = "10.0.10";
    String artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, targetArtifactId, targetVersion,
        "iar");

    // Assert case without archived artifact
    String expectedResult = String.join(CommonConstants.SLASH,
        MavenConstants.DEFAULT_IVY_MAVEN_BASE_URL, "com/axonivy/connector", targetArtifactId, targetVersion,
        artifactFileName);
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
    targetArtifact.setArchivedArtifacts(List.of(adobeArchivedArtifactVersion9, adobeArchivedArtifactVersion8));
    result = MavenUtils.buildDownloadUrl(targetArtifact, "10.0.9");
    artifactFileName = String.format(MavenConstants.ARTIFACT_FILE_NAME_FORMAT, "adobe-connector", targetVersion,
        customType);
    expectedResult = String.join(CommonConstants.SLASH,
        customRepoUrl, "com/axonivy/adobe/connector", "adobe-connector", targetVersion,
        artifactFileName);
    Assertions.assertEquals(expectedResult, result);
  }
}
