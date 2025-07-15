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
      Path tempDir = Files.createTempDirectory("test-reports-" + name + "-");

      try (ZipInputStream zis = new ZipInputStream(zipStream)) {
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
          Path entryPath = tempDir.resolve(entry.getName()).normalize();

          if (!entryPath.startsWith(tempDir)) {
            throw new IOException("Bad zip entry: " + entry.getName());
          }

          if (entry.isDirectory()) {
            Files.createDirectories(entryPath);
          } else {
            Files.createDirectories(entryPath.getParent());
            try (OutputStream os = Files.newOutputStream(entryPath)) {
              zis.transferTo(os);
            }
          }
        }
      }
      return tempDir.toFile();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to extract ZIP content to temp dir", e);
    }
  }

}