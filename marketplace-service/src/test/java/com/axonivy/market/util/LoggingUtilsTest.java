package com.axonivy.market.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class LoggingUtilsTest {

  @Test
  void testEscapeXml() {
    String input = "<Test'& \"Method>";
    String expectedValue = "&lt;Test&apos;&amp; &quot;Method&gt;";
    String result = LoggingUtils.escapeXml(input);
    Assertions.assertEquals(expectedValue, result);
  }

  @Test
  void testGetArgumentsString() {
    String expectedValue = "a: random, b: sample";
    String result = LoggingUtils.getArgumentsString(new String[]{"a", "b"}, new String[]{"random", "sample"});
    Assertions.assertEquals(expectedValue, result);
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

    Assertions.assertEquals(expected, result);
  }

}
