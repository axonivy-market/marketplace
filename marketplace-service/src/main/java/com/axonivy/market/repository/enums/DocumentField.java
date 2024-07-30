package com.axonivy.market.repository.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum DocumentField {
  LISTED("listed", false), TYPE("type", false), NAMES("names", true), SHORT_DESCRIPTIONS("shortDescriptions", true),
  MARKET_DIRECTORY("marketDirectory", false);

  private String fieldName;
  private boolean isSupportedLocalized;
}
