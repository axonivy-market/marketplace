package com.axonivy.market.util;

import com.axonivy.market.constants.LoggingConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class LoggingUtilsTest {

  @Test
  void testEscapeXmlSuccess() {
    String input = "<Test'& \"Method>";
    String expectedValue = "&lt;Test&apos;&amp; &quot;Method&gt;";
    String result = LoggingUtils.escapeXml(input);
    Assertions.assertEquals(expectedValue, result);
  }

  @Test
  void testEscapeXmlOnNullValue() {
    String expectedValue = "";
    String result = LoggingUtils.escapeXml(null);
    Assertions.assertEquals(expectedValue, result);
  }

  @Test
  void testGetArgumentsString() {
    String expectedValue = "a: random, b: sample";
    String result = LoggingUtils.getArgumentsString(new String[]{"a", "b"}, new String[]{"random", "sample"});
    Assertions.assertEquals(expectedValue, result);
  }

  @Test
  void testGetArgumentsStringOnNullValue() {
    String result = LoggingUtils.getArgumentsString(null, null);
    Assertions.assertEquals(LoggingConstants.NO_ARGUMENTS, result);
  }

//  @Test
//  void testBuildLogEntry() {
//    Map<String, String> given = Map.of(
//        "method", "test",
//        "timestamp", "15:02:00"
//    );
//    String expected = """
//        <LogEntry>
//          <method>test</method>
//          <timestamp>15:02:00</timestamp>
//        </LogEntry>
//        """.indent(2);
//
//    var result = LoggingUtils.buildLogEntry(given);
//    Assertions.assertEquals(expected, result);
//  }

  @Test
  void testGetCurrentDate() {
    String expectedDate = LocalDate.now().toString();
    String actualDate = LoggingUtils.getCurrentDate();
    Assertions.assertEquals(expectedDate, actualDate, "The returned date does not match the current date");
  }

  @Test
  void testGetCurrentTimestamp() {
    String expectedTimestamp = LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern(LoggingConstants.TIMESTAMP_FORMAT));
    String actualTimestamp = LoggingUtils.getCurrentTimestamp();
    Assertions.assertEquals(expectedTimestamp.substring(0, 19), actualTimestamp.substring(0, 19),
        "The returned timestamp does not match the expected format or value");
  }

}
