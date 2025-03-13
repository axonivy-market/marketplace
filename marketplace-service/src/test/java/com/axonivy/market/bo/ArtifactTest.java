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
    Assertions.assertNotEquals(null, artifact);
    Assertions.assertNotEquals(new Object(), artifact);
    Assertions.assertEquals(artifact, artifact);

    Assertions.assertNotEquals(artifact, new Object());

    Artifact sameArtifact = new Artifact();
    sameArtifact.setGroupId("com.axonivy.com");
    sameArtifact.setArtifactId("octopus-demo");
    Assertions.assertEquals(sameArtifact, artifact);
  }
}
