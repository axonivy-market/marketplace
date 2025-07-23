package com.axonivy.market.enums;

import lombok.Getter;

@Getter
public enum WorkFlowType {
  CI("ci.yml"),
  DEV("dev.yml");

  private final String fileName;

  WorkFlowType(String fileName) {
    this.fileName = fileName;
  }
}