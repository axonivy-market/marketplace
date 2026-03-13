package com.axonivy.market.core.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class CoreVersionFactoryTest {

  @Test
  void testFindVersionStartWithOrNull_Happy() {
    List<String> releaseVersions = List.of("10.0.0", "11.0.0");
    String result = CoreVersionFactory.findVersionStartWithOrNull(releaseVersions, "10");
    assertEquals("10.0.0", result);
  }

  @Test
  void testFindVersionStartWithOrNull_EmptyListReturnsInput() {
    String result = CoreVersionFactory.findVersionStartWithOrNull(List.of(), "10.0");
    assertEquals("10.0", result);
  }

  @Test
  void testFindVersionStartWithOrNull_NotFoundReturnsNull() {
    List<String> releaseVersions = List.of("10.0.0", "11.0.0");
    String result = CoreVersionFactory.findVersionStartWithOrNull(releaseVersions, "12");
    assertNull(result);
  }

  @Test
  void testFindLowerVersion_Happy() {
    List<String> releaseVersions = List.of("10.0.0", "9.0.0");
    String result = CoreVersionFactory.findLowerVersion(releaseVersions, "11.0.0");
    // MavenVersionComparator logic: "10.0.0" < "11.0.0"
    assertEquals("10.0.0", result);
  }

  @Test
  void testFindLowerVersion_NullList() {
    String result = CoreVersionFactory.findLowerVersion(null, "11.0.0");
    assertNull(result);
  }
}
