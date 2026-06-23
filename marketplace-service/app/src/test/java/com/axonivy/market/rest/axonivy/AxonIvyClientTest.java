package com.axonivy.market.rest.axonivy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class AxonIvyClientTest {

  @SpyBean
  private AxonIvyClient axonIvyClient;

  @Test
  void testGetDocumentVersions() {
    List<String> versions = axonIvyClient.getDocumentVersions();
    assertFalse(versions.isEmpty(), "Expected to fetch at least one document");
  }

  @Test
  void testGetAllVersions() {
    List<String> versions = axonIvyClient.getAllVersions();
    assertFalse(versions.isEmpty(), "Expected to fetch at least one version");
    assertTrue(versions.stream().noneMatch("dev"::equalsIgnoreCase),
        "Dev version should be resolved to actual version number");
  }
}
