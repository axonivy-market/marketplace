package com.axonivy.market.enums;

import com.axonivy.market.exceptions.model.InvalidParamException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.data.domain.Sort;

public class SortOptionTest {
  @Test
  void testOfValidOptions() {
    assertEquals(SortOption.POPULARITY, SortOption.of("popularity"),
        "of() should return POPULARITY for option 'popularity'");
    assertEquals(SortOption.ALPHABETICALLY, SortOption.of("AlPhAbEtIcAlLy"),
        "of() should return ALPHABETICALLY for case-insensitive input");
    assertEquals(SortOption.RECENT, SortOption.of(" recent "),
        "of() should trim input and return RECENT");
    assertEquals(SortOption.STANDARD, SortOption.of("standard"),
        "of() should return STANDARD for option 'standard'");
    assertEquals(SortOption.ID, SortOption.of("id"),
        "of() should return ID for option 'id'");
  }

  @Test
  void testOfInvalidOptionThrows() {
    InvalidParamException ex = assertThrows(InvalidParamException.class,
        () -> SortOption.of("unknown"),
        "of() should throw InvalidParamException for an invalid option");
    assertTrue(ex.getMessage().contains("SortOption: unknown"),
        "Exception message should include the invalid option");
  }

  @Test
  void testOfBlankOptionThrows() {
    assertThrows(InvalidParamException.class,
        () -> SortOption.of(" "),
        "of() should throw InvalidParamException for blank input");
  }

  @Test
  void testOfNullOptionThrows() {
    assertThrows(InvalidParamException.class,
        () -> SortOption.of(null),
        "of() should throw InvalidParamException for null input");
  }

  @Test
  void testGetCodeForAlphabeticallyWithLanguage() {
    String code = SortOption.ALPHABETICALLY.getCode("en");
    assertEquals("names.en", code,
        "getCode() should append language for ALPHABETICALLY option when language is not blank");
  }

  @Test
  void testGetCodeForAlphabeticallyWithBlankLanguage() {
    String code = SortOption.ALPHABETICALLY.getCode(" ");
    assertEquals("names", code,
        "getCode() should return base code if language is blank for ALPHABETICALLY option");
  }

  @Test
  void testGetCodeForOtherOptionsIgnoresLanguage() {
    String code = SortOption.RECENT.getCode("de");
    assertEquals("firstPublishedDate", code,
        "getCode() should ignore language for non-ALPHABETICALLY options");
  }

  @Test
  void testEnumFields() {
    assertEquals("popularity", SortOption.POPULARITY.name().toLowerCase(),
        "Enum name should match the defined option string (case-insensitive)");
    assertEquals("marketplaceData.installationCount", SortOption.POPULARITY.getCode(null),
        "POPULARITY should have correct code");
    assertEquals(Sort.Direction.DESC, SortOption.RECENT.getDirection(),
        "RECENT should have Sort.Direction.DESC direction");
  }
}
