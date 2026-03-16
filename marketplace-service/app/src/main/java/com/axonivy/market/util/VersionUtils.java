package com.axonivy.market.util;

import com.axonivy.market.core.enums.DevelopmentVersion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.axonivy.market.core.constants.CoreCommonConstants.*;
import static com.axonivy.market.core.constants.CoreMavenConstants.*;
import static com.axonivy.market.core.utils.CoreVersionUtils.isReleasedVersion;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VersionUtils {
  private static final Pattern INVALID_VERSION_CHAR_PATTERN = Pattern.compile("[^\\p{L}\\p{N}._-]",
      Pattern.UNICODE_CHARACTER_CLASS);
  private static final String VERSION_REGEX = "^\\d+(\\.\\d+){0,5}(-[\\p{L}\\p{N}.]+)?$";
  private static final Pattern VERSION_PATTERN = Pattern.compile(VERSION_REGEX);
  private static final Pattern MAIN_VERSION_PATTERN = Pattern.compile(MAIN_VERSION_REGEX);
  private static final Pattern SPRINT_RELEASE_PATTERN = Pattern.compile(SPRINT_RELEASE_POSTFIX);

  public static boolean isSnapshotVersion(String version) {
    return version.endsWith(SNAPSHOT_RELEASE_POSTFIX);
  }

  public static boolean isMatchWithDesignerVersion(String version, String designerVersion) {
    return isReleasedVersion(version) && version.startsWith(designerVersion);
  }

  public static List<String> removeSyncedVersionsFromReleasedVersions(List<String> releasedVersion,
      Set<String> syncTags) {
    if (ObjectUtils.isNotEmpty(syncTags)) {
      releasedVersion.removeAll(syncTags);
    }
    return releasedVersion;
  }

  public static String getNumbersOnly(String version) {
    return StringUtils.defaultIfBlank(version, StringUtils.EMPTY).split(DASH_SEPARATOR)[0];
  }

  public static boolean isMajorVersion(String version) {
    return MAIN_VERSION_PATTERN.split(getNumbersOnly(version)).length == ONE &&
        isReleasedVersion(version);
  }

  public static boolean isMinorVersion(String version) {
    return MAIN_VERSION_PATTERN.split(getNumbersOnly(version)).length == TWO &&
        isReleasedVersion(version);
  }

  public static boolean isMavenVersion(String versionStr) {
    if (StringUtils.isBlank(versionStr)) {
      return false;
    }

    return VERSION_PATTERN.matcher(versionStr).matches();
  }

  public static boolean isDevelopmentVersion(String version) {
    if (StringUtils.isBlank(version)) {
      return false;
    }

    for (var dv : DevelopmentVersion.values()) {
      if (version.startsWith(dv.getCode()) || version.endsWith(dv.getCode())) {
        return true;
      }
    }

    return false;
  }

  public static String normalizeVersion(String version) {
    if (StringUtils.isBlank(version)) {
      return EMPTY;
    }

    return INVALID_VERSION_CHAR_PATTERN
        .matcher(version)
        .replaceAll(EMPTY);
  }
}
