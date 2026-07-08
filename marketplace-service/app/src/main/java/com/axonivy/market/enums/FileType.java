package com.axonivy.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.Strings;

/**
 * <p>
 * File type enumeration defining categorization of supported product metadata and resource files.
 * </p>
 *
 * @since 15/04/2026
 * @author nqhoan
 */
@Getter
@AllArgsConstructor
public enum FileType {
  META("meta.json"), LOGO("logo.png"), LOGO_DARK("logo-dark.png"), OTHER("other");

  private final String fileName;

  public static FileType of(String name) {
    for (var type : values()) {
      if (Strings.CI.endsWith(name, type.getFileName())) {
        return type;
      }
    }
    return OTHER;
  }
}
