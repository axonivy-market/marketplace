package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class HttpFetchingUtilsTest extends BaseSetup {

  @Test
  void testGetFileAsBytesWithInvalidUrlReturnsNull() {
    byte[] result = HttpFetchingUtils.getFileAsBytes(MOCK_DUMP_DOWNLOAD_URL);
    assertEquals(0, result.length, "The byte array should be empty with invalid url");
  }

  @Test
  void testGetFileAsStringWithInvalidUrlReturnsNull() {
    String result = HttpFetchingUtils.getFileAsString(MOCK_DUMP_DOWNLOAD_URL);
    assertEquals(StringUtils.EMPTY, result, "Expected empty string when fetching from an unreachable URL");
  }

  @Test
  void testFetchResourceUrlReturns4xxWithInvalidUrl() {
    var result = HttpFetchingUtils.fetchResourceUrl(MOCK_DUMP_DOWNLOAD_URL);
    assertNull(result, "Expected null when fetching from an unreachable URL");
  }

  @Test
  void testExtractFileNameFromValidUrl() {
    String url = "https://example.com/files/document.pdf";
    String result = HttpFetchingUtils.extractFileNameFromUrl(url);
    assertEquals("document.pdf", result, "File name should be extracted from url.");
    url = "https://example.com/files/";
    result = HttpFetchingUtils.extractFileNameFromUrl(url);
    assertEquals("unknown_file", result, "File name should be default if can no be extracted from url.");
    url = "ht!tp:/malformed-url";
    result = HttpFetchingUtils.extractFileNameFromUrl(url);
    assertEquals("unknown_file", result, "File name should be default if can no be extracted from url.");
    url = "https://example.com/files/document.pdf?version=2";
    result = HttpFetchingUtils.extractFileNameFromUrl(url);
    assertEquals("document.pdf", result, "File name should be default if can no be extracted from url.");
    url = "https://example.com/files/file%20with%20spaces.txt";
    result = HttpFetchingUtils.extractFileNameFromUrl(url);
    assertEquals("file with spaces.txt", result, "File name should be default if can no be extracted from url.");
  }
}
