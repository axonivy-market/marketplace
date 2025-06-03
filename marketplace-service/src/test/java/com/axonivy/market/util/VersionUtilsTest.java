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
    Assertions.assertTrue(VersionUtils.isSnapshotVersion(MOCK_SNAPSHOT_VERSION));
    Assertions.assertFalse(VersionUtils.isSnapshotVersion(MOCK_SPRINT_RELEASED_VERSION));
    Assertions.assertFalse(VersionUtils.isSnapshotVersion(MOCK_RELEASED_VERSION));
  }

  @Test
  void testIsSprintVersion() {
    Assertions.assertTrue(VersionUtils.isSprintVersion(MOCK_SPRINT_RELEASED_VERSION));
    Assertions.assertFalse(VersionUtils.isSprintVersion(MOCK_SNAPSHOT_VERSION));
    Assertions.assertFalse(VersionUtils.isSprintVersion(MOCK_RELEASED_VERSION));
  }

  @Test
  void testIsReleasedVersion() {
    Assertions.assertTrue(VersionUtils.isReleasedVersion(MOCK_RELEASED_VERSION));
    Assertions.assertFalse(VersionUtils.isReleasedVersion(MOCK_SNAPSHOT_VERSION));
    Assertions.assertFalse(VersionUtils.isReleasedVersion(MOCK_SPRINT_RELEASED_VERSION));
  }

  @Test
  void testIsMatchWithDesignerVersion() {
    Assertions.assertTrue(VersionUtils.isMatchWithDesignerVersion(MOCK_BUGFIX_VERSION, MOCK_RELEASED_VERSION));
    Assertions.assertFalse(VersionUtils.isMatchWithDesignerVersion(MOCK_SNAPSHOT_VERSION,
        MOCK_RELEASED_VERSION));

    String targetVersion = "10.0.9";
    Assertions.assertFalse(VersionUtils.isMatchWithDesignerVersion(targetVersion, MOCK_RELEASED_VERSION));
  }

  @Test
  void testGetVersionsToDisplay() {
    ArrayList<String> versionFromArtifact = new ArrayList<>();
    versionFromArtifact.add("10.0.6");
    versionFromArtifact.add("10.0.5");
    versionFromArtifact.add("10.0.4");
    versionFromArtifact.add("10.0.3-SNAPSHOT");
    Assertions.assertEquals(versionFromArtifact, VersionUtils.getVersionsToDisplay(versionFromArtifact, true));
    versionFromArtifact.remove(versionFromArtifact.size()-1);
    Assertions.assertEquals(versionFromArtifact, VersionUtils.getVersionsToDisplay(versionFromArtifact, false));
  }


  @Test
  void testIsReleasedVersionOrUnReleaseDevVersion() {
    String unreleasedSprintVersion = "10.0.21-m1235";
    List<String> versions = List.of(MOCK_RELEASED_VERSION, MOCK_SNAPSHOT_VERSION, MOCK_BUGFIX_VERSION,
        MOCK_SPRINT_RELEASED_VERSION, unreleasedSprintVersion);
    Assertions.assertTrue(VersionUtils.isOfficialVersionOrUnReleasedDevVersion(versions, MOCK_RELEASED_VERSION));
    Assertions.assertFalse(
        VersionUtils.isOfficialVersionOrUnReleasedDevVersion(versions, MOCK_SNAPSHOT_VERSION));
    Assertions.assertTrue(VersionUtils.isOfficialVersionOrUnReleasedDevVersion(versions, MOCK_BUGFIX_VERSION));
    Assertions.assertFalse(
        VersionUtils.isOfficialVersionOrUnReleasedDevVersion(versions, MOCK_SPRINT_RELEASED_VERSION));
    Assertions.assertTrue(VersionUtils.isOfficialVersionOrUnReleasedDevVersion(versions, unreleasedSprintVersion));
  }

  @Test
  void testGetBugfixVersion() {
    String shortReleasedVersion = "10.0";
    Assertions.assertEquals(MOCK_RELEASED_VERSION, VersionUtils.getBugfixVersion(MOCK_RELEASED_VERSION));
    Assertions.assertEquals(MOCK_RELEASED_VERSION, VersionUtils.getBugfixVersion(MOCK_SNAPSHOT_VERSION));
    Assertions.assertEquals(MOCK_RELEASED_VERSION, VersionUtils.getBugfixVersion(MOCK_BUGFIX_VERSION));
    Assertions.assertEquals(MOCK_RELEASED_VERSION, VersionUtils.getBugfixVersion(MOCK_SPRINT_RELEASED_VERSION));
    Assertions.assertEquals(shortReleasedVersion, VersionUtils.getBugfixVersion(shortReleasedVersion));

  }

  @Test
  void testGetBestMatchVersion() {
    List<String> releasedVersions = List.of("10.0.21-SNAPSHOT", "10.0.21", "10.0.19", "10.0.17");
    Assertions.assertEquals("10.0.19", VersionUtils.getBestMatchVersion(releasedVersions, "10.0.19"));
    Assertions.assertEquals("10.0.21", VersionUtils.getBestMatchVersion(releasedVersions, "10.0.22"));
    Assertions.assertEquals("10.0.17", VersionUtils.getBestMatchVersion(releasedVersions, "10.0.18"));
    Assertions.assertEquals("10.0.21", VersionUtils.getBestMatchVersion(releasedVersions, "10.0.16"));
  }

  @Test
  void testRemoveSyncedVersionsFromReleasedVersions() {
    Set syncVersion = Set.of("1.0.0");
    List<String> releasedVersions = new ArrayList<>();
    releasedVersions.add("1.0.0");
    releasedVersions.add("2.0.0");
    List<String> result = VersionUtils.removeSyncedVersionsFromReleasedVersions(releasedVersions,
        Collections.emptySet());
    Assertions.assertEquals(2, result.size());
    result = VersionUtils.removeSyncedVersionsFromReleasedVersions(releasedVersions,
        syncVersion);
    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals("2.0.0", result.get(0));
  }

  @Test
  void testGetInstallableVersionsFromMetadataList() {
    Metadata mockProductMeta1 = getMockMetadataWithVersions();
    mockProductMeta1.setArtifactId(MOCK_PRODUCT_ARTIFACT_ID);
    Metadata mockProductMeta2 = getMockMetadataWithVersions();
    mockProductMeta2.setArtifactId(MOCK_PRODUCT_ARTIFACT_ID);
    mockProductMeta2.setVersions(Set.of(MOCK_BUGFIX_VERSION));

    Assertions.assertEquals(3, mockProductMeta1.getVersions().size());
    Assertions.assertEquals(1, mockProductMeta2.getVersions().size());


    List<String> results = VersionUtils.getInstallableVersionsFromMetadataList(
        List.of(mockProductMeta1, mockProductMeta2));
    Assertions.assertEquals(4, results.size());
  }
}
