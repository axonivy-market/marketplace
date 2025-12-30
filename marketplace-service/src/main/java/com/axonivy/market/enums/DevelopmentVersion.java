package com.axonivy.market.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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

  public final static List<DevelopmentVersion> DYNAMIC_DEVELOPMENT_VERSIONS = List.of(DEV, NIGHTLY);
}
