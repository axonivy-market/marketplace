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
public enum SecurityMonitorSortOption {
  DEPENDABOT_ALERTS("dependabotAlerts"),
  CODE_SCANNING_ALERTS("codeScanningAlerts"),
  SECRET_SCANNING_ALERTS("secretScanningAlerts"),
  REPO_NAME("repoName");

  private static final List<String> SEVERITY_ORDER = List.of("critical", "high", "medium", "low");
  private static final int[] SEVERITY_WEIGHTS = {1000, 100, 10, 1};

  private final String field;

  public Comparator<ProductSecurityInfo> getComparator() {
    return switch (this) {
      case DEPENDABOT_ALERTS ->
          Comparator.comparingInt((ProductSecurityInfo p) -> get(p, "critical")).reversed()
              .thenComparingInt(p -> get(p, "high")).reversed()
              .thenComparingInt(p -> get(p, "medium")).reversed()
              .thenComparingInt(p -> get(p, "low")).reversed();
      case CODE_SCANNING_ALERTS -> Comparator.comparingInt(
          (ProductSecurityInfo p) -> alertScore(p.getCodeScanning() != null ? p.getCodeScanning().getAlerts() : null)
      ).reversed();
      case SECRET_SCANNING_ALERTS -> Comparator.comparingInt(
          (ProductSecurityInfo p) -> p.getSecretScanning() != null
              && p.getSecretScanning().getNumberOfSecretScanningAlerts() != null
              ? p.getSecretScanning().getNumberOfSecretScanningAlerts() : 0
      ).reversed();
      default -> Comparator.comparing(ProductSecurityInfo::getRepoName);
    };
  }

  private static int get(ProductSecurityInfo p, String severity) {
    if (p.getDependabot() == null || p.getDependabot().getAlerts() == null) {
      return 0;
    }
    return p.getDependabot().getAlerts().getOrDefault(severity, 0);
  }

  private static int alertScore(Map<String, Integer> alerts) {
    if (alerts == null) {
      return 0;
    }
    int score = 0;
    for (int i = 0; i < SEVERITY_ORDER.size(); i++) {
      score += alerts.getOrDefault(SEVERITY_ORDER.get(i), 0) * SEVERITY_WEIGHTS[i];
    }
    return score;
  }

  public static SecurityMonitorSortOption of(String field) {
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

