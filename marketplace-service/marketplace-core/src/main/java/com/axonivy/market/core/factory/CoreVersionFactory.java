package com.axonivy.market.core.factory;

import com.axonivy.market.core.comparator.MavenVersionComparator;
import com.axonivy.market.core.enums.DevelopmentVersion;
import com.axonivy.market.core.utils.CoreVersionUtils;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.axonivy.market.core.constants.CoreMavenConstants.DEV_RELEASE_POSTFIX;
import static com.axonivy.market.core.constants.CoreMavenConstants.DEV_RELEASE_PREFIX;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class CoreVersionFactory {
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

  protected static String findVersionStartWith(List<String> releaseVersions, String version) {
    if (CollectionUtils.isEmpty(releaseVersions)) {
      return version;
    }
    return releaseVersions.stream().filter(ver -> ver.startsWith(version)).findAny().orElse(releaseVersions.get(0));
  }
}
