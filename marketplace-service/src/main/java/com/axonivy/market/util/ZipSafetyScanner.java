package com.axonivy.market.util;

import com.axonivy.market.exceptions.model.InvalidZipEntryException;
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
  public static final int MAX_ENTRIES = 100;
  public static final double MAX_COMPRESSION_RATIO_PER_ENTRY = 200.0;
  public static final int MAX_NESTED_ARCHIVE_DEPTH = 3;
  private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
      ".exe", ".dll", ".bat", ".cmd", ".sh", ".ps1", ".vbs", ".js", ".jar",
      ".php", ".phps", ".phtml", ".py", ".rb", ".pl", ".class", ".com",
      ".msi", ".cpl", ".cab", ".msp", ".msu", ".scr", ".sys", ".drv",
      ".appx", ".xap", ".dmg", ".pkg", ".rpm", ".deb", ".bash"
  );

  /**
   * Analyze a ZIP file without unzipping
   */
  public static void analyze(MultipartFile zipFile) throws IOException {
    if (zipFile == null || zipFile.isEmpty()) {
      throw new InvalidZipEntryException("Zip file is null or empty");
    }

    File tempFile = File.createTempFile("tempUpload-", ".zip");
    try (InputStream input = zipFile.getInputStream();
         OutputStream output = new FileOutputStream(tempFile)) {
      input.transferTo(output);
    }

    try (ZipFile zf = new ZipFile(tempFile)) {
      int entries = 0;
      long totalUncompressed = 0;
      boolean hasReadme = false;

      Enumeration<? extends ZipEntry> en = zf.entries();
      while (en.hasMoreElements()) {
        ZipEntry e = en.nextElement();
        entries++;

        String name = e.getName();
        long compressedSize = e.getCompressedSize();
        long uncompressedSize = e.getSize();

        isEntryValid(name, entries);
        isEntrySizeValid(name, compressedSize, uncompressedSize, totalUncompressed);

        if (uncompressedSize > 0) {
          totalUncompressed += uncompressedSize;
        }
        // Check if README.md exists (case-insensitive)
        if ("README.md".equalsIgnoreCase(name)) {
          hasReadme = true;
        }
      }

      if (!hasReadme) {
        throw new InvalidZipEntryException("Missing required file: README.md");
      }

      if (countNestedZipsRecursive(new FileInputStream(tempFile)) >= MAX_NESTED_ARCHIVE_DEPTH) {
        var errorDetectNestedZip = String.format("Too many nested ZIP entries detected (>%d) in %s",
            MAX_NESTED_ARCHIVE_DEPTH, zipFile.getOriginalFilename());
        throw new InvalidZipEntryException(errorDetectNestedZip);
      }
    } catch (InvalidZipEntryException ex) {
      log.error("Invalid zip entry detected: {}", ex.getMessage());
      throw ex;
    } finally {
      tempFile.deleteOnExit();
    }
  }

  private static void isEntryValid(String name, int entries) {
    // Stop if there are too much sub files in zip file
    if (entries > MAX_ENTRIES) {
      throw new InvalidZipEntryException("Zip entry " + name + " is too large");
    }
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
    if (hasDangerousExtension(name)) {
      throw new InvalidZipEntryException("Entry name " + name + " is dangerous extension");
    }
  }

  private static void isEntrySizeValid(String name, long compressedSize, long uncompressedSize,
      long totalUncompressed) {
    // Purpose: Prevent single files from being too large.
    if (uncompressedSize > MAX_SINGLE_UNCOMPRESSED_BYTES) {
      throw new InvalidZipEntryException("Uncompressed entry file " + name + " exceeds 30MB in size");
    }
    // Purpose: Calculate the ratio between compressed and decompressed size.
    // Reason: If the ratio is too high (e.g. 1 byte compressed → 1GB decompressed), it could be a ZIP bomb.
    if (compressedSize > 0 && uncompressedSize > 0) {
      double ratio = (double) uncompressedSize / compressedSize;
      if (ratio > MAX_COMPRESSION_RATIO_PER_ENTRY) {
        throw new InvalidZipEntryException("File " + name +
            " has size difference between compressed and uncompressed entry files is too large");
      }
    }

    if (totalUncompressed + uncompressedSize > MAX_TOTAL_UNCOMPRESSED_BYTES) {
      throw new InvalidZipEntryException(
          "Total uncompressed bytes too large: " + (totalUncompressed + uncompressedSize));
    }
  }

  public static int countNestedZipsRecursive(InputStream inputStream) throws IOException {
    int count = 0;
    try (var zis = new ZipInputStream(inputStream)) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (entry.isDirectory() || !looksLikeNestedArchive(entry.getName())) {
          continue;
        }
        count++;
        count += countInNestedZip(zis);
      }
    }
    return count;
  }

  private static int countInNestedZip(ZipInputStream zis) throws IOException {
    var byteArrayOutputStream = new ByteArrayOutputStream();
    var buffer = new byte[FileUtils.DEFAULT_BUFFER_SIZE];
    int len;
    while ((len = zis.read(buffer)) != -1) {
      byteArrayOutputStream.write(buffer, 0, len);
    }
    return countNestedZipsRecursive(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
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
