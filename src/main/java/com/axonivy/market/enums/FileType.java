package com.axonivy.market.enums;

import org.apache.commons.lang3.StringUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileType {
  META("meta.json"), LOGO("logo.png");

  private String fileName;

  public static FileType of(String name) {
    for (var type : values()) {
      if (StringUtils.endsWithIgnoreCase(name, type.getFileName())) {
        return type;
      }
    }
    return null;
  }
}
