package com.axonivy.market.util;

import com.axonivy.market.exceptions.model.InvalidZipEntryException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.*;

import static com.axonivy.market.constants.CommonConstants.ZIP_EXTENSION;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZipSafetyScanner {
  // Limit the size for uncompressed bytes
  public static final long MAX_TOTAL_UNCOMPRESSED_BYTES = 50L * 1024 * 1024; // 50MB
  public static final long MAX_SINGLE_UNCOMPRESSED_BYTES = 30L * 1024 * 1024; // 30MB
  public static final int MAX_ENTRIES = 100;
  public static final double MAX_COMPRESSION_RATIO_PER_ENTRY = 200.0;
  public static final int MAX_NESTED_ARCHIVE_DEPTH = 3;
  private static final Set<String> WHITELIST_EXTENSIONS = Set.of(".xml", ".svg", "png", "jpg",
      "jpeg", ".gif", ".json", ".md", ".zip");

  /**
   * Analyze a ZIP file without unzipping
   */
  public static void analyze(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new InvalidZipEntryException("Zip file is null or empty");
    }

    var tempFile = new File("tempUpload" + ZIP_EXTENSION);
    try (var input = file.getInputStream();
         OutputStream output = new FileOutputStream(tempFile)) {
      input.transferTo(output);
    }

    try (var zipFile = new ZipFile(tempFile)) {
      var totalSizeArchive = 0;
      var totalEntryArchive = 0;
      boolean hasReadme = false;
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry zipEntry = entries.nextElement();
        InputStream in = new BufferedInputStream(zipFile.getInputStream(zipEntry));
        isEntryValid(zipEntry.isDirectory(), zipEntry.getName());
        totalEntryArchive++;

        int nBytes;
        var buffer = new byte[FileUtils.DEFAULT_BUFFER_SIZE];
        var totalSizeEntry = 0;
        while ((nBytes = in.read(buffer)) > 0) {
          totalSizeEntry += nBytes;
          totalSizeArchive += nBytes;
          isEntrySizeValid(totalSizeEntry, zipEntry, totalSizeArchive, totalEntryArchive);
        }

        // Check if README.md exists (case-insensitive)
        if ("README.md".equalsIgnoreCase(zipEntry.getName())) {
          hasReadme = true;
        }

        if (!zipEntry.isDirectory() && looksLikeNestedArchive(zipEntry.getName())) {
          throw new InvalidZipEntryException("There is nested ZIP entry " + zipEntry.getName() + " detected");
        }
      }

      if (!hasReadme) {
        throw new InvalidZipEntryException("Missing required file: README.md");
      }

    } catch (InvalidZipEntryException ex) {
      log.error("Invalid zip entry detected: {}", ex.getMessage());
      throw ex;
    } finally {
      Files.deleteIfExists(tempFile.toPath());
    }
  }

  private static void isEntrySizeValid(double totalSizeEntry, ZipEntry ze, int totalSizeArchive,
      int totalEntryArchive) {
    double compressionRatio = totalSizeEntry / ze.getCompressedSize();
    if (compressionRatio > MAX_COMPRESSION_RATIO_PER_ENTRY) {
      throw new InvalidZipEntryException("File " + ze.getName() +
          " has size difference between compressed and uncompressed entry files is too large");
    }

    if (totalSizeArchive > MAX_TOTAL_UNCOMPRESSED_BYTES) {
      throw new InvalidZipEntryException(
          "Total uncompressed bytes too large: " + totalSizeArchive);
    }

    if (totalEntryArchive > MAX_ENTRIES) {
      throw new InvalidZipEntryException("Zip entry " + ze.getName() + " is too large");
    }
  }

  private static void isEntryValid(boolean isDirectory , String name) {
    // Purpose: Detect files inside ZIP with names like ../../etc/passwd.
    if (isTraversal(name)) {
      throw new InvalidZipEntryException("Entry " + name + " has traversal");
    }
    // Prevent entries with paths starting from the root (e.g. /usr/bin/file or C:\Windows\...).
    // Avoid overwriting system files when extracting.
    if (isAbsolutePathLike(name)) {
      throw new InvalidZipEntryException("Entry name " + name + " has absolute path");
    }
    // Purpose: Prevent files like .bashrc, .gitignore, .ssh/authorized_keys , .env
    // Protect the system from overwriting hidden configuration files when unzipping.
    if (isHiddenDotFile(name)) {
      throw new InvalidZipEntryException("Entry file " + name + " is hidden");
    }

    if (!isDirectory && hasDangerousExtension(name)) {
      throw new InvalidZipEntryException("Entry name " + name + " is dangerous extension");
    }
  }

  private static boolean isTraversal(String name) {
    boolean containsDoubleDotSlash = name.contains(".." + "/") || name.contains("../") || name.contains("..\\");
    boolean startsWithDoubleDot = name.startsWith("../") || name.startsWith("..\\");
    return containsDoubleDotSlash || startsWithDoubleDot;
  }

  private static boolean isAbsolutePathLike(String name) {
    return name.startsWith("/") || name.startsWith("\\");
  }

  private static boolean isHiddenDotFile(String name) {
    String simple = name;
    if (name.contains("/")) {
      simple = name.substring(name.lastIndexOf('/') + 1);
    }
    return simple.startsWith(".") && simple.length() > 1;
  }

  private static boolean hasDangerousExtension(String name) {
    String lower = name.toLowerCase(Locale.ROOT);
    return WHITELIST_EXTENSIONS.stream().noneMatch(lower::endsWith);
  }

  private static boolean looksLikeNestedArchive(String name) {
    String lower = name.toLowerCase(Locale.ROOT);
    return lower.endsWith(".zip") || lower.endsWith(".jar") || lower.endsWith(".war") || lower.endsWith(".ear");
  }
}
