/*
package com.axonivy.market.service.impl;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class GithubArtifactExtractImplTest {

  private final GithubArtifactExtractUtils extractor = new GithubArtifactExtractUtils();

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
  void testExtractZipToTempDirWithFileEntry() throws IOException {
    InputStream zipStream = createZipStream("file.txt");
    File tempDir = extractor.extractZipToTempDir(zipStream, "test");
    File extracted = new File(tempDir, "file.txt");

    assertTrue(extracted.exists(), "Expected extracted file to exist");
    assertEquals("content-file.txt", Files.readString(extracted.toPath()), "Extracted file content mismatch");
  }

  @Test
  void testExtractZipToTempDirWithDirectoryEntry() throws IOException {
    InputStream zipStream = createZipStream("dir/", "dir/file.txt");
    File tempDir = extractor.extractZipToTempDir(zipStream, "test");
    File dir = new File(tempDir, "dir");
    File file = new File(dir, "file.txt");

    assertTrue(dir.isDirectory(), "Expected 'dir' to be a directory");
    assertTrue(file.exists(), "Expected file inside directory to exist");
    assertEquals("content-dir/file.txt", Files.readString(file.toPath()), "Extracted file content mismatch");
  }

  @Test
  void testExtractZipCreatesEmptyTempDir() throws IOException {
    InputStream zipStream = createZipStream();
    File tempDir = extractor.extractZipToTempDir(zipStream, "empty");

    assertTrue(tempDir.exists(), "Expected temp directory to exist");
    assertTrue(tempDir.isDirectory(), "Expected temp path to be a directory");
    assertEquals(0, tempDir.listFiles().length, "Expected temp directory to be empty");
  }

  @Test
  void testExtractZipToTempDirRejectsZipSlip() throws IOException {
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
    assertTrue(ex.getMessage().contains("Failed to extract ZIP content"), "Expected UncheckedIOException for zip slip");
  }

  @Test
  void testExtractZipThrowsUncheckedIOExceptionOnReadError() {
    InputStream zipStream = new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException("Simulated IO error");
      }
    };

    UncheckedIOException ex = assertThrows(UncheckedIOException.class, () ->
        extractor.extractZipToTempDir(zipStream, "ioerror")
    );
    assertTrue(ex.getMessage().contains("Failed to extract ZIP content"), "Expected UncheckedIOException for IO error");
  }
}
*/
