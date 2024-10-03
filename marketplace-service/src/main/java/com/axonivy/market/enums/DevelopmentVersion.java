package com.axonivy.market.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum DevelopmentVersion {

  DEV("dev"), NIGHTLY("nightly"), SPRINT("sprint"), LATEST("latest");

  private String code;

  public static DevelopmentVersion of(String versionNumber) {
    for (var version : values()) {
      if (version.getCode().equals(versionNumber)) {
        return version;
      }
    }
    return null;
  }
}
