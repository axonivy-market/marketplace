package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.Metadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class VersionUtilsTest extends BaseSetup {

  @Test
  void testIsSnapshotVersion() {
    Assertions.assertTrue(VersionUtils.isSnapshotVersion(MOCK_SNAPSHOT_VERSION),
        "Expected snapshot version to be identified as snapshot");
    Assertions.assertFalse(VersionUtils.isSnapshotVersion(MOCK_SPRINT_RELEASED_VERSION),
        "Sprint released version should not be identified as snapshot");
    Assertions.assertFalse(VersionUtils.isSnapshotVersion(MOCK_RELEASED_VERSION),
        "Released version should not be identified as snapshot");
  }

  @Test
  void testIsSprintVersion() {
    Assertions.assertTrue(VersionUtils.isSprintVersion(MOCK_SPRINT_RELEASED_VERSION),
        "Expected sprint released version to be identified as sprint");
    Assertions.assertFalse(VersionUtils.isSprintVersion(MOCK_SNAPSHOT_VERSION),
        "Snapshot version should not be identified as sprint");
    Assertions.assertFalse(VersionUtils.isSprintVersion(MOCK_RELEASED_VERSION),
        "Released version should not be identified as sprint");
  }

  @Test
  void testIsReleasedVersion() {
    Assertions.assertTrue(VersionUtils.isReleasedVersion(MOCK_RELEASED_VERSION),
        "Released version should be identified as released");
    Assertions.assertFalse(VersionUtils.isReleasedVersion(MOCK_SNAPSHOT_VERSION),
        "Snapshot version should not be identified as released");
    Assertions.assertFalse(VersionUtils.isReleasedVersion(MOCK_SPRINT_RELEASED_VERSION),
        "Sprint released version should not be identified as released");
  }

  @Test
  void testIsMatchWithDesignerVersion() {
    Assertions.assertTrue(VersionUtils.isMatchWithDesignerVersion(MOCK_BUGFIX_VERSION, MOCK_RELEASED_VERSION),
        "Bugfix version should match with released version");
    Assertions.assertFalse(VersionUtils.isMatchWithDesignerVersion(MOCK_SNAPSHOT_VERSION, MOCK_RELEASED_VERSION),
        "Snapshot version should not match with released version");

    String targetVersion = "10.0.9";
    Assertions.assertFalse(VersionUtils.isMatchWithDesignerVersion(targetVersion, MOCK_RELEASED_VERSION),
        "Target version 10.0.9 should not match with released version");
  }

  @Test
  void testGetVersionsToDisplay() {
    ArrayList<String> versionFromArtifact = new ArrayList<>();
    versionFromArtifact.add("10.0.6");
    versionFromArtifact.add("10.0.5");
    versionFromArtifact.add("10.0.4");
    versionFromArtifact.add("10.0.3-SNAPSHOT");

    Assertions.assertEquals(versionFromArtifact,
        VersionUtils.getVersionsToDisplay(versionFromArtifact, true),
        "Displayed versions should include snapshots when 'show dev versions' is selected");

    versionFromArtifact.remove(versionFromArtifact.size() - 1);

    Assertions.assertEquals(versionFromArtifact,
        VersionUtils.getVersionsToDisplay(versionFromArtifact, false),
        "Displayed versions should exclude snapshots when snapshot filter is enabled");
  }

  @Test
  void testIsReleasedVersionOrUnReleaseDevVersion() {
    String unreleasedSprintVersion = "10.0.21-m1235";
    List<String> versions = List.of(MOCK_RELEASED_VERSION, MOCK_SNAPSHOT_VERSION, MOCK_BUGFIX_VERSION,
        MOCK_SPRINT_RELEASED_VERSION, unreleasedSprintVersion);

    Assertions.assertTrue(
        VersionUtils.isOfficialVersionOrUnReleasedDevVersion(versions, MOCK_RELEASED_VERSION),
        "Released version should be recognized as official or unreleased dev");
    Assertions.assertFalse(
        VersionUtils.isOfficialVersionOrUnReleasedDevVersion(versions, MOCK_SNAPSHOT_VERSION),
        "Snapshot version should not be recognized as official or unreleased dev");
    Assertions.assertTrue(
        VersionUtils.isOfficialVersionOrUnReleasedDevVersion(versions, MOCK_BUGFIX_VERSION),
        "Bugfix version should be recognized as official or unreleased dev");
    Assertions.assertFalse(
        VersionUtils.isOfficialVersionOrUnReleasedDevVersion(versions, MOCK_SPRINT_RELEASED_VERSION),
        "Released sprint version should not be recognized as unreleased dev");
    Assertions.assertTrue(
        VersionUtils.isOfficialVersionOrUnReleasedDevVersion(versions, unreleasedSprintVersion),
        "Unreleased sprint version should be recognized as unreleased dev");
  }

  @Test
  void testGetBugfixVersion() {
    String shortReleasedVersion = "10.0";

    Assertions.assertEquals(MOCK_RELEASED_VERSION, VersionUtils.getBugfixVersion(MOCK_RELEASED_VERSION),
        "Bugfix version of released version should equal released version");
    Assertions.assertEquals(MOCK_RELEASED_VERSION, VersionUtils.getBugfixVersion(MOCK_SNAPSHOT_VERSION),
        "Bugfix version of snapshot version should equal released version");
    Assertions.assertEquals(MOCK_RELEASED_VERSION, VersionUtils.getBugfixVersion(MOCK_BUGFIX_VERSION),
        "Bugfix version of bugfix version should equal released version");
    Assertions.assertEquals(MOCK_RELEASED_VERSION, VersionUtils.getBugfixVersion(MOCK_SPRINT_RELEASED_VERSION),
        "Bugfix version of sprint released version should equal released version");
    Assertions.assertEquals(shortReleasedVersion, VersionUtils.getBugfixVersion(shortReleasedVersion),
        "Bugfix version of short released version should equal short version");
  }

  @Test
  void testGetBestMatchVersion() {
    List<String> releasedVersions = List.of("10.0.21-SNAPSHOT", "10.0.21", "10.0.19", "10.0.17");

    Assertions.assertEquals("10.0.19", VersionUtils.getBestMatchVersion(releasedVersions, "10.0.19"),
        "Exact version 10.0.19 should be matched");
    Assertions.assertEquals("10.0.21", VersionUtils.getBestMatchVersion(releasedVersions, "10.0.22"),
        "Closest lower version to 10.0.22 should be 10.0.21");
    Assertions.assertEquals("10.0.17", VersionUtils.getBestMatchVersion(releasedVersions, "10.0.18"),
        "Closest lower version to 10.0.18 should be 10.0.17");
    Assertions.assertEquals("10.0.21", VersionUtils.getBestMatchVersion(releasedVersions, "10.0.16"),
        "Closest higher version to 10.0.16 should be 10.0.21");
  }

  @Test
  void testRemoveSyncedVersionsFromReleasedVersions() {
    Set<String> syncVersion = Set.of("1.0.0");
    List<String> releasedVersions = new ArrayList<>();
    releasedVersions.add("1.0.0");
    releasedVersions.add("2.0.0");

    List<String> result = VersionUtils.removeSyncedVersionsFromReleasedVersions(releasedVersions,
        Collections.emptySet());
    Assertions.assertEquals(2, result.size(),
        "When no versions are synced, both versions should remain");

    result = VersionUtils.removeSyncedVersionsFromReleasedVersions(releasedVersions, syncVersion);
    Assertions.assertEquals(1, result.size(),
        "After removing synced version, only one version should remain");
    Assertions.assertEquals("2.0.0", result.get(0),
        "Remaining version should be 2.0.0 after removing 1.0.0");
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

    List<String> results = VersionUtils.getInstallableVersionsFromMetadataList(
        List.of(mockProductMeta1, mockProductMeta2));
    Assertions.assertEquals(4, results.size(),
        "Expected total installable versions from both metadata objects to be 4");
  }
}
