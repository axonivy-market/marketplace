package com.axonivy.market.util;

import com.axonivy.market.core.constants.CoreCommonConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileValidator {

  private static final int BINARY_UNIT_MULTIPLIER = 1024;
  private static final long DEFAULT_MAX_FILE_SIZE = 10L * BINARY_UNIT_MULTIPLIER * BINARY_UNIT_MULTIPLIER;
  private static final Set<String> DEFAULT_ALLOWED_MIME_TYPES = new HashSet<>(Set.of(
      "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
  ));

  public static void validateImageFile(MultipartFile file) throws IOException {
    validateImageFile(file, DEFAULT_MAX_FILE_SIZE, DEFAULT_ALLOWED_MIME_TYPES);
  }

  public static void validateImageFile(MultipartFile file, long maxFileSize) throws IOException {
    validateImageFile(file, maxFileSize, DEFAULT_ALLOWED_MIME_TYPES);
  }

  public static void validateImageFile(MultipartFile file, long maxFileSize, Set<String> allowedMimeTypes) throws IOException {
    validateFileNotEmpty(file);
    validateFileSize(file, maxFileSize);
    validateMimeType(file, allowedMimeTypes);
    validateFilename(file);
    log.info("File validation passed for: {}", file.getOriginalFilename());
  }

  public static void validateFileNotEmpty(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IOException("File is empty or null");
    }
  }

  public static void validateFileSize(MultipartFile file, long maxFileSize) throws IOException {
    if (file.getSize() > maxFileSize) {
      throw new IOException("File size exceeds maximum allowed size of " + (maxFileSize / BINARY_UNIT_MULTIPLIER / BINARY_UNIT_MULTIPLIER) + " MB");
    }
  }

  public static void validateMimeType(MultipartFile file, Set<String> allowedMimeTypes) throws IOException {
    String contentType = file.getContentType();
    if (contentType == null || !allowedMimeTypes.contains(contentType)) {
      throw new IOException("Invalid file type. Allowed types: " + allowedMimeTypes);
    }
  }

  public static void validateFilename(MultipartFile file) throws IOException {
    String fileName = file.getOriginalFilename();
    validateFilename(fileName);
  }

  public static void validateFilename(String fileName) throws IOException {
    if (fileName == null || fileName.isEmpty()) {
      throw new IOException("Filename is missing");
    }

    if (isTraversal(fileName)) {
      throw new IOException("Invalid filename: contains path traversal characters");
    }

    if (isAbsolutePathLike(fileName)) {
      throw new IOException("Invalid filename: contains absolute path");
    }

    if (isHiddenDotFile(fileName)) {
      throw new IOException("Invalid filename: hidden files are not allowed");
    }
  }

  private static boolean isTraversal(String name) {
    boolean containsDoubleDotSlash = name.contains(CoreCommonConstants.DOUBLE_DOT) && (name.contains(
        CoreCommonConstants.SLASH) || name.contains(CoreCommonConstants.BACKSLASH));
    boolean startsWithDoubleDot = name.startsWith(
        CoreCommonConstants.DOUBLE_DOT + CoreCommonConstants.SLASH) || name.startsWith(
        CoreCommonConstants.DOUBLE_DOT + CoreCommonConstants.BACKSLASH);
    return containsDoubleDotSlash || startsWithDoubleDot;
  }

  private static boolean isAbsolutePathLike(String name) {
    return name.startsWith(CoreCommonConstants.SLASH) || name.startsWith(CoreCommonConstants.BACKSLASH);
  }

  private static boolean isHiddenDotFile(String name) {
    String simple = name;
    if (name.contains(CoreCommonConstants.SLASH)) {
      simple = name.substring(name.lastIndexOf(CoreCommonConstants.SLASH.charAt(0)) + 1);
    } else if (name.contains(CoreCommonConstants.BACKSLASH)) {
      simple = name.substring(name.lastIndexOf(CoreCommonConstants.BACKSLASH.charAt(0)) + 1);
    }
    return simple.startsWith(CoreCommonConstants.DOT_SEPARATOR) && simple.length() > 1;
  }

  public static boolean hasAllowedExtension(String fileName, Set<String> allowedExtensions) {
    String lower = fileName.toLowerCase(Locale.ROOT);
    return allowedExtensions.stream().anyMatch(lower::endsWith);
  }

  public static boolean isArchiveFile(String fileName) {
    String lower = fileName.toLowerCase(Locale.ROOT);
    return lower.endsWith(CoreCommonConstants.ZIP_EXTENSION) || lower.endsWith(
        CoreCommonConstants.JAR_EXTENSION) || lower.endsWith(CoreCommonConstants.WAR_EXTENSION) || lower.endsWith(
        CoreCommonConstants.EAR_EXTENSION);
  }
}
