package com.axonivy.market.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataTest {
  @Test
  void testEqualsAndHashCodeSameUrl() {
    Metadata m1 = Metadata.builder().url("url-1").build();
    Metadata m2 = Metadata.builder().url("url-1").build();

    assertThat(m1).as("Same url -> should be equal").isEqualTo(m2);
    assertThat(m1).as("Hashcode should be equal for same url").hasSameHashCodeAs(m2);
  }

  @Test
  void testEqualsAndHashCodeDifferentUrl() {
    Metadata m1 = Metadata.builder().url("url-1").build();
    Metadata m2 = Metadata.builder().url("url-2").build();

    assertThat(m1).as("Different url -> should not be equal").isNotEqualTo(m2);
    assertThat(m1.hashCode()).as("Hashcode should not be equal for different url").isNotEqualTo(m2.hashCode());
  }

  @Test
  void testEqualsNullAndDifferentClass() {
    Metadata m1 = Metadata.builder().url("url-1").build();

    assertThat(m1).as("Should not equal null").isNotNull();
    assertThat(m1.equals("not-a-metadata")).as("Should not equal different type").isFalse();
  }

  @Test
  void testGetIdAndSetId() {
    Metadata metadata = new Metadata();
    metadata.setId("id-123");

    assertThat(metadata.getId()).as("getId should return url").isEqualTo("id-123");
  }

  @Test
  void testEqualsBothUrlsNull() {
    Metadata m1 = new Metadata();
    Metadata m2 = new Metadata();

    assertThat(m1.equals(m2)).as("Both null urls should be equal").isTrue();
    assertThat(m1).as("Hashcode should also be equal when urls are null").hasSameHashCodeAs(m2);
  }

  @Test
  void testEqualsOneUrlNullOtherNot() {
    Metadata m1 = Metadata.builder().url(null).build();
    Metadata m2 = Metadata.builder().url("not-null").build();

    assertThat(m1.equals(m2)).as("One null url and one not should not be equal").isFalse();
  }
}
