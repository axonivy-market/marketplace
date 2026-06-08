package com.axonivy.market.rest.axonivy;

import com.axonivy.market.enums.AppSettingKey;
import com.axonivy.market.model.DocumentInfoResponse;
import com.axonivy.market.service.AppSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AxonIvyClient {

  private static final String DOCUMENT_VERSION_PATH = "/api/docs/Axon-Ivy-Platform/dev/en";
  private static final String HOST_PATH_FORMAT = "%s%s";

  private final RestTemplate restTemplate;
  private final AppSettingService settingService;

  public List<String> getDocumentVersions() {
    String host = settingService.getStringValueByKey(AppSettingKey.AXON_IVY_DEVELOPER_URL);
    var url = String.format(HOST_PATH_FORMAT, host, DOCUMENT_VERSION_PATH);
    try {
      DocumentInfoResponse response = restTemplate.getForObject(url, DocumentInfoResponse.class);
      if (response != null) {
        return response.getVersions().stream()
            .map(DocumentInfoResponse.DocumentVersion::getVersion)
            .toList();
      }
    } catch (RestClientException e) {
      log.error("Error fetching document from Axon Ivy Developer", e);
    }
    return Collections.emptyList();
  }
}
