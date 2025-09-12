package com.axonivy.market.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MetadataTest {
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
    Metadata m = Metadata.builder().url("url-1").build();

    assertNotEquals(m, null, "Different object type should equal");
    assertNotEquals(m, "string", "Should not equal different type");
  }

  @Test
  void testGetIdAndSetId() {
    Metadata metadata = new Metadata();
    metadata.setId("id-123");

    assertEquals(metadata.getId(), "id-123", "getId should return url");
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
