package com.axonivy.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum Language {
  EN("en"), DE("de");

  private final String value;

  public static Language of(String lang) {
    for (var language : values()) {
      if (StringUtils.equalsIgnoreCase(lang, language.value)) {
        return language;
      }
    }
    return null;
  }
}
