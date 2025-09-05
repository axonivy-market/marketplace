package com.axonivy.market.factory;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.enums.DevelopmentVersion;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class VersionFactoryTest  extends BaseSetup {

  final List<String> mockVersions = List.of("10.0.0", "11.4.0-m1", "10.0.1-SNAPSHOT");

  @Test
  void testResolveVersion() {
    var resolvedVersion = VersionFactory.get(mockVersions, "10");
    assertEquals("10.0.1-SNAPSHOT", resolvedVersion, "Should return highest release of that major release");

    resolvedVersion = VersionFactory.get(mockVersions, "10.0");
    assertEquals("10.0.1-SNAPSHOT", resolvedVersion, "Should return highest release of that minor release");

    resolvedVersion = VersionFactory.get(mockVersions, "10.0.0");
    assertEquals("10.0.0", resolvedVersion, "Should return exactly release");

    resolvedVersion = VersionFactory.get(mockVersions, "10.0-dev");
    assertEquals("10.0.1-SNAPSHOT", resolvedVersion, "Should retun highest release of that minor dev release");

    resolvedVersion = VersionFactory.get(mockVersions, "11.4");
    assertEquals("11.4.0-m1", resolvedVersion, "Should return highest sprint release of that minor release");

    resolvedVersion = VersionFactory.get(mockVersions, "dev");
    assertEquals("11.4.0-m1", resolvedVersion, "Should return highest dev release of that minor release");
  }
  @Test
  void testGetFromMetadata() {
    assertEquals(StringUtils.EMPTY, VersionFactory.getFromMetadata(List.of(), DevelopmentVersion.LATEST.getCode()),
        "Should return empty string when metadata list is empty for LATEST version.");
    assertEquals(StringUtils.EMPTY, VersionFactory.getFromMetadata(List.of(), DevelopmentVersion.DEV.getCode()),
        "Should return empty string when metadata list is empty for DEV version.");

    assertEquals(MOCK_RELEASED_VERSION, VersionFactory.getFromMetadata(List.of(getMockMetadataWithVersions()),
            DevelopmentVersion.LATEST.getCode()),
        "Should return the latest released version from metadata for LATEST version.");
    assertEquals(MOCK_SPRINT_RELEASED_VERSION, VersionFactory.getFromMetadata(List.of(getMockMetadataWithVersions()),
            DevelopmentVersion.DEV.getCode()),
        "Should return the sprint released version from metadata for DEV version.");
    assertEquals(MOCK_RELEASED_VERSION, VersionFactory.getFromMetadata(List.of(getMockMetadataWithVersions()),
            "10-dev"),
        "Should return the correct released version from metadata for version '10-dev'.");
    assertEquals(MOCK_RELEASED_VERSION, VersionFactory.getFromMetadata(List.of(getMockMetadataWithVersions()),
            "10"),
        "Should return the correct released version from metadata for version '10'.");
    assertEquals(MOCK_RELEASED_VERSION, VersionFactory.getFromMetadata(List.of(getMockMetadataWithVersions()),
            "10.0"),
        "Should return the correct released version from metadata for version '10.0'.");
  }

  @Test
  void testResolvedVersion() {
    var resolvedVersion = VersionFactory.resolveVersion("${project.version}", MOCK_RELEASED_VERSION);
    assertEquals(MOCK_RELEASED_VERSION, resolvedVersion, "Should return default release version");

    resolvedVersion = VersionFactory.resolveVersion("[10.0.10)", null);
    assertEquals(MOCK_RELEASED_VERSION, resolvedVersion, "Should return version in range");

    resolvedVersion = VersionFactory.resolveVersion("[10.0.10, 10.0.10-m123)", null);
    assertEquals("10.0.10-m123", resolvedVersion, "Should return highest version in range");

    resolvedVersion = VersionFactory.resolveVersion(MOCK_PRODUCT_ID_WITH_VERSION, MOCK_RELEASED_VERSION);
    assertEquals(MOCK_RELEASED_VERSION, resolvedVersion, "Should return default release version");
  }
}
