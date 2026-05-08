package com.axonivy.market.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum ProductSecuritySortOption {
  DEPENDABOT_ALERTS("dependabotAlerts"),
  CODE_SCANNING_ALERTS("codeScanningAlerts"),
  SECRET_SCANNING_ALERTS("secretScanningAlerts"),
  BRANCH_PROTECTION("branchProtection"),
  COMMIT_DATE("commitDate"),
  REPO_NAME("repoName");

  private final String field;

  public static ProductSecuritySortOption of(String field) {
    if (StringUtils.isBlank(field)) {
      return REPO_NAME;
    }
    for (var option : values()) {
      if (StringUtils.equalsIgnoreCase(option.field, field)) {
        return option;
      }
    }
    return REPO_NAME;
  }
}

