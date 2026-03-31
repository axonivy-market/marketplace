package com.axonivy.market.core.factory;

import com.axonivy.market.core.comparator.MavenVersionComparator;

import static com.axonivy.market.core.constants.CoreMavenConstants.DEV_RELEASE_POSTFIX;
import static com.axonivy.market.core.constants.CoreMavenConstants.DEV_RELEASE_PREFIX;

import com.axonivy.market.core.enums.DevelopmentVersion;
import com.axonivy.market.core.utils.CoreVersionUtils;

import static org.apache.commons.lang3.StringUtils.EMPTY;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class CoreVersionFactory {

  public static String findVersionStartWithOrNull(List<String> releaseVersions, String version) {
    if (CollectionUtils.isEmpty(releaseVersions)) {
      return version;
    }
    return releaseVersions.stream().filter(ver -> ver.startsWith(version)).findAny().orElse(null);
  }

  public static String findLowerVersion(List<String> releaseVersions, String version) {
    if (releaseVersions == null || releaseVersions.isEmpty()) {
      return null;
    }
    return releaseVersions.stream().filter(v -> MavenVersionComparator.compare(v, version) < 0).findFirst().orElse(
        null);
  }

  public static String get(List<String> versions, String requestedVersion) {
    var sortedVersions = Optional.ofNullable(versions).orElse(new ArrayList<>()).stream()
        .filter(Objects::nonNull)
        .sorted((v1, v2) -> MavenVersionComparator.compare(v2, v1)).toList();

    // Redirect to the newest version for special keywords
    var version = DevelopmentVersion.of(requestedVersion);

    // Get latest released version if requested version is 'latest' or 'sprint'
    if (version == DevelopmentVersion.LATEST || version == DevelopmentVersion.SPRINT) {
      return sortedVersions.stream().filter(CoreVersionUtils::isReleasedVersion)
          .findFirst().orElse(null);
    }

    if (version != null && !sortedVersions.isEmpty()) {
      return sortedVersions.get(0);
    }

    // e.g. 10.0-dev
    if (requestedVersion.endsWith(DEV_RELEASE_POSTFIX)) {
      requestedVersion = requestedVersion.replace(DEV_RELEASE_POSTFIX, EMPTY);
    }

    // e.g. dev-10.0
    if (requestedVersion.startsWith(DEV_RELEASE_PREFIX)) {
      requestedVersion = requestedVersion.replace(DEV_RELEASE_PREFIX, EMPTY);
    }

    return findVersionStartWith(sortedVersions, requestedVersion);
  }

  private static String findVersionStartWith(List<String> releaseVersions, String version) {
    if (CollectionUtils.isEmpty(releaseVersions)) {
      return version;
    }

    return releaseVersions.stream()
        .filter(sameMajorVersion(version))
        .findFirst()
        .orElse(releaseVersions.get(0));
  }

  private static Predicate<String> sameMajorVersion(String requestedVersion) {
    String targetMajor = requestedVersion.split("\\.")[0];

    return version -> {
      if (version == null) {
        return false;
      }
      String[] parts = version.split("\\.");
      return parts.length > 0 && parts[0].equals(targetMajor);
    };
  }
}
