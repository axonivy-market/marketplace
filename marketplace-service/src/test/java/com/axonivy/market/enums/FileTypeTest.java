package com.axonivy.market.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileTypeTest {
  @Test
  void testOfReturnsMetaForMetaJson() {
    FileType result = FileType.of("my-meta.json");
    assertEquals(FileType.META, result,
        "FileType.of should return META when name ends with 'meta.json'");
  }

  @Test
  void testOfReturnsLogoForLogoPng() {
    FileType result = FileType.of("assets/logo.png");
    assertEquals(FileType.LOGO, result,
        "FileType.of should return LOGO when name ends with 'logo.png'");
  }

  @Test
  void testOfIsCaseInsensitive() {
    FileType result = FileType.of("META.JSON");
    assertEquals(FileType.META, result,
        "FileType.of should ignore case when matching file names");
  }

  @Test
  void testOfReturnsOtherForUnknownFile() {
    FileType result = FileType.of("readme.md");
    assertEquals(FileType.OTHER, result,
        "FileType.of should return OTHER when file name does not match any known suffix");
  }

  @Test
  void testOfReturnsOtherForNullInput() {
    FileType result = FileType.of(null);
    assertEquals(FileType.OTHER, result,
        "FileType.of should return OTHER when input is null");
  }

  @Test
  void testOfReturnsOtherForEmptyInput() {
    FileType result = FileType.of("");
    assertEquals(FileType.OTHER, result,
        "FileType.of should return OTHER when input is empty");
  }

  @Test
  void testFileNameGetter() {
    assertEquals("meta.json", FileType.META.getFileName(),
        "META should have fileName 'meta.json'");
    assertEquals("logo.png", FileType.LOGO.getFileName(),
        "LOGO should have fileName 'logo.png'");
    assertEquals("other", FileType.OTHER.getFileName(),
        "OTHER should have fileName 'other'");
  }
}
