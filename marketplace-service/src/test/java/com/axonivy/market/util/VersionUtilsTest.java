package com.axonivy.market.util;

import com.axonivy.market.service.impl.VersionServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
