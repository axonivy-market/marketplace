package com.axonivy.market.util;

import com.axonivy.market.enums.NonStandardProduct;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)

class VersionUtilsTest {
    @InjectMocks
    private VersionUtils versionUtils;

    @Test
    void testIsSnapshotVersion() {
        String targetVersion = "10.0.21-SNAPSHOT";
        Assertions.assertTrue(VersionUtils.isSnapshotVersion(targetVersion));

        targetVersion = "10.0.21-m1234";
        Assertions.assertFalse(VersionUtils.isSnapshotVersion(targetVersion));

        targetVersion = "10.0.21";
        Assertions.assertFalse(VersionUtils.isSnapshotVersion(targetVersion));
    }

    @Test
    void testIsSprintVersion() {
        String targetVersion = "10.0.21-m1234";
        Assertions.assertTrue(VersionUtils.isSprintVersion(targetVersion));

        targetVersion = "10.0.21-SNAPSHOT";
        Assertions.assertFalse(VersionUtils.isSprintVersion(targetVersion));

        targetVersion = "10.0.21";
        Assertions.assertFalse(VersionUtils.isSprintVersion(targetVersion));
    }

    @Test
    void testIsReleasedVersion() {
        String targetVersion = "10.0.21";
        Assertions.assertTrue(VersionUtils.isReleasedVersion(targetVersion));

        targetVersion = "10.0.21-SNAPSHOT";
        Assertions.assertFalse(VersionUtils.isReleasedVersion(targetVersion));

        targetVersion = "10.0.21-m1231";
        Assertions.assertFalse(VersionUtils.isReleasedVersion(targetVersion));
    }

    @Test
    void testIsMatchWithDesignerVersion() {
        String designerVersion = "10.0.21";
        String targetVersion = "10.0.21.2";
        Assertions.assertTrue(VersionUtils.isMatchWithDesignerVersion(targetVersion, designerVersion));

        targetVersion = "10.0.21-SNAPSHOT";
        Assertions.assertFalse(VersionUtils.isMatchWithDesignerVersion(targetVersion, designerVersion));

        targetVersion = "10.0.19";
        Assertions.assertFalse(VersionUtils.isMatchWithDesignerVersion(targetVersion, designerVersion));
    }

    @Test
    void testConvertVersionToTag() {

        String rawVersion = StringUtils.EMPTY;
        Assertions.assertEquals(rawVersion, VersionUtils.convertVersionToTag(StringUtils.EMPTY, rawVersion));

        rawVersion = "11.0.0";
        String tag = "11.0.0";
        Assertions.assertEquals(tag, VersionUtils.convertVersionToTag(NonStandardProduct.PORTAL.getId(), rawVersion));

        tag = "v11.0.0";
        Assertions.assertEquals(tag, VersionUtils.convertVersionToTag(NonStandardProduct.GRAPHQL_DEMO.getId(), rawVersion));
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
        String releasedVersion = "10.0.20";
        String snapshotVersion = "10.0.20-SNAPSHOT";
        String sprintVersion = "10.0.20-m1234";
        String minorSprintVersion = "10.0.20.1-m1234";
        String unreleasedSprintVersion = "10.0.21-m1235";
        List<String> versions = List.of(releasedVersion, snapshotVersion, sprintVersion, unreleasedSprintVersion);
        Assertions.assertTrue(VersionUtils.isOfficialVersionOrUnReleasedDevVersion(versions, releasedVersion));
        Assertions.assertFalse(VersionUtils.isOfficialVersionOrUnReleasedDevVersion(versions, sprintVersion));
        Assertions.assertFalse(VersionUtils.isOfficialVersionOrUnReleasedDevVersion(versions, snapshotVersion));
        Assertions.assertFalse(VersionUtils.isOfficialVersionOrUnReleasedDevVersion(versions, minorSprintVersion));
        Assertions.assertTrue(VersionUtils.isOfficialVersionOrUnReleasedDevVersion(versions, unreleasedSprintVersion));
    }

    @Test
    void testGetBugfixVersion() {
        String releasedVersion = "10.0.20";
        String shortReleasedVersion = "10.0";
        String snapshotVersion = "10.0.20-SNAPSHOT";
        String sprintVersion = "10.0.20-m1234";
        String minorSprintVersion = "10.0.20.1-m1234";
        Assertions.assertEquals(releasedVersion, VersionUtils.getBugfixVersion(releasedVersion));
        Assertions.assertEquals(releasedVersion, VersionUtils.getBugfixVersion(snapshotVersion));
        Assertions.assertEquals(releasedVersion, VersionUtils.getBugfixVersion(sprintVersion));
        Assertions.assertEquals(releasedVersion, VersionUtils.getBugfixVersion(minorSprintVersion));
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
        Assertions.assertEquals("10.0.19", VersionUtils.convertTagToVersion("10.0.19"));
        Assertions.assertEquals("10.0.19", VersionUtils.convertTagToVersion("v10.0.19"));
        Assertions.assertEquals("", VersionUtils.convertTagToVersion(""));
    }

    @Test
    void testConvertTagsToVersions() {
        List<String> results = VersionUtils.convertTagsToVersions(List.of("10.0.1", "v10.0.2"));
        Assertions.assertEquals(2, results.size());
        Assertions.assertEquals("10.0.1", results.get(0));
        Assertions.assertEquals("10.0.2", results.get(1));
    }

}
