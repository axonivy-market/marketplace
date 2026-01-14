package com.axonivy.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum SecurityFeature {
  DEPENDABOT("Dependabot"),
  SECRET_SCANNING("Secret Scanning"),
  CODE_SCANNING("Code Scanning"),
  BRANCH_PROTECTION("Branch Protection");

  private final String securityLabel;

  public static SecurityFeature of(String name) {
    for (var feature : values()) {
      if (StringUtils.endsWithIgnoreCase(name, feature.getSecurityLabel())) {
        return feature;
      }
    }
    return null;
  }
}
