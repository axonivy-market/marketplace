package com.axonivy.market.strategy.impl;

import com.axonivy.market.core.strategy.VersionMatchStrategy;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class StartsWithVersionStrategy implements VersionMatchStrategy {
  @Override
  public String findMatch(List<String> releaseVersions, String version) {
    if (CollectionUtils.isEmpty(releaseVersions)) {
      return version;
    }
    return releaseVersions.stream().filter(ver -> ver.startsWith(version)).findAny().orElse(releaseVersions.get(0));
  }
}
