package com.axonivy.market.rest.axonivy;

import com.axonivy.market.model.DocumentInfoResponse;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;
import java.util.List;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@TestPropertySource("classpath:application-test.properties")
class AxonIvyClientTest {

  @SpyBean
  private AxonIvyClient axonIvyClient;

  @Test
  void testGetDocumentVersions() {
    List<String> versions = axonIvyClient.getDocumentVersions();
    assertFalse(versions.isEmpty(), "Expected to fetch at least one document");
  }
}
