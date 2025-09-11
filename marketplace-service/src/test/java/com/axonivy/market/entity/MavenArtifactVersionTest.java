package com.axonivy.market.entity;

import com.axonivy.market.entity.key.MavenArtifactKey;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class MavenArtifactVersionTest {
  @Test
  void testGetIdReturnsMavenArtifactKeyId() {
    var mavenArtifactVersion = new MavenArtifactVersion();
    var mavenArtifactKey = new MavenArtifactKey();
    mavenArtifactKey.setArtifactId("portal");
    mavenArtifactKey.setProductVersion("13.1.0");
    mavenArtifactKey.setAdditionalVersion(false);
    mavenArtifactVersion.setId(mavenArtifactKey);

    assertEquals("portal", mavenArtifactVersion.getId().getArtifactId(),
        "Expected getArtifactId() to return portal");
    assertEquals("13.1.0", mavenArtifactVersion.getId().getProductVersion(),
        "Expected getProductVersion() to return 13.1.0");
    assertFalse(mavenArtifactVersion.getId().isAdditionalVersion(), "Expected isAdditionalVersion() to be false");
  }
}
