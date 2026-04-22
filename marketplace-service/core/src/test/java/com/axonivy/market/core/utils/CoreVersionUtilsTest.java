package com.axonivy.market.core.utils;

import com.axonivy.market.core.CoreBaseSetup;
import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.entity.key.MavenArtifactKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoreVersionUtilsTest extends CoreBaseSetup {


  @Test
  void testExtractAllVersions_ReleasedOnly() {
    List<MavenArtifactVersion> artifacts = List.of(
        createArtifact(MOCK_FIRST_RELEASED_VERSION_FOR_TEN),
        createArtifact(MOCK_BUGFIX_VERSION),
        createArtifact(MOCK_SNAPSHOT_VERSION)
    );

    List<String> result = CoreVersionUtils.extractAllVersions(artifacts, false);

    assertEquals(2, result.size());
    assertEquals(MOCK_BUGFIX_VERSION, result.get(0));
    assertEquals(MOCK_FIRST_RELEASED_VERSION_FOR_TEN, result.get(1));
    result = CoreVersionUtils.extractAllVersions(List.of(createArtifact(MOCK_ALPHA_VERSION)), false);
    assertEquals(1, result.size(), "should return all version if there is no record of official release version");
  }

  @Test
  void testExtractAllVersions_WithDevVersions() {
    List<MavenArtifactVersion> artifacts = List.of(
        createArtifact(MOCK_FIRST_RELEASED_VERSION_FOR_TEN),
        createArtifact(MOCK_SNAPSHOT_VERSION),
        createArtifact(MOCK_ALPHA_VERSION)
    );

    List<String> result = CoreVersionUtils.extractAllVersions(artifacts, true);

    assertEquals(3, result.size());
    assertTrue(result.contains(MOCK_FIRST_RELEASED_VERSION_FOR_TEN));
    assertTrue(result.contains(MOCK_SNAPSHOT_VERSION));
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
    versionFromArtifact.add("10.0.2-m123");
    versionFromArtifact.add("10.0.1-a1");

    Assertions.assertEquals(versionFromArtifact,
        CoreVersionUtils.getVersionsToDisplay(versionFromArtifact, true),
        "Displayed versions should include snapshots when 'show dev versions' is selected");
    Assertions.assertEquals(3,
        CoreVersionUtils.getVersionsToDisplay(versionFromArtifact, false).size(),
        "Displayed versions should exclude snapshots when snapshot filter is enabled");
  }

  @Test
  void testShouldReturnNullWhenVersionsIsEmpty() {
    String result = CoreVersionUtils.getBestMatchVersion(List.of(), MOCK_RELEASED_VERSION, true);
    assertNull(result, "Expected null when versions list is empty");
  }

  @Test
  void testShouldReturnExactMatchWhenExists() {
    List<String> versions = List.of("1.0.0", "1.1.0", "2.0.0");
    String result = CoreVersionUtils.getBestMatchVersion(versions, "1.1.0", false);

    assertEquals("1.1.0", result,
        "Expected exact match '1.1.0' to be returned when it exists in versions");
  }

  @Test
  void testShouldReturnPriorDevVersionWhenAllowed() {
    List<String> versions = List.of("1.0.0-SNAPSHOT", "1.1.0-SNAPSHOT");

    String result = CoreVersionUtils.getBestMatchVersion(versions, "1.2.0", true);
    assertEquals("1.0.0-SNAPSHOT", result,
        "Expected a prior dev version when allowDevVersion=true and no released version exists");
  }

  @Test
  void testShouldNotReturnDevVersionWhenNotAllowed() {
    List<String> versions = List.of("1.0.0-SNAPSHOT", "1.1.0-SNAPSHOT");

    String result = CoreVersionUtils.getBestMatchVersion(versions, "1.2.0", false);
    assertEquals("1.0.0-SNAPSHOT", result,
        "Expected fallback to first element when dev versions are not allowed and no released version exists");
  }

  @Test
  void testShouldDelegateToMainMethodWithAllowDevVersionTrue() {
    List<String> versions = List.of("1.0.0-SNAPSHOT", "1.1.0-SNAPSHOT");
    String resultFromOverload =
        CoreVersionUtils.getBestMatchVersion(versions, "1.2.0");
    String resultFromMain =
        CoreVersionUtils.getBestMatchVersion(versions, "1.2.0", true);

    assertEquals(resultFromMain, resultFromOverload,
        "Expected overload method to behave the same as main method with allowDevVersion=true");
  }

  @Test
  void testIsDevVersion() {
    Assertions.assertTrue(CoreVersionUtils.isDevVersion("1.0.0-a1"),
        "Expected sprint released version to be identified as sprint");

    Assertions.assertFalse(CoreVersionUtils.isDevVersion("1.0.0"),
        "Released version should not be identified as sprint");
  }

  @Test
  void testIsReleasedVersion() {
    Assertions.assertTrue(CoreVersionUtils.isReleasedVersion("1.0.0"),
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
}
