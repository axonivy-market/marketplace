package com.axonivy.market.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;

@ExtendWith(MockitoExtension.class)
class SafeHttpDownloaderUtilsTest {

  private SafeHttpDownloaderUtils utils = new SafeHttpDownloaderUtils();

  @BeforeEach
  void setUp() {
    utils = new SafeHttpDownloaderUtils();
    String[] allowedUrls = {"https://example.com", "https://docs.axonivy.com"};
    ReflectionTestUtils.setField(utils, "allowedUrlsArray", allowedUrls);
    utils.init();
  }

  @Test
  void testValidateUri_validAllowedUrl() {
    URI uri = URI.create("https://example.com/resources/file.zip");
    assertDoesNotThrow(() -> utils.validateUri(uri));
  }

  @Test
  void testValidateUri_invalidHost_shouldThrow() {
    URI uri = URI.create("http://nonexistent.unknown-domain.xyz");
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> utils.validateUri(uri));
    assertTrue(exception.getMessage().contains("Unknown host"));
  }

  @Test
  void testValidateUri_nullHost_shouldThrow() {
    URI uri = URI.create("");
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> utils.validateUri(uri));
    assertTrue(exception.getMessage().contains("missing host"));
  }
}