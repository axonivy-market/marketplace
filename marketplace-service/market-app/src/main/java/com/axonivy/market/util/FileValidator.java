package com.axonivy.market.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * General file validator for multipart file uploads with security checks
 */
@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileValidator {

  private static final long DEFAULT_MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
  private static final Set<String> DEFAULT_ALLOWED_MIME_TYPES = new HashSet<>(Set.of(
      "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
  ));

  /**
   * Validate image file with default constraints
   */
  public static void validateImageFile(MultipartFile file) throws IOException {
    validateImageFile(file, DEFAULT_MAX_FILE_SIZE, DEFAULT_ALLOWED_MIME_TYPES);
  }

  /**
   * Validate image file with custom max size
   */
  public static void validateImageFile(MultipartFile file, long maxFileSize) throws IOException {
    validateImageFile(file, maxFileSize, DEFAULT_ALLOWED_MIME_TYPES);
  }

  /**
   * Validate image file with custom constraints
   */
  public static void validateImageFile(MultipartFile file, long maxFileSize, Set<String> allowedMimeTypes) throws IOException {
    validateFileNotEmpty(file);
    validateFileSize(file, maxFileSize);
    validateMimeType(file, allowedMimeTypes);
    validateFilename(file);
    log.info("File validation passed for: {}", file.getOriginalFilename());
  }

  /**
   * Validate that file is not null or empty
   */
  public static void validateFileNotEmpty(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new IOException("File is empty or null");
    }
  }

  /**
   * Validate file size does not exceed maximum
   */
  public static void validateFileSize(MultipartFile file, long maxFileSize) throws IOException {
    if (file.getSize() > maxFileSize) {
      throw new IOException("File size exceeds maximum allowed size of " + (maxFileSize / 1024 / 1024) + " MB");
    }
  }

  /**
   * Validate MIME type is in whitelist
   */
  public static void validateMimeType(MultipartFile file, Set<String> allowedMimeTypes) throws IOException {
    String contentType = file.getContentType();
    if (contentType == null || !allowedMimeTypes.contains(contentType)) {
      throw new IOException("Invalid file type. Allowed types: " + allowedMimeTypes);
    }
  }

  /**
   * Validate filename to prevent path traversal and other attacks
   */
  public static void validateFilename(MultipartFile file) throws IOException {
    String fileName = file.getOriginalFilename();
    validateFilename(fileName);
  }

  /**
   * Validate filename string to prevent path traversal and other attacks
   */
  public static void validateFilename(String fileName) throws IOException {
    if (fileName == null || fileName.isEmpty()) {
      throw new IOException("Filename is missing");
    }

    // Check for path traversal characters
    if (isTraversal(fileName)) {
      throw new IOException("Invalid filename: contains path traversal characters");
    }

    // Check for absolute paths
    if (isAbsolutePathLike(fileName)) {
      throw new IOException("Invalid filename: contains absolute path");
    }

    // Check for hidden files
    if (isHiddenDotFile(fileName)) {
      throw new IOException("Invalid filename: hidden files are not allowed");
    }
  }

  /**
   * Check for path traversal attempts
   */
  private static boolean isTraversal(String name) {
    boolean containsDoubleDotSlash = name.contains("..") && (name.contains("/") || name.contains("\\"));
    boolean startsWithDoubleDot = name.startsWith("../") || name.startsWith("..\\");
    return containsDoubleDotSlash || startsWithDoubleDot;
  }

  /**
   * Check for absolute path patterns
   */
  private static boolean isAbsolutePathLike(String name) {
    return name.startsWith("/") || name.startsWith("\\");
  }

  /**
   * Check for hidden dot files
   */
  private static boolean isHiddenDotFile(String name) {
    String simple = name;
    if (name.contains("/")) {
      simple = name.substring(name.lastIndexOf('/') + 1);
    } else if (name.contains("\\")) {
      simple = name.substring(name.lastIndexOf('\\') + 1);
    }
    return simple.startsWith(".") && simple.length() > 1;
  }

  /**
   * Check if filename has allowed extension
   */
  public static boolean hasAllowedExtension(String fileName, Set<String> allowedExtensions) {
    String lower = fileName.toLowerCase(Locale.ROOT);
    return allowedExtensions.stream().anyMatch(lower::endsWith);
  }

  /**
   * Check if filename looks like a nested archive
   */
  public static boolean isArchiveFile(String fileName) {
    String lower = fileName.toLowerCase(Locale.ROOT);
    return lower.endsWith(".zip") || lower.endsWith(".jar") || lower.endsWith(".war") || lower.endsWith(".ear");
  }
}
