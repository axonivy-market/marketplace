package com.axonivy.market.factory;

import com.axonivy.market.comparator.MavenVersionComparator;
import com.axonivy.market.enums.DevelopmentVersion;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.axonivy.market.constants.MavenConstants.DEV_RELEASE_POSTFIX;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VersionFactory {

  public static String get(List<String> versions, String requestedVersion) {
    var releaseVersions = Optional.ofNullable(versions).orElse(new ArrayList<>()).stream()
        .filter(Objects::nonNull)
        .sorted((v1, v2) -> MavenVersionComparator.compare(v2, v1)).toList();
    // Redirect to the newest version for special keywords
    var version = DevelopmentVersion.of(requestedVersion);
    if (version != null) {
      return MavenVersionComparator.findHighestMavenVersion(releaseVersions);
    }

    // e.g. 10.0-dev
    if (requestedVersion.endsWith(DEV_RELEASE_POSTFIX)) {
      requestedVersion = requestedVersion.replace(DEV_RELEASE_POSTFIX, EMPTY);
    }
    return findVersionStartWith(releaseVersions, requestedVersion);
  }

  private static String findVersionStartWith(List<String> releaseVersions, String version) {
    return Optional.ofNullable(releaseVersions).orElse(List.of()).stream().filter(
        ver -> ver.startsWith(version)).findAny().orElse(version);
  }
}
