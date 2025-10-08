package com.axonivy.market.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.zip.*;

@Log4j2
public class ZipSafetyScanner {
  // Giới hạn kích thước
  public static final long maxTotalUncompressedBytes = 50L * 1024 * 1024; // 50MB
  public static final long maxSingleUncompressedBytes = 30L * 1024 * 1024; // 30MB
  public static final int maxEntries = 50;
  public static final double maxCompressionRatioPerEntry = 200.0;
  public static final double maxGlobalCompressionRatio = 100.0;
  public static final int maxNestedArchiveDepth = 3;
  public static final Set<String> dangerousExtensions = new HashSet<>(Arrays.asList(
      ".exe", ".dll", ".bat", ".cmd", ".sh", ".ps1", ".vbs", ".js", ".jar",
      ".php", ".phps", ".phtml", ".py", ".rb", ".pl", ".class", ".com"
  ));

  // Giới hạn đọc thử nội dung để kiểm nested (chỉ đọc vài byte đầu entry)
  public static int nestedHeaderProbeBytes = 8;

  /* ===================== KẾT QUẢ / ISSUE ===================== */
  public static class Issue {
    public final String code;
    public final String message;
    public final String entryName;

    public Issue(String code, String message, String entryName) {
      this.code = code;
      this.message = message;
      this.entryName = entryName;
    }

    @Override
    public String toString() {
      return "[" + code + "] " + message + (entryName != null ? " (entry=" + entryName + ")" : "");
    }
  }

  /**
   * Analyze a ZIP file without unzipping
   */
  public static boolean analyze(MultipartFile zipFile) throws IOException {
    if (zipFile == null || zipFile.isEmpty()) {
      log.error("Zip file is null or empty");
      return false;
    }

    File tempFile = File.createTempFile("upload-", ".zip");
    try (InputStream input = zipFile.getInputStream();
         OutputStream output = new FileOutputStream(tempFile)) {
      input.transferTo(output);
    }

    int entries = 0;
    long totalCompressed = 0;
    long totalUncompressed = 0;
    double maxEntryRatio = 0.0;
    int nestedArchiveCount = 0;
    boolean isValid = true;
    try (ZipFile zf = new ZipFile(tempFile)) {
      Enumeration<? extends ZipEntry> en = zf.entries();
      while (en.hasMoreElements()) {
        ZipEntry e = en.nextElement();
        entries++;

        String name = e.getName();
        long compressedSize = e.getCompressedSize();
        long uncompressedSize = e.getSize();

        // Stop if there are too much sub files in zip file
        if (entries > maxEntries) {
          log.error("Zip entry too large: {}", name);
          isValid = false;
          break;
        }
        // Purpose: Detect files inside ZIP with names like ../../etc/passwd.
        if (isTraversal(name)) {
          log.error("Entry name has traversal signal: {}", name);
          isValid = false;
          break;
        }

        // Prevent entries with paths starting from the root (e.g. /usr/bin/file or C:\Windows\...).
        // Avoid overwriting system files when extracting.
        if (isAbsolutePathLike(name)) {
          log.error("Entry name has absolutely path: {}", name);
          isValid = false;
          break;
        }

        // Purpose: Prevent files like .bashrc, .gitignore, .ssh/authorized_keys , .env
        // Protect the system from overwriting hidden configuration files when unzipping.
        if (isHiddenDotFile(name)) {
          log.error("Entry file is an anonymous file (.): {}", name);
          isValid = false;
          break;
        }

        if (hasDangerousExtension(name)) {
          log.error("Dangerous extensions detected: {}", name);
          isValid = false;
          break;
        }

        // Purpose: Prevent single files from being too large.
        if (uncompressedSize > maxSingleUncompressedBytes) {
          log.error("Uncompressed entry file {} size too large: {}", name, uncompressedSize);
          isValid = false;
          break;
        }

        // Purpose: Calculate the ratio between compressed and decompressed size.
        // Reason: If the ratio is too high (e.g. 1 byte compressed → 1GB decompressed), it could be a ZIP bomb.
        if (compressedSize > 0 && uncompressedSize > 0) {
          double ratio = (double) uncompressedSize / (double) compressedSize;
          if (ratio > maxEntryRatio) maxEntryRatio = ratio;
          if (ratio > maxCompressionRatioPerEntry) {
            log.error("The file {} has high compression ratio too large: {}", name, ratio);
            isValid = false;
            break;
          }
        }

        if (compressedSize > 0) totalCompressed += compressedSize;
        if (uncompressedSize > 0) totalUncompressed += uncompressedSize;

        // Purpose: Check the total size when extracting the entire ZIP.
        // Avoid ZIP bombs that compress many small files but have a very large total size.
        if (totalUncompressed > maxTotalUncompressedBytes) {
          log.error("The total of uncompressed bytes too large: {}", totalUncompressed);
          isValid = false;
          break;
        }

        // Purpose: Detect ZIP files that contain other ZIP files inside.
        if (looksLikeNestedArchive(name)) {
          if (isZipSignature(zf, e, nestedHeaderProbeBytes)) {
            nestedArchiveCount++;
            if (nestedArchiveCount > maxNestedArchiveDepth) {
              log.error("There are many nested file in the entry: {}", name);
              isValid = false;
              break;
            }
          }
        }
      }
    } finally {
      tempFile.deleteOnExit();
    }

    // Global compression ratio check
    if (isValid) {
      double globalRatio = (totalCompressed > 0 && totalUncompressed > 0) ?
          (double) totalUncompressed / totalCompressed : 0.0;

      if (globalRatio > maxGlobalCompressionRatio) {
        log.error("Global compression ratio too large: {}", globalRatio);
        isValid = false;
      }
    }
    return isValid;
  }

  private static boolean isTraversal(String name) {
    return name.contains(".." + "/") || name.contains("../") || name.contains("..\\") || name.startsWith("../")
        || name.startsWith("..\\");
  }

  private static boolean isAbsolutePathLike(String name) {
    return name.startsWith("/") || name.startsWith("\\");
  }

  private static boolean isHiddenDotFile(String name) {
    String simple = name.contains("/") ? name.substring(name.lastIndexOf('/') + 1) : name;
    return simple.startsWith(".") && simple.length() > 1;
  }

  private static boolean hasDangerousExtension(String name) {
    String lower = name.toLowerCase(Locale.ROOT);
    for (String ext : ZipSafetyScanner.dangerousExtensions) {
      if (lower.endsWith(ext)) return true;
    }
    return false;
  }

  private static boolean looksLikeNestedArchive(String name) {
    String lower = name.toLowerCase(Locale.ROOT);
    return lower.endsWith(".zip") || lower.endsWith(".jar") || lower.endsWith(".war") || lower.endsWith(".ear");
  }

  private static boolean isZipSignature(ZipFile zf, ZipEntry entry, int probeBytes) {
    if (entry.isDirectory()) return false;
    try (InputStream in = zf.getInputStream(entry)) {
      byte[] buf = new byte[Math.min(probeBytes, 8)];
      int read = in.read(buf);
      if (read < 4) return false;
      return buf[0] == 'P' && buf[1] == 'K'; // Đơn giản
    } catch (IOException e) {
      return false;
    }
  }
}
