package com.axonivy.market.stable.strategy.impl;

import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.core.exceptions.model.NotFoundException;
import com.axonivy.market.core.strategy.VersionMatchStrategy;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class SameMajorVersionStrategy implements VersionMatchStrategy {
  @Override
  public String findMatch(List<String> versions, String requestedVersion) {
    if (CollectionUtils.isEmpty(versions)) {
      throw new NotFoundException(ErrorCode.VERSION_NOT_FOUND, "Cannot found version: " + requestedVersion);
    }

    String targetMajor = requestedVersion.split("\\.")[0];

//    return versions.stream()
//        .filter(v -> {
//          if (v == null) return false;
//          String[] parts = v.split("\\.");
//          return parts.length > 0 && parts[0].equals(targetMajor);
//        })
//        .findFirst()
//        .orElse(versions.get(0));

    return versions.stream()
        .filter(v -> {
          if (v == null) return false;
          String[] parts = v.split("\\.");
          return parts.length > 0 && parts[0].equals(targetMajor);
        })
        .findFirst()
        .orElseThrow(() -> new NotFoundException(
            ErrorCode.VERSION_NOT_FOUND,
            "No version found for major: " + targetMajor
        ));
  }
}
