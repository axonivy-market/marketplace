package com.axonivy.market.util;

import com.axonivy.market.enums.AppSettingKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SettingValueParserTest {

  @Test
  void testParseLongWithValidValue() {
    Long result = SettingValueParser.parseLong("12345", AppSettingKey.GITHUB_CONNECT_TIMEOUT);
    assertEquals(12345L, result, "Should parse valid long value");
  }

  @Test
  void testParseLongFallsBackToDefault() {
    Long result = SettingValueParser.parseLong("invalid", AppSettingKey.GITHUB_CONNECT_TIMEOUT);
    assertEquals(10000L, result, "Should fall back to default when value is not a valid long");
  }

  @Test
  void testParseLongReturnsZeroWhenBothInvalid() {
    Long result = SettingValueParser.parseLong("invalid", AppSettingKey.GITHUB_TOKEN);
    assertEquals(0L, result, "Should return 0 when both value and default are invalid");
  }

  @Test
  void testParseIntegerWithValidValue() {
    Integer result = SettingValueParser.parseInteger("42", AppSettingKey.MAIL_PORT);
    assertEquals(42, result, "Should parse valid integer value");
  }

  @Test
  void testParseIntegerFallsBackToDefault() {
    Integer result = SettingValueParser.parseInteger("invalid", AppSettingKey.MAIL_PORT);
    assertEquals(587, result, "Should fall back to default when value is not a valid integer");
  }

  @Test
  void testParseIntegerReturnsNullWhenBothInvalid() {
    Integer result = SettingValueParser.parseInteger("invalid", AppSettingKey.GITHUB_TOKEN);
    assertNull(result, "Should return null when both value and default are invalid");
  }

  @Test
  void testParseBooleanTrue() {
    Boolean result = SettingValueParser.parseBoolean("true");
    assertTrue(result, "Should parse 'true' string as Boolean.TRUE");
  }

  @Test
  void testParseBooleanFalse() {
    Boolean result = SettingValueParser.parseBoolean("false");
    assertFalse(result, "Should parse 'false' string as Boolean.FALSE");
  }

  @Test
  void testParseBooleanInvalidReturnsFalse() {
    Boolean result = SettingValueParser.parseBoolean("notaboolean");
    assertFalse(result, "Should return false for non-boolean strings");
  }

  @Test
  void testParseBooleanNull() {
    Boolean result = SettingValueParser.parseBoolean(null);
    assertFalse(result, "Should return false for null input");
  }
}
