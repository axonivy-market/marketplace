package com.axonivy.market.core.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * <p>
 * Development version enumeration defining different version types for product releases and tracking.
 * </p>
 *
 * @since 15/04/2026
 * @author nqhoan
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public enum DevelopmentVersion {

  DEV("dev"), NIGHTLY("nightly"), SPRINT("sprint"), LATEST("latest");

  public static final List<DevelopmentVersion> DYNAMIC_DEVELOPMENT_VERSIONS = List.of(DEV, NIGHTLY);

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
