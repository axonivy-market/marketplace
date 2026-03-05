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

  @Autowired
  private AxonIvyClient axonIvyClient;

  @MockBean
  private RestTemplate restTemplate;

  @Value("${axon.ivy.developer.url}")
  private String host;

  private static final String DOCUMENT_VERSION_PATH = "/api/docs/Axon-Ivy-Platform/dev/en";

  private String buildUrl() {
    return String.format("%s%s", host, DOCUMENT_VERSION_PATH);
  }

  @Test
  void shouldReturnEmptyListWhenRestClientThrowsException() {
    String url = buildUrl();

    when(restTemplate.getForObject(url, DocumentInfoResponse.class))
        .thenThrow(new RestClientException("Connection error"));

    List<String> versions = axonIvyClient.getDocumentVersions();

    assertTrue(versions.isEmpty(), "Expected empty list when exception occurs");
  }

  @Test
  void shouldReturnEmptyListWhenResponseIsNull() {
    String url = buildUrl();

    when(restTemplate.getForObject(url, DocumentInfoResponse.class))
        .thenReturn(null);

    List<String> versions = axonIvyClient.getDocumentVersions();

    assertTrue(versions.isEmpty(), "Expected empty list when response is null");
  }

  @Test
  void shouldReturnVersionsWhenResponseIsValid() {
    String url = buildUrl();

    DocumentInfoResponse.DocumentVersion v1 =
        DocumentInfoResponse.DocumentVersion.builder()
            .version("1.0")
            .url("/1.0")
            .build();

    DocumentInfoResponse.DocumentVersion v2 =
        DocumentInfoResponse.DocumentVersion.builder()
            .version("2.0")
            .url("/2.0")
            .build();

    DocumentInfoResponse response =
        DocumentInfoResponse.builder()
            .versions(List.of(v1, v2))
            .languages(Collections.emptyList())
            .build();

    when(restTemplate.getForObject(url, DocumentInfoResponse.class))
        .thenReturn(response);

    List<String> versions = axonIvyClient.getDocumentVersions();

    assertEquals(List.of("1.0", "2.0"), versions);
  }
}
