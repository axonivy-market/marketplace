package com.axonivy.market.enums;

import lombok.Getter;

/**
 * <p>
 * Workflow type enumeration defining different types of CI/CD workflow configurations.
 * </p>
 *
 * @since 15/04/2026
 * @author ttan
 */
@Getter
public enum WorkFlowType {
  CI("ci.yml"),
  DEV("dev.yml"),
  E2E("e2e.yml");

  private final String fileName;

  WorkFlowType(String fileName) {
    this.fileName = fileName;
  }
}
