package com.axonivy.market.core.utils;

import com.axonivy.market.core.entity.MavenArtifactVersion;
import com.axonivy.market.core.entity.key.MavenArtifactKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoreVersionUtilsTest {

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
  void testShouldReturnNullWhenVersionsIsEmpty() {
    String result = CoreVersionUtils.getBestMatchVersion(List.of(), "1.0.0", true);
    assertNull(result, "Expected null when versions list is empty");
  }

  @Test
  void ttShouldReturnExactMatchWhenExists() {
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
}
