package com.axonivy.market.enums;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.market.exceptions.model.NotFoundException;

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
    throw new NotFoundException(ErrorCode.GH_FILE_TYPE_INVALID, "FileType: " + name);
  }
}
