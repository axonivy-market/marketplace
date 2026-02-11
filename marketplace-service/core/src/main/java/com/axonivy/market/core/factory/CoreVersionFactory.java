package com.axonivy.market.core.factory;

import com.axonivy.market.core.comparator.MavenVersionComparator;
import org.springframework.util.CollectionUtils;

import java.util.List;

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
}
