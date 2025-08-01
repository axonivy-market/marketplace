package com.axonivy.market.enums;

import lombok.Getter;

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