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
import java.util.regex.Matcher;

import static com.axonivy.market.rest.axonivy.AxonIvyClientConstant.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AxonIvyClient {

  private final RestTemplate restTemplate;
  private final AppSettingService settingService;

  public List<String> getDocumentVersions() {
    var host = settingService.getStringValueByKey(AppSettingKey.AXON_IVY_DEVELOPER_URL);
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

  public List<String> getAllVersions() {
    var host = settingService.getStringValueByKey(AppSettingKey.AXON_IVY_DEVELOPER_URL);
    var url = String.format(HOST_PATH_FORMAT, host ,DOCUMENT_VERSION_PATH);
    try {
      DocumentInfoResponse response = restTemplate.getForObject(url, DocumentInfoResponse.class);
      if (response != null) {
        return response.getVersions().stream()
            .map(this::extractVersion)
            .toList();
      }
    } catch (RestClientException e) {
      log.error("Error fetching document from Axon Ivy Developer", e);
    }
    return Collections.emptyList();
  }

  private String extractVersion(DocumentInfoResponse.DocumentVersion documentVersion) {
    if (DEV_VERSION.equalsIgnoreCase(documentVersion.getVersion())) {
      Matcher matcher = VERSION_FROM_URL_PATTERN.matcher(documentVersion.getUrl());
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    return documentVersion.getVersion();
  }
}
