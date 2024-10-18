package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.Product;
import com.axonivy.market.enums.NonStandardProduct;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHTag;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
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
  void testConvertVersionToTag() {
    Assertions.assertEquals(StringUtils.EMPTY, VersionUtils.convertVersionToTag(StringUtils.EMPTY, StringUtils.EMPTY));
    Assertions.assertEquals(MOCK_RELEASED_VERSION,
        VersionUtils.convertVersionToTag(NonStandardProduct.PORTAL.getId(), MOCK_RELEASED_VERSION));
    Assertions.assertEquals(MOCK_TAG_FROM_RELEASED_VERSION,
        VersionUtils.convertVersionToTag(NonStandardProduct.GRAPHQL_DEMO.getId(), MOCK_RELEASED_VERSION));
  }

  @Test
  void testGetVersionsToDisplay() {
    ArrayList<String> versionFromArtifact = new ArrayList<>();
    versionFromArtifact.add("10.0.6");
    versionFromArtifact.add("10.0.5");
    versionFromArtifact.add("10.0.4");
    versionFromArtifact.add("10.0.3-SNAPSHOT");
    Assertions.assertEquals(versionFromArtifact, VersionUtils.getVersionsToDisplay(versionFromArtifact, true, null));
    Assertions.assertEquals(List.of("10.0.5"), VersionUtils.getVersionsToDisplay(versionFromArtifact, null, "10.0.5"));
    versionFromArtifact.remove("10.0.3-SNAPSHOT");
    Assertions.assertEquals(versionFromArtifact, VersionUtils.getVersionsToDisplay(versionFromArtifact, null, null));
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
  void testConvertTagToVersion() {
    Assertions.assertEquals(MOCK_RELEASED_VERSION, VersionUtils.convertTagToVersion(MOCK_RELEASED_VERSION));
    Assertions.assertEquals(MOCK_RELEASED_VERSION, VersionUtils.convertTagToVersion(MOCK_TAG_FROM_RELEASED_VERSION));
    Assertions.assertEquals(StringUtils.EMPTY, VersionUtils.convertTagToVersion(StringUtils.EMPTY));
  }

  @Test
  void testConvertTagsToVersions() {
    List<String> results = VersionUtils.convertTagsToVersions(List.of("10.0.1", MOCK_TAG_FROM_RELEASED_VERSION));
    Assertions.assertEquals(2, results.size());
    Assertions.assertEquals("10.0.1", results.get(0));
    Assertions.assertEquals(MOCK_RELEASED_VERSION, results.get(1));
  }

  @Test
  void testGetOldestVersionWithEmptyTags() {
    List<GHTag> tags = List.of();
    String oldestTag = VersionUtils.getOldestTag(tags);
    Assertions.assertEquals(StringUtils.EMPTY, oldestTag);
  }

  @Test
  void testGetOldestVersionWithNullTags() {
    String oldestTag = VersionUtils.getOldestTag(null);
    Assertions.assertEquals(StringUtils.EMPTY, oldestTag);
  }

  @Test
  void testGetOldestVersionWithNonNumericCharacters() {
    GHTag tag1 = Mockito.mock(GHTag.class);
    GHTag tag2 = Mockito.mock(GHTag.class);
    Mockito.when(tag1.getName()).thenReturn("v1.0");
    Mockito.when(tag2.getName()).thenReturn("2.1");
    List<GHTag> tags = Arrays.asList(tag1, tag2);
    String oldestTag = VersionUtils.getOldestTag(tags);
    Assertions.assertEquals("1.0", oldestTag);
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
  void testGetReleaseTagsFromProduct() {
    List<String> result = VersionUtils.getReleaseTagsFromProduct(null);
    Assertions.assertNotNull(result);
    Assertions.assertTrue(CollectionUtils.isEmpty(result));
    Product mockProduct = Product.builder().id("portal").releasedVersions(List.of("1.0.0")).build();
    result = VersionUtils.getReleaseTagsFromProduct(mockProduct);
    Assertions.assertTrue(ObjectUtils.isNotEmpty(result));
    Assertions.assertEquals("1.0.0", result.get(0));
  }
}
