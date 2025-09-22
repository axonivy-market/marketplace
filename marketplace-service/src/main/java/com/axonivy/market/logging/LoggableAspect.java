package com.axonivy.market.logging;

import com.axonivy.market.constants.CommonConstants;
import com.axonivy.market.constants.LoggingConstants;
import com.axonivy.market.exceptions.model.MissingHeaderException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

import static com.axonivy.market.util.FileUtils.createFile;
import static com.axonivy.market.util.FileUtils.writeToFile;
import static com.axonivy.market.util.LoggingUtils.*;

@Log4j2
@Aspect
@Component
public class LoggableAspect {

  @Value("${loggable.log-path}")
  public String logFilePath;

  @Before("@annotation(com.axonivy.market.logging.Loggable)")
  public void logMethodCall(JoinPoint joinPoint) throws MissingHeaderException {
    MethodSignature signature = (MethodSignature) joinPoint.getSignature();
    ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes != null) {
      var method = signature.getMethod();
      HttpServletRequest request = attributes.getRequest();
      Map<String, String> headersMap = extractHeaders(request, method, joinPoint);
      saveLogToDailyFile(headersMap);

      // block execution if request isn't from Market or Ivy Designer
      if (!LoggingConstants.MARKET_WEBSITE.equals(headersMap.get(CommonConstants.REQUESTED_BY))) {
        throw new MissingHeaderException();
      }
    }
  }

  private Map<String, String> extractHeaders(HttpServletRequest request, Method method,
      JoinPoint joinPoint) {
    return Map.of(
        LoggingConstants.METHOD, escapeXml(String.valueOf(method)),
        LoggingConstants.TIMESTAMP, escapeXml(getCurrentTimestamp()),
        CommonConstants.USER_AGENT, escapeXml(request.getHeader(CommonConstants.USER_AGENT)),
        LoggingConstants.ARGUMENTS,
        escapeXml(getArgumentsString(
            Arrays.stream(method.getParameters()).map(Parameter::getName).toArray(String[]::new),
            joinPoint.getArgs())),
        CommonConstants.REQUESTED_BY, escapeXml(request.getHeader(CommonConstants.REQUESTED_BY))
    );
  }

  // Use synchronized to prevent race condition
  private synchronized void saveLogToDailyFile(Map<String, String> headersMap) {
    try {
      var logFile = createFile(generateFileName());

      var content = new StringBuilder();
      if (logFile.exists()) {
        content.append(Files.readString(logFile.toPath(), StandardCharsets.UTF_8));
      }
      if (content.isEmpty()) {
        content.append(LoggingConstants.LOG_START);
      }
      int lastLogIndex = content.lastIndexOf(LoggingConstants.LOG_END);
      if (lastLogIndex != -1) {
        content.delete(lastLogIndex, content.length());
      }
      content.append(buildLogEntry(headersMap));
      content.append(LoggingConstants.LOG_END);

      writeToFile(logFile, content.toString());
    } catch (IOException e) {
      log.error("Error writing log to file: {}", e.getMessage());
    }
  }

  private String generateFileName() {
    return Path.of(logFilePath, "log-" + getCurrentDate() + ".xml").toString();
  }

}
