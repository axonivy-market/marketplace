package com.axonivy.market.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.zip.*;

@Log4j2
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZipSafetyScanner {

  // Limit the size for uncompressed bytes
  public static final long MAX_TOTAL_UNCOMPRESSED_BYTES = 50L * 1024 * 1024; // 50MB
  public static final long MAX_SINGLE_UNCOMPRESSED_BYTES = 30L * 1024 * 1024; // 30MB
  public static final int MAX_ENTRIES = 50;
  public static final double MAX_COMPRESSION_RATIO_PER_ENTRY = 200.0;
  public static final double MAX_GLOBAL_COMPRESSION_RATIO = 100.0;
  public static final int MAX_NESTED_ARCHIVE_DEPTH = 3;
  private static final Set<String> DANGEROUS_EXTENSIONS = new HashSet<>(Arrays.asList(
      ".exe", ".dll", ".bat", ".cmd", ".sh", ".ps1", ".vbs", ".js", ".jar",
      ".php", ".phps", ".phtml", ".py", ".rb", ".pl", ".class", ".com"
  ));

  /**
   * Analyze a ZIP file without unzipping
   */
  public static boolean analyze(MultipartFile zipFile) throws IOException {
    if (zipFile == null || zipFile.isEmpty()) {
      log.error("Zip file is null or empty");
      return false;
    }

    File tempFile = File.createTempFile("tempUpload-", ".zip");
    try (InputStream input = zipFile.getInputStream();
         OutputStream output = new FileOutputStream(tempFile)) {
      input.transferTo(output);
    }

    try (ZipFile zf = new ZipFile(tempFile)) {
      int entries = 0;
      long totalCompressed = 0;
      long totalUncompressed = 0;
      double maxEntryRatio = 0.0;

      Enumeration<? extends ZipEntry> en = zf.entries();
      while (en.hasMoreElements()) {
        ZipEntry e = en.nextElement();
        entries++;

        String name = e.getName();
        long compressedSize = e.getCompressedSize();
        long uncompressedSize = e.getSize();

        if (!isEntryValid(name, compressedSize, uncompressedSize, entries, totalUncompressed)) {
          return false;
        }

        if (compressedSize > 0) {
          totalCompressed += compressedSize;
        }

        if (uncompressedSize > 0) {
          totalUncompressed += uncompressedSize;
        }

        if (compressedSize > 0 && uncompressedSize > 0) {
          double ratio = (double) uncompressedSize / compressedSize;
          maxEntryRatio = Math.max(maxEntryRatio, ratio);
        }
      }
      return !detectNestedZipFiles(zf) && isGlobalRatioValid(totalCompressed, totalUncompressed);
    } finally {
      tempFile.deleteOnExit();
    }
  }

  private static boolean isEntryValid(String name, long compressedSize, long uncompressedSize,
      int entries, long totalUncompressed) {
    // Stop if there are too much sub files in zip file
    if (entries > MAX_ENTRIES) {
      log.error("Zip entry too large: {}", name);
      return false;
    }
    // Purpose: Detect files inside ZIP with names like ../../etc/passwd.
    if (isTraversal(name)) {
      log.error("Entry name has traversal signal: {}", name);
      return false;
    }
    // Prevent entries with paths starting from the root (e.g. /usr/bin/file or C:\Windows\...).
    // Avoid overwriting system files when extracting.
    if (isAbsolutePathLike(name)) {
      log.error("Entry name has absolutely path: {}", name);
      return false;
    }
    // Purpose: Prevent files like .bashrc, .gitignore, .ssh/authorized_keys , .env
    // Protect the system from overwriting hidden configuration files when unzipping.
    if (isHiddenDotFile(name)) {
      log.error("Entry file is an anonymous file (.): {}", name);
      return false;
    }
    if (hasDangerousExtension(name)) {
      log.error("Dangerous extensions detected: {}", name);
      return false;
    }
    // Purpose: Prevent single files from being too large.
    if (uncompressedSize > MAX_SINGLE_UNCOMPRESSED_BYTES) {
      log.error("Uncompressed entry file {} size too large: {}", name, uncompressedSize);
      return false;
    }
    // Purpose: Calculate the ratio between compressed and decompressed size.
    // Reason: If the ratio is too high (e.g. 1 byte compressed → 1GB decompressed), it could be a ZIP bomb.
    if (compressedSize > 0 && uncompressedSize > 0) {
      double ratio = (double) uncompressedSize / compressedSize;
      if (ratio > MAX_COMPRESSION_RATIO_PER_ENTRY) {
        log.error("The file {} has high compression ratio too large: {}", name, ratio);
        return false;
      }
    }

    if (totalUncompressed + uncompressedSize > MAX_TOTAL_UNCOMPRESSED_BYTES) {
      log.error("The total of uncompressed bytes too large: {}", totalUncompressed + uncompressedSize);
      return false;
    }
    return true;
  }

  private static boolean isGlobalRatioValid(long totalCompressed, long totalUncompressed) {
    if (totalCompressed > 0 && totalUncompressed > 0) {
      double ratio = (double) totalUncompressed / totalCompressed;
      if (ratio > MAX_GLOBAL_COMPRESSION_RATIO) {
        log.error("Global compression ratio too large: {}", ratio);
        return false;
      }
    }
    return true;
  }

  public static boolean detectNestedZipFiles(ZipFile zipFile) throws IOException {
    Enumeration<? extends ZipEntry> entries = zipFile.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      // skip directories
      if (entry.isDirectory()) {
        continue;
      }

      if (hasNestedZip(zipFile)) {
        return true;
      }
    }
    return false; //false
  }

  private static boolean hasNestedZip(ZipFile zipFile) throws IOException {
    int nestedArchiveCount = 0;
    Enumeration<? extends ZipEntry> entries = zipFile.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      if (entry.isDirectory()) {
        continue;
      }
      String name = entry.getName();
      if (looksLikeNestedArchive(name)) {
        nestedArchiveCount++;
        // Read this entry as a ZIP and process recursively
        try (InputStream is = zipFile.getInputStream(entry)) {
          byte[] data = is.readAllBytes();
          try (ZipInputStream nestedZip = new ZipInputStream(new ByteArrayInputStream(data))) {
            if (hasNestedZip(nestedZip, nestedArchiveCount)) {
              log.error("Too many nested ZIP entries detected (>{}): {}", MAX_NESTED_ARCHIVE_DEPTH, name);
              return true;
            }
          } catch (IOException e) {
            // Not a valid zip, skip
            log.error("It is not a ZIP entry: {}", name, e);
          }
        }
      }
    }
    return false;
  }

  private static boolean hasNestedZip(ZipInputStream zipStream, int nestedArchiveCount) throws IOException {
    ZipEntry entry;
    while ((entry = zipStream.getNextEntry()) != null) {
      if (entry.isDirectory()) {
        continue;
      }
      String name = entry.getName();
      if (looksLikeNestedArchive(name)) {
        nestedArchiveCount++;
        byte[] data = zipStream.readAllBytes(); // Read this entry's bytes
        try (ZipInputStream nestedZip = new ZipInputStream(new ByteArrayInputStream(data))) {
          if (hasNestedZip(nestedZip,nestedArchiveCount)) {
            return true;
          }
        } catch (IOException e) {
          // Not a valid zip, skip
          log.error("It is not a ZIP entry: {}", name, e);
        }
      }
      zipStream.closeEntry();
    }
    return nestedArchiveCount >= MAX_NESTED_ARCHIVE_DEPTH;
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
    String simple;
    if (name.contains("/")) {
      simple = name.substring(name.lastIndexOf('/') + 1);
    } else {
      simple = name;
    }
    return simple.startsWith(".") && simple.length() > 1;
  }

  private static boolean hasDangerousExtension(String name) {
    String lower = name.toLowerCase(Locale.ROOT);
    for (String ext : ZipSafetyScanner.DANGEROUS_EXTENSIONS) {
      if (lower.endsWith(ext)) {
        return true;
      }
    }
    return false;
  }

  private static boolean looksLikeNestedArchive(String name) {
    String lower = name.toLowerCase(Locale.ROOT);
    return lower.endsWith(".zip") || lower.endsWith(".jar") || lower.endsWith(".war") || lower.endsWith(".ear");
  }
}
