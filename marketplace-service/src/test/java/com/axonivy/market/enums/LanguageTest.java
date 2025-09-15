package com.axonivy.market.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LanguageTest {
  @Test
  void testOfReturnsEnForLowercase() {
    assertEquals(Language.EN, Language.of("en"),
        "Expected 'en' to map to Language.EN");
  }

  @Test
  void testOfReturnsDeForLowercase() {
    assertEquals(Language.DE, Language.of("de"),
        "Expected 'de' to map to Language.DE");
  }

  @Test
  void testOfIsCaseInsensitive() {
    assertEquals(Language.EN, Language.of("EN"),
        "Expected 'EN' to map to Language.EN");
    assertEquals(Language.DE, Language.of("De"),
        "Expected 'De' to map to Language.DE");
  }

  @Test
  void testOfReturnsNullForInvalidValue() {
    assertNull(Language.of("fr"),
        "Expected 'fr' to return null since it's not supported");
  }

  @Test
  void testOfReturnsNullForNullInput() {
    assertNull(Language.of(null),
        "Expected null input to return null");
  }

  @Test
  void testEnumValuesHaveCorrectValueField() {
    assertEquals("en", Language.EN.getValue(),
        "Expected Language.EN to have value 'en'");
    assertEquals("de", Language.DE.getValue(),
        "Expected Language.DE to have value 'de'");
  }
}
