package com.axonivy.market.core.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <p>
 * Core request parameter constants defining HTTP request parameter names used in API endpoints within the Core module.
 * </p>
 *
 * @since 15/04/2026
 * @author ntqdinh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoreRequestParamConstants {
  public static final String ID = "id";
  public static final String TYPE = "type";
  public static final String KEYWORD = "keyword";
  public static final String LANGUAGE = "language";
  public static final String VERSION = "version";
  public static final String SHOW_DEV_VERSION = "isShowDevVersion";
  public static final String DESIGNER_VERSION = "designerVersion";
  public static final String PRODUCT_VERSION = "productVersion";
  public static final String URL = "url";
}
