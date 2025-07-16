package com.axonivy.market.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubArtifactExtractImpl {

  public File extractZipToTempDir(InputStream zipStream, String name) {
    try {
      Path tempDir = createTempDirectory(name);
      extractZipEntries(zipStream, tempDir);
      return tempDir.toFile();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to extract ZIP content to temp dir", e);
    }
  }

  private Path createTempDirectory(String name) throws IOException {
    return Files.createTempDirectory("test-reports-" + name + "-");
  }

  private void extractZipEntries(InputStream zipStream, Path tempDir) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(zipStream)) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        processZipEntry(entry, zis, tempDir);
      }
    }
  }

  private void processZipEntry(ZipEntry entry, ZipInputStream zis, Path tempDir) throws IOException {
    Path entryPath = resolveEntryPath(entry, tempDir);
    if (entry.isDirectory()) {
      Files.createDirectories(entryPath);
    } else {
      writeZipEntryContent(entryPath, zis);
    }
  }

  private Path resolveEntryPath(ZipEntry entry, Path tempDir) throws IOException {
    Path entryPath = tempDir.resolve(entry.getName()).normalize();
    if (!entryPath.startsWith(tempDir)) {
      throw new IOException("Bad zip entry: " + entry.getName());
    }
    return entryPath;
  }

  private void writeZipEntryContent(Path entryPath, ZipInputStream zis) throws IOException {
    Files.createDirectories(entryPath.getParent());
    try (OutputStream os = Files.newOutputStream(entryPath)) {
      zis.transferTo(os);
    }
  }
}