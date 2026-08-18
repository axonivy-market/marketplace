package com.axonivy.market.controller;

import com.axonivy.market.entity.ExternalDocumentMeta;
import com.axonivy.market.service.ExternalDocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(ExternalDocumentController.class)
class ExternalDocumentControllerTest extends WebMvcControllerTestSupport {

  private static final String VERSION = "13.1.1";
  private static final String PORTAL = "portal";

  @MockitoBean
  private ExternalDocumentService service;

  @Test
  void testFindProductDoc() throws Exception {
    when(service.findExternalDocument(any(), any())).thenReturn(buildExternalDocumentMock());

    mockMvc.perform(get("/api/externaldocument/{id}/{version}", PORTAL, VERSION))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.relativeLink").value("/market-cache/portal/10.0.0/doc/index.html"));
  }

  @Test
  void testRedirectToBestVersionWithInvalidPath() throws Exception {
    mockMvc.perform(get("/api/externaldocument/best-match"))
        .andExpect(status().isFound())
        .andExpect(header().string("Location", containsString("/error-page/404")));
  }

  @Test
  void testSyncDocumentForProductWithVersion() throws Exception {
    when(service.determineProductIdsForSync(anyString())).thenReturn(List.of(PORTAL));

    mockMvc.perform(put("/api/externaldocument/sync")
            .with(requestedByHeader())
            .param("product-id", PORTAL)
            .param("version", "invalid-version"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void testSyncDocumentForProduct() throws Exception {
    mockMvc.perform(put("/api/externaldocument/sync")
            .with(requestedByHeader()))
        .andExpect(status().isNoContent());

    when(service.determineProductIdsForSync(anyString())).thenReturn(List.of(PORTAL));
    mockMvc.perform(put("/api/externaldocument/sync")
            .with(requestedByHeader())
            .param("product-id", PORTAL))
        .andExpect(status().isOk());
  }

  private ExternalDocumentMeta buildExternalDocumentMock() {
    return ExternalDocumentMeta.builder()
        .relativeLink("/market-cache/portal/10.0.0/doc/index.html")
        .build();
  }
}
