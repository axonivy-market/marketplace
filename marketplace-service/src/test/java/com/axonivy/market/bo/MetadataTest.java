package com.axonivy.market.bo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MetadataTest {
  @Test
  void testEqual() {
    Metadata meta =
        Metadata.builder().url("https://maven.axonivy.com/com/axonivy/utils/octopus/maven-metadata.xml").build();

    Assertions.assertNotEquals(null, meta);
    Assertions.assertNotEquals(new Object(), meta);
    Assertions.assertEquals(meta, meta);

    Metadata sameMeta =
        Metadata.builder().url("https://maven.axonivy.com/com/axonivy/utils/octopus/maven-metadata.xml").build();
    Assertions.assertEquals(sameMeta, meta);
  }
}
