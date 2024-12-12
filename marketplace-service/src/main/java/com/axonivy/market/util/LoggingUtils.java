package com.axonivy.market.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggingUtils {

  public static String getCurrentDate() {
    return new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
  }

  public static String getCurrentTimestamp() {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
  }

  public static String escapeXml(String value) {
    if (value == null) {
      return "";
    }
    return value.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;");
  }

  public static String getArgumentsString(String[] paramNames, Object[] args) {
    if (paramNames == null || paramNames.length == 0 || args == null || args.length == 0) {
      return "No arguments";
    }
    return IntStream.range(0, paramNames.length)
        .mapToObj(i -> paramNames[i] + ": " + args[i])
        .collect(Collectors.joining(", "));
  }

  public static String buildLogEntry(Map<String, String> headersMap) {
    StringBuilder logEntry = new StringBuilder();
    Map<String, String> map = new TreeMap<>(headersMap);
    logEntry.append("  <LogEntry>\n");
    map.forEach((key, value) -> logEntry.append(String.format("    <%s>%s</%s>%n", key, value, key)));
    logEntry.append("  </LogEntry>\n");
    return logEntry.toString();
  }

}
