package com.axonivy.market.util;

import com.axonivy.market.enums.NonStandardProduct;
import com.axonivy.market.github.model.MavenArtifact;
import com.axonivy.market.service.impl.VersionServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
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
}
