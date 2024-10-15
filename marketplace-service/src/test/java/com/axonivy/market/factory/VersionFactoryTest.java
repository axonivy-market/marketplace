package com.axonivy.market.factory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class VersionFactoryTest {

  final List<String> mockVersions = List.of("10.0.0", "11.4.0-m1", "10.0.1-SNAPSHOT");

  @Test
  void testResolveVersion() {
    var resolvedVersion = VersionFactory.get(mockVersions, "10");
    assertEquals("10.0.1-SNAPSHOT", resolvedVersion, "Should return highest release of that major release");

    resolvedVersion = VersionFactory.get(mockVersions, "10.0");
    assertEquals("10.0.1-SNAPSHOT", resolvedVersion, "Should return highest release of that minor release");

    resolvedVersion = VersionFactory.get(mockVersions, "10.0.0");
    assertEquals("10.0.0", resolvedVersion, "Should return exactly release");

    resolvedVersion = VersionFactory.get(mockVersions, "10.0-dev");
    assertEquals("10.0.1-SNAPSHOT", resolvedVersion, "Should retun highest release of that minor dev release");

    resolvedVersion = VersionFactory.get(mockVersions, "11.4");
    assertEquals("11.4.0-m1", resolvedVersion, "Should return highest sprint release of that minor release");

    resolvedVersion = VersionFactory.get(mockVersions, "dev");
    assertEquals("11.4.0-m1", resolvedVersion, "Should return highest dev release of that minor release");
  }
}
