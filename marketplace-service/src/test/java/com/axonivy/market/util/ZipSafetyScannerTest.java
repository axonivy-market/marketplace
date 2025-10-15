package com.axonivy.market.util;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.stream.Stream;
import java.util.zip.*;

import static org.junit.jupiter.api.Assertions.*;

class ZipSafetyScannerTest {

  static Stream<Arguments> invalidEntryNamesProvider() {
    return Stream.of(
        Arguments.of("../etc/passwd", "bad".getBytes()),
        Arguments.of("/root.txt", "bad".getBytes()),
        Arguments.of(".env", "bad".getBytes()),
        Arguments.of("virus.exe", "bad".getBytes())
    );
  }

  @Test
  void testAnalyze_validZip() throws IOException {
    byte[] zip = createZipWithEntry("valid.txt", "Hello World".getBytes());
    MultipartFile file = mockZipFile(zip, false);
    assertTrue(ZipSafetyScanner.analyze(file));
  }

  @Test
  void testAnalyze_nullOrEmptyZip() throws IOException {
    MultipartFile file = mockZipFile(new byte[0], true);
    assertFalse(ZipSafetyScanner.analyze(file));
  }

  @ParameterizedTest
  @MethodSource("invalidEntryNamesProvider")
  void testAnalyze_invalidEntry(String entryName, byte[] data) throws IOException {
    byte[] zip = createZipWithEntry(entryName, data);
    MultipartFile file = mockZipFile(zip, false);
    assertFalse(ZipSafetyScanner.analyze(file));
  }

  @Test
  void testAnalyze_entryTooLarge() throws IOException {
    byte[] largeData = new byte[(int) (ZipSafetyScanner.MAX_SINGLE_UNCOMPRESSED_BYTES + 1)];
    byte[] zip = createZipWithEntry("big.txt", largeData);
    MultipartFile file = mockZipFile(zip, false);
    assertFalse(ZipSafetyScanner.analyze(file));
  }

  @Test
  void testAnalyze_tooManyEntries() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      for (int i = 0; i < ZipSafetyScanner.MAX_ENTRIES + 1; i++) {
        ZipEntry entry = new ZipEntry("file" + i + ".txt");
        zos.putNextEntry(entry);
        zos.write(("file" + i).getBytes());
        zos.closeEntry();
      }
    }
    MultipartFile file = mockZipFile(baos.toByteArray(), false);
    assertFalse(ZipSafetyScanner.analyze(file));
  }

  @Test
  void testAnalyze_highCompressionRatioEntry() throws IOException {
    // compressed size = 1, uncompressed = 1000000
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      ZipEntry entry = new ZipEntry("bomb.txt");
      zos.putNextEntry(entry);
      zos.write(new byte[1000000]); // 1MB uncompressed
      zos.closeEntry();
    }
    byte[] zip = baos.toByteArray();
    MultipartFile file = mockZipFile(zip, false);
    assertFalse(ZipSafetyScanner.analyze(file));
  }

  @Test
  void testAnalyze_totalUncompressedTooLarge() throws IOException {
    // Add multiple entries to go over total uncompressed limit
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      for (int i = 0; i < 2; i++) {
        ZipEntry entry = new ZipEntry("large" + i + ".txt");
        zos.putNextEntry(entry);
        zos.write(new byte[(int) (ZipSafetyScanner.MAX_TOTAL_UNCOMPRESSED_BYTES / 2 + 1)]);
        zos.closeEntry();
      }
    }
    MultipartFile file = mockZipFile(baos.toByteArray(), false);
    assertFalse(ZipSafetyScanner.analyze(file));
  }

  @Test
  void testAnalyze_globalCompressionRatioTooHigh() throws IOException {
    // Compress a lot of repetitive data (high global ratio)
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      byte[] data = new byte[1000000]; // 1MB uncompressed, will compress well
      ZipEntry entry = new ZipEntry("bomb.txt");
      zos.putNextEntry(entry);
      zos.write(data);
      zos.closeEntry();
    }
    MultipartFile file = mockZipFile(baos.toByteArray(), false);
    // This may or may not fail depending on compression, but if ratio is high, should fail
    boolean result = ZipSafetyScanner.analyze(file);
    // Assert that the analysis fails due to high global compression ratio
    assertFalse(result, "Expected analyze to return false for a zip bomb with high global compression ratio");
  }

  @Test
  void testDetectNestedZipFiles() throws IOException {
    // Create the innermost zip (level 3)
    byte[] level3ZipBytes = createZipWithEntry("file3.txt", "level3".getBytes());

    // Level 2: zip containing level3 zip
    byte[] level2ZipBytes;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ZipOutputStream zos = new ZipOutputStream(baos)) {
      ZipEntry entry = new ZipEntry("level3.zip");
      zos.putNextEntry(entry);
      zos.write(level3ZipBytes);
      zos.closeEntry();
      level2ZipBytes = baos.toByteArray();
    }

    // Level 1: zip containing level2 zip
    byte[] level1ZipBytes;
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ZipOutputStream zos = new ZipOutputStream(baos)) {
      ZipEntry entry = new ZipEntry("level2.zip");
      zos.putNextEntry(entry);
      zos.write(level2ZipBytes);
      zos.closeEntry();
      level1ZipBytes = baos.toByteArray();
    }

    // Outer zip (top-level) containing level1 zip
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      ZipEntry entry = new ZipEntry("level1.zip");
      zos.putNextEntry(entry);
      zos.write(level1ZipBytes);
      zos.closeEntry();
    }
    byte[] zipBytes = baos.toByteArray();

    // Write to temp file and check
    File tempZip = File.createTempFile("testNested-", ".zip");
    Files.write(tempZip.toPath(), zipBytes);

    try (ZipFile zipFile = new ZipFile(tempZip)) {
      // Should be false because there are 3 nested zips (MAX_NESTED_ARCHIVE_DEPTH == 3)
      assertTrue(ZipSafetyScanner.detectNestedZipFiles(zipFile));
    } finally {
      tempZip.deleteOnExit();
    }
  }

  private MultipartFile mockZipFile(byte[] zipBytes, boolean empty) throws IOException {
    MultipartFile file = Mockito.mock(MultipartFile.class);
    Mockito.when(file.isEmpty()).thenReturn(empty);
    Mockito.when(file.getInputStream()).thenReturn(new ByteArrayInputStream(zipBytes));
    Mockito.when(file.getOriginalFilename()).thenReturn("test.zip");
    return file;
  }

  private byte[] createZipWithEntry(String entryName, byte[] data) throws IOException {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
      ZipEntry entry = new ZipEntry(entryName);
      zos.putNextEntry(entry);
      zos.write(data);
      zos.closeEntry();
    }
    return outputStream.toByteArray();
  }
}