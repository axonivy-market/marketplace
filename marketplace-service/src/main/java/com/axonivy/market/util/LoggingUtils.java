package com.axonivy.market.util;

import com.axonivy.market.constants.LoggingConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggingUtils {

  public static String getCurrentDate() {
    return new SimpleDateFormat(LoggingConstants.DATE_FORMAT).format(System.currentTimeMillis());
  }

  public static String getCurrentTimestamp() {
    return new SimpleDateFormat(LoggingConstants.TIMESTAMP_FORMAT).format(System.currentTimeMillis());
  }

  public static String escapeXml(String value) {
    if (StringUtils.isEmpty(value)) {
      return StringUtils.EMPTY;
    }
    return value.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }

  public static String getArgumentsString(String[] paramNames, Object[] args) {
    if (paramNames == null || paramNames.length == 0 || args == null || args.length == 0) {
      return LoggingConstants.NO_ARGUMENTS;
    }
    return IntStream.range(0, paramNames.length)
        .mapToObj(i -> paramNames[i] + ": " + args[i])
        .collect(Collectors.joining(", "));
  }

  public static String buildLogEntry(Map<String, String> headersMap) {
    var logEntry = new StringBuilder();
    Map<String, String> map = new TreeMap<>(headersMap);
    logEntry.append(LoggingConstants.ENTRY_START);
    map.forEach((key, value) -> logEntry.append(String.format(LoggingConstants.ENTRY_FORMAT, key, value, key)));
    logEntry.append(LoggingConstants.ENTRY_END);
    return logEntry.toString();
  }

}
