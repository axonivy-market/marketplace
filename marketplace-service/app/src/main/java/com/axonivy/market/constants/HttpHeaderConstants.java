package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * <p>
 * HTTP header constants defining custom HTTP headers used for client identification and proxy information.
 * </p>
 *
 * @since 15/04/2026
 * @author ntqdinh
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpHeaderConstants {
  public static final String X_FORWARDED_FOR = "X-Forwarded-For";
  public static final String X_REAL_IP = "X-Real-IP";
}
