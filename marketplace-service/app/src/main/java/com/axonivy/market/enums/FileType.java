package com.axonivy.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum FileType {
  META("meta.json"), LOGO("logo.png"), OTHER("other");

  private final String fileName;

  public static FileType of(String name) {
    for (var type : values()) {
      if (StringUtils.endsWithIgnoreCase(name, type.getFileName())) {
        return type;
      }
    }
    return OTHER;
  }
}
