package com.axonivy.market.controller;

import com.axonivy.market.model.ReleasePreview;
import com.axonivy.market.service.ReleasePreviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleasePreviewControllerTest {

  @Mock
  private ReleasePreviewService mockPreviewService;

  @Mock
  private ReleasePreview mockReleasePreview;

  private ReleasePreviewController controller;

  private MultipartFile testFile;
  private static final String TEST_BASE_URL = "http://localhost:8080";

  @BeforeEach
  void setUp() {
    controller = new ReleasePreviewController(mockPreviewService);
    testFile = new MockMultipartFile(
        "file",
        "test-release.zip",
        "application/zip",
        "test zip content".getBytes()
    );
  }

  @Test
  void testShouldReturnOkResponseWhenPreviewIsSuccessfullyExtracted() {
    try (MockedStatic<ServletUriComponentsBuilder> mockedStatic = mockStatic(ServletUriComponentsBuilder.class)) {
      ServletUriComponentsBuilder mockServletBuilder = mock(ServletUriComponentsBuilder.class);
      UriComponents mockUriComponents = mock(UriComponents.class);

      mockedStatic.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mockServletBuilder);
      when(mockServletBuilder.build()).thenReturn(mockUriComponents);
      when(mockUriComponents.toUriString()).thenReturn(TEST_BASE_URL);

      when(mockPreviewService.extract(testFile, TEST_BASE_URL)).thenReturn(mockReleasePreview);

      ResponseEntity<Object> response = controller.extractZipFile(testFile);

      assertNotNull(response, "Response should not be null");
      assertEquals(HttpStatus.OK, response.getStatusCode(),
          "Response status should be OK when preview is successfully extracted");
      assertSame(mockReleasePreview, response.getBody(),
          "Response body should contain the extracted ReleasePreview object");
      verify(mockPreviewService, times(1)).extract(testFile, TEST_BASE_URL);
    }
  }

  @Test
  void testShouldReturnNoContentResponseWhenPreviewIsSuccessfullyExtractedAndIsNull() {
    try (MockedStatic<ServletUriComponentsBuilder> mockedStatic = mockStatic(ServletUriComponentsBuilder.class)) {
      ServletUriComponentsBuilder mockServletBuilder = mock(ServletUriComponentsBuilder.class);
      UriComponents mockUriComponents = mock(UriComponents.class);

      mockedStatic.when(ServletUriComponentsBuilder::fromCurrentContextPath).thenReturn(mockServletBuilder);
      when(mockServletBuilder.build()).thenReturn(mockUriComponents);
      when(mockUriComponents.toUriString()).thenReturn(TEST_BASE_URL);

      when(mockPreviewService.extract(testFile, TEST_BASE_URL)).thenReturn(null);

      ResponseEntity<Object> response = controller.extractZipFile(testFile);

      assertNotNull(response, "Response should not be null");
      assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode(),
          "Response status should be NO_CONTENT when preview is successfully extracted and is null");
      assertNull(response.getBody(),
          "Response body should be null when preview is not extracted");
      verify(mockPreviewService, times(1)).extract(testFile, TEST_BASE_URL);
    }
  }
}
