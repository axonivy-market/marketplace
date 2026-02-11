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
  private static final Set<String> WHITELIST_EXTENSIONS = Set.of(".xml", ".svg", "png", "jpg",
      "jpeg", ".gif", ".json", ".md", ".zip");

  /**
   * Analyze a ZIP file without unzipping
   */
  public static void analyze(MultipartFile file) throws IOException {
    FileValidator.validateFileNotEmpty(file);

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

        if (!zipEntry.isDirectory() && isArchiveFile(zipEntry.getName())) {
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

  private static void isEntryValid(boolean isDirectory, String name) {
    // Purpose: Detect files inside ZIP with names like ../../etc/passwd.
    // Prevent entries with paths starting from the root (e.g. /usr/bin/file or C:\Windows\...).
    // Purpose: Prevent files like .bashrc, .gitignore, .ssh/authorized_keys , .env
    try {
      FileValidator.validateFilename(name);
    } catch (IOException e) {
      throw new InvalidZipEntryException(e.getMessage());
    }

    if (!isDirectory && isNotInWhiteListExtensions(name)) {
      throw new InvalidZipEntryException("Entry name " + name + " is not supported extension");
    }
  }

  private static boolean isNotInWhiteListExtensions(String name) {
    return !FileValidator.hasAllowedExtension(name, WHITELIST_EXTENSIONS);
  }

  private static boolean isArchiveFile(String name) {
    return FileValidator.isArchiveFile(name);
  }
}
