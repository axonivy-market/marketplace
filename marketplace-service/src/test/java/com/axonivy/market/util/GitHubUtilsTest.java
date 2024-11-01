package com.axonivy.market.util;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.github.util.GitHubUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.axonivy.market.constants.MetaConstants.META_FILE;
import static com.axonivy.market.constants.ProductJsonConstants.LOGO_FILE;

@ExtendWith(MockitoExtension.class)
class GitHubUtilsTest extends BaseSetup {

  @Test
  void testConvertArtifactIdToName() {
    String result = GitHubUtils.convertArtifactIdToName(MOCK_ARTIFACT_ID);
    Assertions.assertEquals("Bpmn Statistic", result);

    result = GitHubUtils.convertArtifactIdToName(null);
    Assertions.assertEquals(StringUtils.EMPTY, result);

    result = GitHubUtils.convertArtifactIdToName(StringUtils.EMPTY);
    Assertions.assertEquals(StringUtils.EMPTY, result);

    result = GitHubUtils.convertArtifactIdToName(" ");
    Assertions.assertEquals(StringUtils.EMPTY, result);
  }

  @Test
  void testSortMetaJsonFirst() {
    int result = GitHubUtils.sortMetaJsonFirst(META_FILE, LOGO_FILE);
    Assertions.assertEquals(-1, result);

    result = GitHubUtils.sortMetaJsonFirst(LOGO_FILE, META_FILE);
    Assertions.assertEquals(1, result);

    result = GitHubUtils.sortMetaJsonFirst(LOGO_FILE, LOGO_FILE);
    Assertions.assertEquals(0, result);
  }

  @Test
  void testExtractJson() {
    // Test case: valid JSON inside a string
    String exceptionMessage = "Error occurred: {\"message\":\"An error occurred\"}";
    String json = GitHubUtils.extractJson(exceptionMessage);
    Assertions.assertEquals("{\"message\":\"An error occurred\"}", json);

    // Test case: no JSON in string
    exceptionMessage = "Error occurred: no json here";
    json = GitHubUtils.extractJson(exceptionMessage);
    Assertions.assertEquals(StringUtils.EMPTY, json);

    // Test case: empty string
    exceptionMessage = StringUtils.EMPTY;
    json = GitHubUtils.extractJson(exceptionMessage);
    Assertions.assertEquals(StringUtils.EMPTY, json);
  }

  @Test
  void testExtractMessageFromExceptionMessage() {
    // Test case: valid message extraction
    String exceptionMessage = "Some error occurred: {\"message\":\"Invalid input data\"}";
    String extractedMessage = GitHubUtils.extractMessageFromExceptionMessage(exceptionMessage);
    Assertions.assertEquals("Invalid input data", extractedMessage);

    // Test case: no message key
    exceptionMessage = "Some error occurred: {\"error\":\"Something went wrong\"}";
    extractedMessage = GitHubUtils.extractMessageFromExceptionMessage(exceptionMessage);
    Assertions.assertEquals(StringUtils.EMPTY, extractedMessage);

    // Test case: empty exception message
    exceptionMessage = "";
    extractedMessage = GitHubUtils.extractMessageFromExceptionMessage(exceptionMessage);
    Assertions.assertEquals(StringUtils.EMPTY, extractedMessage);
  }
}
