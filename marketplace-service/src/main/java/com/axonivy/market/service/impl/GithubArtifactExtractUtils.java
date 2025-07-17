package com.axonivy.market.service.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GithubArtifactExtractUtils {

  public static File extractZipToTempDir(InputStream zipStream, String name) {
    try {
      Path tempDir = createTempDirectory(name);
      extractZipEntries(zipStream, tempDir);
      return tempDir.toFile();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to extract ZIP content to temp dir", e);
    }
  }

  private static Path createTempDirectory(String name) throws IOException {
    return Files.createTempDirectory("test-reports-" + name + "-");
  }

  private static void extractZipEntries(InputStream zipStream, Path tempDir) throws IOException {
    try (var zis = new ZipInputStream(zipStream)) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        processZipEntry(entry, zis, tempDir);
      }
    }
  }

  private static void processZipEntry(ZipEntry entry, ZipInputStream zis, Path tempDir) throws IOException {
    var entryPath = resolveEntryPath(entry, tempDir);
    if (entry.isDirectory()) {
      Files.createDirectories(entryPath);
    } else {
      writeZipEntryContent(entryPath, zis);
    }
  }

  private static Path resolveEntryPath(ZipEntry entry, Path tempDir) {
    var entryPath = tempDir.resolve(entry.getName()).normalize();
    if (entryPath.startsWith(tempDir)) {
      return entryPath;
    }
    return null;
  }

  private static void writeZipEntryContent(Path entryPath, ZipInputStream zis) throws IOException {
    Files.createDirectories(entryPath.getParent());
    try (var os = Files.newOutputStream(entryPath)) {
      zis.transferTo(os);
    }
  }
}