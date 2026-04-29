package com.axonivy.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * <p>
 * Document language enumeration defining supported languages for document indexing and display.
 * </p>
 *
 * @since 15/04/2026
 * @author pvquan
 */
@Getter
@AllArgsConstructor
public enum DocumentLanguage {

  ENGLISH("en"), JAPANESE("ja");

  private final String code;

  public static List<String> getCodes() {
    return Stream.of(values()).map(DocumentLanguage::getCode).toList();
  }

  public static DocumentLanguage fromCode(String code) {
    return Arrays.stream(values())
        .filter(lang -> lang.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unsupported language: " + code));
  }
}
