package com.axonivy.market.enums;

import com.axonivy.market.entity.ProductSecurityInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum ProductSecuritySortOption {
  DEPENDABOT_ALERTS("dependabotAlerts"),
  CODE_SCANNING_ALERTS("codeScanningAlerts"),
  SECRET_SCANNING_ALERTS("secretScanningAlerts"),
  REPO_NAME("repoName");

  private static final List<String> SEVERITY_ORDER = List.of("critical", "high", "medium", "low");
  private static final int[] SEVERITY_WEIGHTS = {1000, 100, 10, 1};

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

