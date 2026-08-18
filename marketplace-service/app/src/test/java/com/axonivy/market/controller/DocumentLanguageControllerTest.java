package com.axonivy.market.controller;

import com.axonivy.market.enums.DocumentLanguage;
import com.axonivy.market.model.DocumentInfoResponse;
import com.axonivy.market.service.ExternalDocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(DocumentLanguageController.class)
class DocumentLanguageControllerTest extends WebMvcControllerTestSupport {

  private static final String PORTAL = "portal";
  private static final String TEST_VERSION = "12";

  @MockitoBean
  private ExternalDocumentService service;

  @Test
  void testGetDocumentByVersionAndLanguageSuccess() throws Exception {
    var response = DocumentInfoResponse.builder()
        .versions(List.of(new DocumentInfoResponse.DocumentVersion(TEST_VERSION, "url1")))
        .languages(List.of(new DocumentInfoResponse.DocumentLanguage(DocumentLanguage.ENGLISH.getCode(), "url2")))
        .build();

    when(service.findDocVersionsAndLanguages(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(response);

    mockMvc.perform(get("/api/docs/{artifact}/{version}/{language}", PORTAL, TEST_VERSION,
            DocumentLanguage.ENGLISH.getCode()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.versions[0].version").value(TEST_VERSION))
        .andExpect(jsonPath("$.languages[0].language").value(DocumentLanguage.ENGLISH.getCode()));
  }

  @Test
  void testGetDocumentByVersionAndLanguageNotFound() throws Exception {
    when(service.findDocVersionsAndLanguages(anyString(), anyString(), anyString(), anyString()))
        .thenReturn(null);

    mockMvc.perform(get("/api/docs/{artifact}/{version}/{language}", PORTAL, TEST_VERSION,
            DocumentLanguage.ENGLISH.getCode()))
        .andExpect(status().isNotFound());
  }
}
