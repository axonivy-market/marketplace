package com.axonivy.market.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class GithubArtifactExtractImplTest {

  private final GithubArtifactExtractImpl extractor = new GithubArtifactExtractImpl();

  private InputStream createZipStream(String... entries) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      for (String entry : entries) {
        ZipEntry zipEntry = new ZipEntry(entry);
        zos.putNextEntry(zipEntry);
        if (!entry.endsWith("/")) {
          zos.write(("content-" + entry).getBytes());
        }
        zos.closeEntry();
      }
    }
    return new ByteArrayInputStream(baos.toByteArray());
  }

  @Test
  void extractZipToTempDirFileEntryExtractsFile() throws IOException {
    InputStream zipStream = createZipStream("file.txt");
    File tempDir = extractor.extractZipToTempDir(zipStream, "test");
    File extracted = new File(tempDir, "file.txt");
    assertTrue(extracted.exists());
    assertEquals("content-file.txt", Files.readString(extracted.toPath()));
  }

  @Test
  void extractZipToTempDir_DirectoryEntry_ExtractsDirectory() throws IOException {
    InputStream zipStream = createZipStream("dir/", "dir/file.txt");
    File tempDir = extractor.extractZipToTempDir(zipStream, "test");
    File dir = new File(tempDir, "dir");
    File file = new File(dir, "file.txt");
    assertTrue(dir.isDirectory());
    assertTrue(file.exists());
    assertEquals("content-dir/file.txt", Files.readString(file.toPath()));
  }

  @Test
  void extractZipToTempDirEmptyZipCreatesTempDir() throws IOException {
    InputStream zipStream = createZipStream();
    File tempDir = extractor.extractZipToTempDir(zipStream, "empty");
    assertTrue(tempDir.exists());
    assertTrue(tempDir.isDirectory());
    assertEquals(0, tempDir.listFiles().length);
  }

  @Test
  void extractZipToTempDirBadZipEntryThrowsUncheckedIOException() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      ZipEntry zipEntry = new ZipEntry("../evil.txt");
      zos.putNextEntry(zipEntry);
      zos.write("bad".getBytes());
      zos.closeEntry();
    }
    InputStream zipStream = new ByteArrayInputStream(baos.toByteArray());
    UncheckedIOException ex = assertThrows(UncheckedIOException.class, () ->
        extractor.extractZipToTempDir(zipStream, "bad")
    );
    assertTrue(ex.getMessage().contains("Failed to extract ZIP content"));
  }

  @Test
  void extractZipToTempDirIOExceptionDuringExtractionThrowsUncheckedIOException() {
    InputStream zipStream = new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException("Simulated IO error");
      }
    };
    UncheckedIOException ex = assertThrows(UncheckedIOException.class, () ->
        extractor.extractZipToTempDir(zipStream, "ioerror")
    );
    assertTrue(ex.getMessage().contains("Failed to extract ZIP content"));
  }
}