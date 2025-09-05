package com.axonivy.market.bo;

import com.axonivy.market.entity.Metadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MetadataTest {
  @Test
  void testEqual() {
    Metadata meta =
        Metadata.builder().url("https://maven.axonivy.com/com/axonivy/utils/octopus/maven-metadata.xml").build();

    Assertions.assertNotNull(meta, "Metadata object should not be equal to null.");
    Assertions.assertNotEquals(new Object(), meta, "Metadata object should not be equal to a different object type.");

    Metadata sameMeta =
        Metadata.builder().url("https://maven.axonivy.com/com/axonivy/utils/octopus/maven-metadata.xml").build();
    Assertions.assertEquals(sameMeta, meta, "Metadata objects with the same URL should be equal.");
  }
}
