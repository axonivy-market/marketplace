package com.axonivy.market.core.utils;

import com.axonivy.market.core.constants.CoreProductJsonConstants;
import com.axonivy.market.core.entity.Metadata;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreMavenUtilsTest {

  @Test
  void testIsProductMetadataHappy() {
    Metadata metadata = new Metadata();
    metadata.setArtifactId("my-cool-product");
    assertTrue(CoreMavenUtils.isProductMetadata(metadata),
        "Should return true if artifactId ends with -product");
  }

  @Test
  void testIsProductMetadataNegative() {
    Metadata metadata = new Metadata();
    metadata.setArtifactId("my-cool-lib");
    assertFalse(CoreMavenUtils.isProductMetadata(metadata),
        "Should return false if artifactId does not end with -product");
  }

  @Test
  void testIsProductMetadataNullMetadata() {
    assertFalse(CoreMavenUtils.isProductMetadata(null), "Should return false for null metadata");
  }

  @Test
  void testIsProductMetadataNullArtifactId() {
    Metadata metadata = new Metadata();
    metadata.setArtifactId(null);
    assertFalse(CoreMavenUtils.isProductMetadata(metadata), "Should return false for null artifactId");
  }

  @Test
  void testIsJsonContentContainOnlyMavenDropinsHappy() {
    String jsonContent = CoreProductJsonConstants.MAVEN_DROPINS_INSTALLER_ID;

    assertTrue(CoreMavenUtils.isJsonContentContainOnlyMavenDropins(jsonContent),
        "Should return true when json content contains only MAVEN_DROPINS_INSTALLER_ID");
  }

  @Test
  void testIsJsonContentContainOnlyMavenDropinsWithImportInstaller() {
    String jsonContent = CoreProductJsonConstants.MAVEN_DROPINS_INSTALLER_ID
        + CoreProductJsonConstants.MAVEN_IMPORT_INSTALLER_ID;

    assertFalse(CoreMavenUtils.isJsonContentContainOnlyMavenDropins(jsonContent),
        "Should return false when json content contains MAVEN_IMPORT_INSTALLER_ID together with " +
            "MAVEN_DROPINS_INSTALLER_ID");
  }

  @Test
  void testIsJsonContentContainOnlyMavenDropinsWithDependencyInstaller() {
    String jsonContent = CoreProductJsonConstants.MAVEN_DROPINS_INSTALLER_ID
        + CoreProductJsonConstants.MAVEN_DEPENDENCY_INSTALLER_ID;

    assertFalse(CoreMavenUtils.isJsonContentContainOnlyMavenDropins(jsonContent),
        "Should return false when json content contains MAVEN_DEPENDENCY_INSTALLER_ID together with " +
            "MAVEN_DROPINS_INSTALLER_ID");
  }

  @Test
  void testIsJsonContentContainOnlyMavenDropinsWithAllInstallers() {
    String jsonContent = CoreProductJsonConstants.MAVEN_DROPINS_INSTALLER_ID
        + CoreProductJsonConstants.MAVEN_IMPORT_INSTALLER_ID
        + CoreProductJsonConstants.MAVEN_DEPENDENCY_INSTALLER_ID;

    assertFalse(CoreMavenUtils.isJsonContentContainOnlyMavenDropins(jsonContent),
        "Should return false when json content contains MAVEN_DROPINS_INSTALLER_ID together with other installer ids");
  }

  @Test
  void testIsJsonContentContainOnlyMavenDropinsWithoutDropins() {
    String jsonContent = CoreProductJsonConstants.MAVEN_IMPORT_INSTALLER_ID;

    assertFalse(CoreMavenUtils.isJsonContentContainOnlyMavenDropins(jsonContent),
        "Should return false when json content does not contain MAVEN_DROPINS_INSTALLER_ID");
  }
}
