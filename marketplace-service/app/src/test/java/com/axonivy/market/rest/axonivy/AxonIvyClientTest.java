package com.axonivy.market.rest.axonivy;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.config.RestClientBuilder;
import com.axonivy.market.model.DocumentInfoResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AxonIvyClientTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private RestClient restClient;

  @Mock
  private RestClientBuilder restClientBuilder;

  @Mock
  private com.axonivy.market.service.AppSettingService settingService;

  @InjectMocks
  private AxonIvyClient axonIvyClient;

  @org.junit.jupiter.api.BeforeEach
  void setUp() {
    when(restClientBuilder.build()).thenReturn(restClient);
  }

  @Test
  void testGetDocumentVersions() {
    when(settingService.getStringValueByKey(AppSettingKey.AXON_IVY_DEVELOPER_URL)).thenReturn("https://developer.example.com");
    when(restClient.get().uri(anyString()).retrieve().body(eq(DocumentInfoResponse.class))).thenReturn(
        DocumentInfoResponse.builder()
            .versions(List.of(
                DocumentInfoResponse.DocumentVersion.builder().version("12.0").url("/docs/12.0/").build(),
                DocumentInfoResponse.DocumentVersion.builder().version("dev").url("/doc/13.0/").build()))
            .build());
    List<String> versions = axonIvyClient.getDocumentVersions();
    assertFalse(versions.isEmpty(), "Expected to fetch at least one document");
  }

  @Test
  void testGetAllVersions() {
    when(settingService.getStringValueByKey(AppSettingKey.AXON_IVY_DEVELOPER_URL)).thenReturn("https://developer.example.com");
    when(restClient.get().uri(anyString()).retrieve().body(eq(DocumentInfoResponse.class))).thenReturn(
        DocumentInfoResponse.builder()
            .versions(List.of(
                DocumentInfoResponse.DocumentVersion.builder().version("12.0").url("/docs/12.0").build(),
                DocumentInfoResponse.DocumentVersion.builder().version("dev").url("/doc/13.0/").build()))
            .build());
    List<String> versions = axonIvyClient.getAllVersions();
    assertFalse(versions.isEmpty(), "Expected to fetch at least one version");
    assertTrue(versions.stream().noneMatch("dev"::equalsIgnoreCase),
        "Dev version should be resolved to actual version number");
  }
}
