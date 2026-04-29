package com.axonivy.market.core.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Core PostgreSQL database constants defining column/field names and SQL keywords for database operations in the Core module.
 * </p>
 *
 * @since 15/04/2026
 * @author tvtphuc
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CorePostgresDBConstants {
  public static final String ID = "id";
  public static final String PRODUCT_MARKETPLACE_DATA = "productMarketplaceData";
  public static final String INSTALLATION_COUNT = "installationCount";
  public static final String PRODUCT_ID = "productId";
  public static final String CUSTOM_ORDER = "customOrder";
  public static final String PRODUCT_NAMES = "names";
  public static final String FIRST_PUBLISHED_DATE = "firstPublishedDate";
  public static final String LISTED = "listed";
  public static final String TYPE = "type";
  public static final String PRODUCT_SHORT_DESCRIPTION = "shortDescriptions";
  public static final String PRODUCT_ARTIFACT = "artifacts";
}
