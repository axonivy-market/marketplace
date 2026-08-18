package com.axonivy.market.controller;

import com.axonivy.market.model.ReleasePreview;
import com.axonivy.market.service.ReleasePreviewService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ControllerWebMvcTest(ReleasePreviewController.class)
class ReleasePreviewControllerTest extends WebMvcControllerTestSupport {

  @MockitoBean
  private ReleasePreviewService previewService;

  @Test
  void testShouldReturnOkResponseWhenPreviewIsSuccessfullyExtracted() throws Exception {
    MockMultipartFile testFile = new MockMultipartFile(
        "file",
        "test-release.zip",
        "application/zip",
        "test zip content".getBytes()
    );
    ReleasePreview releasePreview = ReleasePreview.builder()
        .description(java.util.Map.of("en", "test"))
        .build();
    when(previewService.extract(any())).thenReturn(releasePreview);

    mockMvc.perform(multipart("/api/release-preview")
            .file(testFile)
            .with(requestedByHeader()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").exists());
  }

  @Test
  void testShouldReturnNoContentResponseWhenPreviewIsSuccessfullyExtractedAndIsNull() throws Exception {
    MockMultipartFile testFile = new MockMultipartFile(
        "file",
        "test-release.zip",
        "application/zip",
        "test zip content".getBytes()
    );
    when(previewService.extract(any())).thenReturn(null);

    mockMvc.perform(multipart("/api/release-preview")
            .file(testFile)
            .with(requestedByHeader()))
        .andExpect(status().isNoContent());
  }
}
