package com.axonivy.market.controller;

import com.axonivy.market.model.DocumentLanguageResponse;
import com.axonivy.market.service.ExternalDocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentLanguageControllerTest {

    @Mock
    private ExternalDocumentService service;

    @InjectMocks
    private DocumentLanguageController languageController;

    @BeforeEach
    void setup() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/docs/portal/12/en");
        request.setServerName("localhost");
        request.setServerPort(8080);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void testGetDocumentByVersionAndLanguageSuccess() {
        // given
        var response = DocumentLanguageResponse.builder()
                .versions(List.of(new DocumentLanguageResponse.DocumentVersion("12.0", "url1")))
                .languages(List.of(new DocumentLanguageResponse.DocumentLanguage("en", "url2")))
                .build();

        when(service.findDocVersionsAndLanguages(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(response);
        ResponseEntity<DocumentLanguageResponse> result =
                languageController.getDocumentByVersionAndLanguage("portal", "12", "en");
        assertTrue(result.getStatusCode().is2xxSuccessful(), "Status code should be 2xx");
        assertEquals(result.getBody(), response, "Response body should match the mock response");
    }

    @Test
    void testGetDocumentByVersionAndLanguageNotFound() {
        when(service.findDocVersionsAndLanguages(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(null);
        ResponseEntity<DocumentLanguageResponse> result =
                languageController.getDocumentByVersionAndLanguage("portal", "12", "en");
        assertTrue(result.getStatusCode().is4xxClientError(), "Status code should be 4xx");
        assertNull(result.getBody(),  "Response body should be null");
    }
}
