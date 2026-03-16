package com.axonivy.market.core.utils;

import com.axonivy.market.core.CoreBaseSetup;
import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.entity.Metadata;
import com.axonivy.market.core.entity.key.MavenArtifactKey;
import com.axonivy.market.core.repository.CoreProductJsonContentRepository;
import com.axonivy.market.core.service.CoreVersionService;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class CoreVersionUtilsTest extends CoreBaseSetup {

  @Mock
  private CoreProductJsonContentRepository coreProductJsonContentRepository;

  @InjectMocks
  private CoreVersionService coreVersionService;

  @Test
  void testExtractAllVersions_ReleasedOnly() {
    List<MavenArtifactVersion> artifacts = List.of(
        createArtifact("10.0.0"),
        createArtifact("10.0.1"),
        createArtifact("10.0.2-SNAPSHOT")
    );

    List<String> result = CoreVersionUtils.extractAllVersions(artifacts, false);

    assertEquals(2, result.size());
    assertEquals("10.0.1", result.get(0));
    assertEquals("10.0.0", result.get(1));
  }

  @Test
  void testExtractAllVersions_WithDevVersions() {
    List<MavenArtifactVersion> artifacts = List.of(
        createArtifact("10.0.0"),
        createArtifact("10.0.0-SNAPSHOT"),
        createArtifact("10.0.1-SNAPSHOT")
    );

    List<String> result = CoreVersionUtils.extractAllVersions(artifacts, true);

    assertEquals(2, result.size());
    assertTrue(result.contains("10.0.0"));
    assertTrue(result.contains("10.0.1-SNAPSHOT"));
  }

  private MavenArtifactVersion createArtifact(String version) {
    MavenArtifactKey key = new MavenArtifactKey();
    key.setProductVersion(version);
    MavenArtifactVersion artifact = new MavenArtifactVersion();
    artifact.setId(key);
    return artifact;
  }

  @Test
  void testGetVersionsToDisplay() {
    ArrayList<String> versionFromArtifact = new ArrayList<>();
    versionFromArtifact.add("10.0.6");
    versionFromArtifact.add("10.0.5");
    versionFromArtifact.add("10.0.4");
    versionFromArtifact.add("10.0.3-SNAPSHOT");

    Assertions.assertEquals(versionFromArtifact,
        CoreVersionUtils.getVersionsToDisplay(versionFromArtifact, true),
        "Displayed versions should include snapshots when 'show dev versions' is selected");

    versionFromArtifact.remove(versionFromArtifact.size() - 1);

    Assertions.assertEquals(versionFromArtifact,
        CoreVersionUtils.getVersionsToDisplay(versionFromArtifact, false),
        "Displayed versions should exclude snapshots when snapshot filter is enabled");
  }

  @Test
  void testIsSprintVersion() {
    Assertions.assertTrue(CoreVersionUtils.isSprintVersion(MOCK_SPRINT_RELEASED_VERSION),
        "Expected sprint released version to be identified as sprint");
    Assertions.assertFalse(CoreVersionUtils.isSprintVersion(MOCK_SNAPSHOT_VERSION),
        "Snapshot version should not be identified as sprint");
    Assertions.assertFalse(CoreVersionUtils.isSprintVersion(MOCK_RELEASED_VERSION),
        "Released version should not be identified as sprint");
  }

  @Test
  void testIsReleasedVersion() {
    Assertions.assertTrue(CoreVersionUtils.isReleasedVersion(MOCK_RELEASED_VERSION),
        "Released version should be identified as released");
    Assertions.assertFalse(CoreVersionUtils.isReleasedVersion(MOCK_SNAPSHOT_VERSION),
        "Snapshot version should not be identified as released");
    Assertions.assertFalse(CoreVersionUtils.isReleasedVersion(MOCK_SPRINT_RELEASED_VERSION),
        "Sprint released version should not be identified as released");
  }

  @Test
  void testGetBugfixVersion() {
    String shortReleasedVersion = "10.0";

    Assertions.assertEquals(MOCK_RELEASED_VERSION, CoreVersionUtils.getBugfixVersion(MOCK_RELEASED_VERSION),
        "Bugfix version of released version should equal released version");
    Assertions.assertEquals(MOCK_RELEASED_VERSION, CoreVersionUtils.getBugfixVersion(MOCK_SNAPSHOT_VERSION),
        "Bugfix version of snapshot version should equal released version");
    Assertions.assertEquals(MOCK_RELEASED_VERSION, CoreVersionUtils.getBugfixVersion(MOCK_BUGFIX_VERSION),
        "Bugfix version of bugfix version should equal released version");
    Assertions.assertEquals(MOCK_RELEASED_VERSION, CoreVersionUtils.getBugfixVersion(MOCK_SPRINT_RELEASED_VERSION),
        "Bugfix version of sprint released version should equal released version");
    Assertions.assertEquals(shortReleasedVersion, CoreVersionUtils.getBugfixVersion(shortReleasedVersion),
        "Bugfix version of short released version should equal short version");
  }

  @Test
  void testGetInstallableVersionsFromMetadataList() {
    Metadata mockProductMeta1 = getMockMetadataWithVersions();
    mockProductMeta1.setArtifactId(MOCK_PRODUCT_ARTIFACT_ID);
    Metadata mockProductMeta2 = getMockMetadataWithVersions();
    mockProductMeta2.setArtifactId(MOCK_PRODUCT_ARTIFACT_ID);
    mockProductMeta2.setVersions(Set.of(MOCK_BUGFIX_VERSION));

    Assertions.assertEquals(3, mockProductMeta1.getVersions().size(),
        "Expected mockProductMeta1 to contain 3 versions");
    Assertions.assertEquals(1, mockProductMeta2.getVersions().size(),
        "Expected mockProductMeta2 to contain 1 version");

    List<String> results = CoreVersionUtils.getInstallableVersionsFromMetadataList(
        List.of(mockProductMeta1, mockProductMeta2));
    Assertions.assertEquals(4, results.size(),
        "Expected total installable versions from both metadata objects to be 4");
  }

  @Test
  void testGetCompatibilityRangeFromSingleVersion() {
    List<String> versions = List.of("10.2.5");
    String result = CoreVersionUtils.getCompatibilityRangeFromVersions(versions, false);

    assertEquals("10.2+", result,
        "Single version should return its major.minor with '+' suffix");
  }

  @Test
  void testGetCompatibilityRangeFromVersionsSameMajorVersion() {
    List<String> versions = List.of(
        "10.3.2",
        "10.2.4",
        "10.1.1"
    );
    String result = CoreVersionUtils.getCompatibilityRangeFromVersions(versions, false);

    assertEquals("10.1+", result,
        "Versions with the same major prefix should return the minimum version with '+' suffix");
  }

  @Test
  void testGetCompatibilityRangeFromVersionsDifferentMajorVersions() {
    List<String> versions = List.of(
        "11.0.1",
        "10.3.4",
        "9.2.1"
    );
    String result = CoreVersionUtils.getCompatibilityRangeFromVersions(versions, false);

    assertEquals("9.2 - 11.0+", result,
        "Versions with different major prefixes should return a formatted compatibility range");
  }

  @Test
  void testGetCompatibilityRangeFromVersionsDeprecatedProduct() {
    List<String> versions = List.of("10.2.5");
    String result = CoreVersionUtils.getCompatibilityRangeFromVersions(versions, true);

    assertEquals("10.2", result,
        "Deprecated products should not include '+' suffix in compatibility range");
  }

  @Test
  void testGetBestMatchVersionEmptyVersions() {
    String result = CoreVersionUtils.getBestMatchVersion(List.of(), "10.0.0");

    assertNull(result, "Empty version list should return null");
  }

  @Test
  void testGetBestMatchVersionExactMatch() {
    List<String> versions = List.of("10.0.0", "9.9.0");
    String result = CoreVersionUtils.getBestMatchVersion(versions, "10.0.0");

    assertEquals("10.0.0", result, "Exact designer version match should be returned");
  }

  @Test
  void testGetBestMatchVersionPriorReleasedVersion() {
    List<String> versions = List.of("10.0.0", "9.5.0");
    String result = CoreVersionUtils.getBestMatchVersion(versions, "10.1.0");

    assertEquals("10.0.0", result, "Closest prior released version should be returned when exact match is not found");
  }

  @Test
  void testGetBestMatchVersionPriorDevVersion() {
    List<String> versions = List.of("10.0.0-SNAPSHOT", "11.0.0-SNAPSHOT");
    String result = CoreVersionUtils.getBestMatchVersion(versions, "12.0.0", true);

    assertEquals("10.0.0-SNAPSHOT", result,
        "Prior development version should be returned when allowDevVersion is true");
  }

  @Test
  void testGetBestMatchVersionFallbackReleasedVersion() {
    List<String> versions = List.of(
        "11.0.0",
        "12.0.0"
    );
    String result = CoreVersionUtils.getBestMatchVersion(versions, "10.0.0");

    assertEquals("11.0.0", result,
        "If no prior version exists, the first available released version should be returned");
  }

  @Test
  void testGetBestMatchVersionFallbackFirstElement() {
    List<String> versions = List.of(
        "11.0.0-SNAPSHOT",
        "12.0.0-SNAPSHOT"
    );
    String result = CoreVersionUtils.getBestMatchVersion(versions, "10.0.0", false);

    assertEquals("11.0.0-SNAPSHOT", result,
        "If no released versions exist and dev versions are not allowed, the first element should be returned");
  }
}
