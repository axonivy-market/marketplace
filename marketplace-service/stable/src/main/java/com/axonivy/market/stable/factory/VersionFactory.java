package com.axonivy.market.stable.factory;

import com.axonivy.market.core.factory.CoreVersionFactory;
import com.axonivy.market.core.strategy.VersionMatchStrategy;
import com.axonivy.market.stable.strategy.impl.SameMajorVersionStrategy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class VersionFactory extends CoreVersionFactory {

  private static final VersionMatchStrategy STRATEGY = new SameMajorVersionStrategy();

  public static String get(List<String> versions, String requestedVersion) {
    return CoreVersionFactory.get(versions, requestedVersion, STRATEGY);
  }
}
