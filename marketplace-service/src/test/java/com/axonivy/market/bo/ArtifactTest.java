package com.axonivy.market.bo;

import com.axonivy.market.entity.Artifact;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ArtifactTest {
  @Test
  void testEqual() {
    Artifact artifact = new Artifact();
    artifact.setGroupId("com.axonivy.com");
    artifact.setArtifactId("octopus-demo");

    Assertions.assertNotNull(artifact, "Artifact object should not be null.");
    Assertions.assertNotEquals(new Object(), artifact, "Artifact should not be equal to an object of a different type.");


    Artifact sameArtifact = new Artifact();
    sameArtifact.setGroupId("com.axonivy.com");
    sameArtifact.setArtifactId("octopus-demo");

    Assertions.assertEquals(sameArtifact, artifact, "Artifacts with the same groupId and artifactId should be equal.");
  }
}
