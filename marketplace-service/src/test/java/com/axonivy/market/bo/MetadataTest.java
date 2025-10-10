package com.axonivy.market.bo;

import com.axonivy.market.entity.Metadata;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

  @Test
  void testEqualsAndHashCodeWithSelf() {
    Metadata m1 = Metadata.builder().url("url-1").build();

    assertEquals(m1, m1, "Same object -> should be equal");
  }

  @Test
  void testEqualsAndHashCodeSameUrl() {
    Metadata m1 = Metadata.builder().url("url-1").build();
    Metadata m2 = Metadata.builder().url("url-1").build();

    assertEquals(m1, m2, "Same url -> should be equal");
    assertEquals(m1.hashCode(), m2.hashCode(), "Hashcode should be equal for same url");
  }

  @Test
  void testEqualsAndHashCodeDifferentUrl() {
    Metadata m1 = Metadata.builder().url("url-1").build();
    Metadata m2 = Metadata.builder().url("url-2").build();


    assertNotEquals(m1, m2, "Different url -> should not be equal");
    assertNotEquals(m1.hashCode(), m2.hashCode(), "Hashcode should not be equal for different url");
  }

  @Test
  void testEqualsNullAndDifferentClass() {
    Metadata m1 = Metadata.builder().url("url-1").build();
    Metadata m2 = null;

    assertNotEquals(m2, m1, "Different object type should equal");
    assertNotEquals("string", m1, "Should not equal different type");
  }

  @Test
  void testGetIdAndSetId() {
    Metadata metadata = new Metadata();
    metadata.setId("id-123");

    assertEquals("id-123", metadata.getId(), "getId should return url");
  }

  @Test
  void testEqualsBothUrlsNull() {
    Metadata m1 = new Metadata();
    Metadata m2 = new Metadata();

    assertEquals(m1, m2, "Both null urls should be equal");
    assertEquals(m1.hashCode(), m2.hashCode(), "Hashcode should also be equal when urls are null");
  }

  @Test
  void testEqualsOneUrlNullOtherNot() {
    Metadata m1 = Metadata.builder().url(null).build();
    Metadata m2 = Metadata.builder().url("not-null").build();

    assertNotEquals(m1, m2, "One null url and one not should not be equal");
  }
}
