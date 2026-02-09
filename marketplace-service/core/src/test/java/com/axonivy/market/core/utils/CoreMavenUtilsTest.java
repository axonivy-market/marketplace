package com.axonivy.market.core.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.axonivy.market.core.entity.Metadata;

class CoreMavenUtilsTest {

  @Test
  void testIsProductMetadata_Happy() {
    Metadata metadata = new Metadata();
    metadata.setArtifactId("my-cool-product");
    assertTrue(CoreMavenUtils.isProductMetadata(metadata), "Should return true if artifactId ends with -product");
  }

  @Test
  void testIsProductMetadata_Negative() {
    Metadata metadata = new Metadata();
    metadata.setArtifactId("my-cool-lib");
    assertFalse(CoreMavenUtils.isProductMetadata(metadata), "Should return false if artifactId does not end with -product");
  }

  @Test
  void testIsProductMetadata_NullMetadata() {
    assertFalse(CoreMavenUtils.isProductMetadata(null), "Should return false for null metadata");
  }

  @Test
  void testIsProductMetadata_NullArtifactId() {
    Metadata metadata = new Metadata();
    metadata.setArtifactId(null);
    assertFalse(CoreMavenUtils.isProductMetadata(metadata), "Should return false for null artifactId");
  }
}
