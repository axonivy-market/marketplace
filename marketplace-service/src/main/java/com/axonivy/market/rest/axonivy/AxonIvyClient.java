package com.axonivy.market.rest.axonivy;

import com.axonivy.market.model.DocumentLanguageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AxonIvyClient {

    private static final String DOCUMENT_VERSION_PATH = "/api/docs/Axon-Ivy-Platform/dev/en";

    @Value("${axon.ivy.developer.url}")
    private String host;

    private final RestTemplate restTemplate;

    public List<String> getDocumentVersions() {
        var url = String.format("%s" + DOCUMENT_VERSION_PATH, host);
        try {
            DocumentLanguageResponse response = restTemplate.getForObject(url, DocumentLanguageResponse.class);
            if (response != null) {
                return response.getVersions().stream()
                        .map(DocumentLanguageResponse.DocumentVersion::getVersion)
                        .toList();
            }
        } catch (Exception e) {
            log.error("Error fetching document from Axon Ivy Developer", e);
        }
        return Collections.emptyList();
    }
}
