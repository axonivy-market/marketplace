package com.axonivy.market.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.Strings;

/**
 * <p>
 * Language enumeration defining supported product description languages for marketplace display.
 * </p>
 *
 * @since 15/04/2026
 * @author vhhoang
 */
@Getter
@AllArgsConstructor
public enum Language {
  EN("en"), DE("de");

  private final String value;

  public static Language of(String lang) {
    for (var language : values()) {
      if (Strings.CS.equals(lang, language.value)) {
        return language;
      }
    }
    return null;
  }
}
