package com.axonivy.market.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MavenArtifactVersionTest {
  @Test
  void testSetterShouldInitNewMapIfNull() {
    MavenArtifactVersion mockMavenArtifactVersion = new MavenArtifactVersion();
    Assertions.assertNotNull(mockMavenArtifactVersion.getAdditionalArtifactsByVersion());
    Assertions.assertNotNull(mockMavenArtifactVersion.getProductArtifactsByVersion());
  }
}
