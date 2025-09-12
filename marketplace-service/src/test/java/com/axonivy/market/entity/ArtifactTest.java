package com.axonivy.market.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArtifactTest {
  @Test
  void testEqualsWitSameInstance() {
    Artifact artifact = new Artifact();
    artifact.setGroupId("com.axonivy");
    artifact.setArtifactId("test-artifact");

    assertEquals(artifact, artifact, "An object should be equal to itself");
  }

  @Test
  void testEqualsWithNullObject() {
    Artifact artifact1 = new Artifact();
    artifact1.setGroupId("com.axonivy");
    artifact1.setArtifactId("test-artifact");

    Artifact artifact2 = null;

    assertNotEquals(artifact1, artifact2, "Artifact should not be equal to null");
  }

  @Test
  void testEqualsWithDifferentClass() {
    Artifact artifact1 = new Artifact();
    var artifact2 = "string";
    artifact1.setGroupId("com.axonivy");
    artifact1.setArtifactId("test-artifact");

    assertNotEquals(artifact1, artifact2,
        "Artifact should not be equal to an object of another class");
  }

  @Test
  void testEqualsWithsameGroupIdAndArtifactId() {
    Artifact artifact1 = new Artifact();
    artifact1.setGroupId("com.axonivy");
    artifact1.setArtifactId("test-artifact");

    Artifact artifact2 = new Artifact();
    artifact2.setGroupId("com.axonivy");
    artifact2.setArtifactId("test-artifact");

    assertEquals(artifact1, artifact2, "Artifacts with the same groupId and artifactId should be equal");
    assertEquals(artifact1.hashCode(), artifact2.hashCode(),
        "Artifacts that are equal should have the same hashCode");
  }

  @Test
  void testEqualsWithDifferentGroupId() {
    Artifact artifact1 = new Artifact();
    artifact1.setGroupId("com.axonivy");
    artifact1.setArtifactId("test-artifact");

    Artifact artifact2 = new Artifact();
    artifact2.setGroupId("com.other");
    artifact2.setArtifactId("test-artifact");

    assertNotEquals(artifact1, artifact2, "Artifacts with different groupIds should not be equal");
  }

  @Test
  void testEqualsWithdifferentArtifactId() {
    Artifact artifact1 = new Artifact();
    artifact1.setGroupId("com.axonivy");
    artifact1.setArtifactId("artifact-1");

    Artifact artifact2 = new Artifact();
    artifact2.setGroupId("com.axonivy");
    artifact2.setArtifactId("artifact-2");

    assertNotEquals(artifact1, artifact2, "Artifacts with different artifactIds should not be equal");
  }
}
