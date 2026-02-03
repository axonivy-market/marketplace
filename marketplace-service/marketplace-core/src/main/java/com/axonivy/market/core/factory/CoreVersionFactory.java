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
