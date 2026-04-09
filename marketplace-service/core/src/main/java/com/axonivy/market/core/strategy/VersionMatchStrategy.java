package com.axonivy.market.core.strategy;

import java.util.List;

public interface VersionMatchStrategy {
  String findMatch(List<String> versions, String requestedVersion);
}
