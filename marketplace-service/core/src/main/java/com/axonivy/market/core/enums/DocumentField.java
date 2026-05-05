package com.axonivy.market.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Document field enumeration defining searchable fields in the document indexing system.
 * </p>
 *
 * @since 15/04/2026
 * @author nqhoan
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum DocumentField {
  LISTED("listed", false),
  TYPE("type", false),
  NAMES("names", true),
  SHORT_DESCRIPTIONS("shortDescriptions", true),
  MARKET_DIRECTORY("marketDirectory", false);

  private String fieldName;
  private boolean isLocalizedSupport;
}
