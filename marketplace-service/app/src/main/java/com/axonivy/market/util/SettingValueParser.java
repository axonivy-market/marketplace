package com.axonivy.market.util;

import com.axonivy.market.enums.AppSettingKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SettingValueParser {

  public static Long parseLong(String value, AppSettingKey settingKey) {
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException ex) {
      try {
        log.warn("Invalid integer value '{}' for setting key '{}', using default value '{}'.", value, settingKey,
            settingKey.getDefaultValue(), ex);
        return Long.parseLong(settingKey.getDefaultValue());
      } catch (NumberFormatException defaultEx) {
        log.warn("Invalid default value '{}' for setting key '{}'.", settingKey.getDefaultValue(), settingKey,
            defaultEx);
        return 0L;
      }
    }
  }

  public static Integer parseInteger(String value, AppSettingKey settingKey) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException ex) {
      try {
        log.warn("Invalid integer value '{}' for setting key '{}', using default value '{}'.", value, settingKey,
            settingKey.getDefaultValue(), ex);
        return Integer.parseInt(settingKey.getDefaultValue());
      } catch (NumberFormatException defaultEx) {
        log.warn("Invalid default value '{}' for setting key '{}'.", settingKey.getDefaultValue(), settingKey,
            defaultEx);
        return null;
      }
    }
  }

  public static Boolean parseBoolean(String value) {
    return Boolean.valueOf(value);
  }
}

