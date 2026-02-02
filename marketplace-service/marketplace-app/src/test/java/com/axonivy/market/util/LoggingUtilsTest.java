package com.axonivy.market.util;

import com.axonivy.market.constants.LoggingConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class LoggingUtilsTest {

  @Test
  void testEscapeXmlSuccess() {
    String input = "<Test'& \"Method>";
    String expectedValue = "&lt;Test&apos;&amp; &quot;Method&gt;";
    String result = LoggingUtils.escapeXml(input);
    Assertions.assertEquals(expectedValue, result,
        "Input XML should be escaped successfully");
  }

  @Test
  void testEscapeXmlOnNullValue() {
    String expectedValue = "";
    String result = LoggingUtils.escapeXml(null);
    Assertions.assertEquals(expectedValue, result,
        "Escaped string should be empty if input XML is null");
  }

  @Test
  void testGetArgumentsString() {
    String expectedValue = "a: random, b: sample";
    String result = LoggingUtils.getArgumentsString(new String[]{"a", "b"}, new String[]{"random", "sample"});
    Assertions.assertEquals(expectedValue, result,
        "Result string should match expected string");
  }

  @Test
  void testGetArgumentsStringOnNullValue() {
    String result = LoggingUtils.getArgumentsString(null, null);
    Assertions.assertEquals(LoggingConstants.NO_ARGUMENTS, result,
        "Result string should be 'No arguments' if all params are null");
  }

  @Test
  void testBuildLogEntry() {
    Map<String, String> given = Map.of(
        "method", "test",
        "timestamp", "15:02:00"
    );
    String expected = """
        <LogEntry>
          <method>test</method>
          <timestamp>15:02:00</timestamp>
        </LogEntry>
        """.indent(2);

    var result = LoggingUtils.buildLogEntry(given);
    Assertions.assertEquals(expected, result,
        "Result log entry should be built successfully based on input map");
  }

  @Test
  void testGetCurrentDate() {
    String expectedDate = LocalDate.now().toString();
    String actualDate = LoggingUtils.getCurrentDate();
    Assertions.assertEquals(expectedDate, actualDate, "The returned date does not match the current date");
  }

  @Test
  void testGetCurrentTimestamp() {
    String timestamp = LoggingUtils.getCurrentTimestamp();
    Assertions.assertNotNull(timestamp, "Timestamp should not be null");
    SimpleDateFormat dateFormat = new SimpleDateFormat(LoggingConstants.TIMESTAMP_FORMAT);
    Assertions.assertDoesNotThrow(() -> {
      dateFormat.parse(timestamp);
    }, "Timestamp does not match the expected format");
  }

}
