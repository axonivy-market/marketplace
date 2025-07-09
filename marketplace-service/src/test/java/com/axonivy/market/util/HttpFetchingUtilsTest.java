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
}
