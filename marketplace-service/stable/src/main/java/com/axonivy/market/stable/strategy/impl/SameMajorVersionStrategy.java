package com.axonivy.market.stable.strategy.impl;

import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.core.strategy.VersionMatchStrategy;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.axonivy.market.core.constants.CoreMavenConstants.MAIN_VERSION_REGEX;

public class SameMajorVersionStrategy implements VersionMatchStrategy {
  private static final String VERSION_NOT_FOUND_MESSAGE = "Cannot find version: %s";

  @Override
  public String findMatch(List<String> versions, String requestedVersion) {
    if (CollectionUtils.isEmpty(versions)) {
      throw new NotFoundException(
          ErrorCode.VERSION_NOT_FOUND, String.format(VERSION_NOT_FOUND_MESSAGE, requestedVersion));
    }

    String targetMajor = requestedVersion.split("\\.")[0];

    return versions.stream()
        .filter(version -> hasSameMajorVersion(version, targetMajor))
        .findFirst()
        .orElseThrow(() -> versionNotFound(requestedVersion));
  }

  private boolean hasSameMajorVersion(String version, String targetMajor) {
    String[] parts = version.split(MAIN_VERSION_REGEX);
    return parts.length > 0 && parts[0].equals(targetMajor);
  }

  private NotFoundException versionNotFound(String requestedVersion) {
    return new NotFoundException(
        ErrorCode.VERSION_NOT_FOUND, String.format(VERSION_NOT_FOUND_MESSAGE, requestedVersion)
    );
  }
}
