package com.axonivy.market.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorMessageConstants {
  public static final String INVALID_MISSING_HEADER_ERROR_MESSAGE = "Invalid or missing header";
  public static final String CURRENT_CLIENT_ID_MISMATCH_MESSAGE = " Client ID mismatch (Request ID: %s, Server ID: %s)";
  public static final String INVALID_USER_ERROR = "%s - User must be a member of team %s and organization %s";
}
