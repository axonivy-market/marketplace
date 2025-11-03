package com.axonivy.market.util;

import com.axonivy.market.exceptions.model.InvalidZipEntryException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
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
  void testAnalyzeValidZip() throws IOException {
    // Include both README.md and a permitted entry
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      // Add README.md
      zos.putNextEntry(new ZipEntry("README.md"));
      zos.write("Doc".getBytes());
      zos.closeEntry();
      // Add another allowed file
      zos.putNextEntry(new ZipEntry("valid.txt"));
      zos.write("Hello World".getBytes());
      zos.closeEntry();
    }
    MultipartFile file = mockZipFile(baos.toByteArray(), false);
    // Expect no exception for a valid zip file with README.md and permitted entry
    assertDoesNotThrow(() -> ZipSafetyScanner.analyze(file),
        "Expected analyze to not throw for a valid zip file with README.md and a permitted entry.");
  }

  @Test
  void testAnalyzeNullOrEmptyZip() throws IOException {
    MultipartFile file = mockZipFile(new byte[0], true);
    assertThrows(InvalidZipEntryException.class, () -> ZipSafetyScanner.analyze(file),
        "Expected analyze to throw InvalidZipEntryException for a null or empty zip file.");
  }

  @ParameterizedTest
  @MethodSource("invalidEntryNamesProvider")
  void testAnalyzeInvalidEntry(String entryName, byte[] data) throws IOException {
    byte[] zip = createZipWithEntry(entryName, data);
    MultipartFile file = mockZipFile(zip, false);
    assertThrows(InvalidZipEntryException.class, () -> ZipSafetyScanner.analyze(file),
        "Expected analyze to throw InvalidZipEntryException for invalid entry name: " + entryName);
  }

  @Test
  void testAnalyzeEntryTooLarge() throws IOException {
    byte[] largeData = new byte[(int) (ZipSafetyScanner.MAX_SINGLE_UNCOMPRESSED_BYTES + 1)];
    byte[] zip = createZipWithEntry("big.txt", largeData);
    MultipartFile file = mockZipFile(zip, false);
    assertThrows(InvalidZipEntryException.class, () -> ZipSafetyScanner.analyze(file),
        "Expected analyze to throw InvalidZipEntryException when a single entry exceeds the maximum allowed uncompressed size.");
  }

  @Test
  void testAnalyzeTooManyEntries() throws IOException {
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
    assertThrows(InvalidZipEntryException.class, () -> ZipSafetyScanner.analyze(file),
        "Expected analyze to throw InvalidZipEntryException when the number of entries exceeds the allowed maximum.");
  }

  @Test
  void testAnalyzeMissingReadmeFile() throws IOException {
    // Create a zip file WITHOUT README.md.
    byte[] zip = createZipWithEntry("somefile.txt", "content".getBytes());
    MultipartFile file = mockZipFile(zip, false);
    assertThrows(InvalidZipEntryException.class, () -> ZipSafetyScanner.analyze(file),
        "Expected analyze to throw InvalidZipEntryException when README.md is missing.");
  }

  @Test
  void testAnalyzeHighCompressionRatioEntry() throws IOException {
    // compressed size = 1, uncompressed = 1_000_000
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      ZipEntry entry = new ZipEntry("bomb.txt");
      zos.putNextEntry(entry);
      zos.write(new byte[1_000_000]); // 1MB uncompressed
      zos.closeEntry();
    }
    byte[] zip = baos.toByteArray();
    MultipartFile file = mockZipFile(zip, false);
    assertThrows(InvalidZipEntryException.class, () -> ZipSafetyScanner.analyze(file),
        "Expected analyze to throw InvalidZipEntryException for an entry with a very high compression ratio (potential zip bomb).");
  }

  @Test
  void testAnalyzeTotalUncompressedTooLarge() throws IOException {
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
    assertThrows(InvalidZipEntryException.class, () -> ZipSafetyScanner.analyze(file),
        "Expected analyze to throw InvalidZipEntryException when total uncompressed size of all entries exceeds the allowed maximum.");
  }

  @Test
  void testDetectTooManyNestedZipFilesThrowsException() throws IOException {
    // Arrange: Create a zip file with too many nested zip files
    int overDepth = ZipSafetyScanner.MAX_NESTED_ARCHIVE_DEPTH + 1;
    byte[] zipData = createNestedZip(overDepth);

    MultipartFile multipartFile = Mockito.mock(MultipartFile.class);
    Mockito.when(multipartFile.isEmpty()).thenReturn(false);
    Mockito.when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(zipData));
    Mockito.when(multipartFile.getOriginalFilename()).thenReturn("test-file.zip");

    // Act & Assert
    InvalidZipEntryException ex = assertThrows(InvalidZipEntryException.class,
        () -> ZipSafetyScanner.analyze(multipartFile),
        "Expected analyze to throw InvalidZipEntryException for many nested zip file");

    assertTrue(ex.getMessage().contains("Too many nested ZIP entries detected (>3) in test-file.zip"),
        "Expected analyze to throw InvalidZipEntryException for many nested zip file");
  }

  // Utility to create a ZIP file in memory with a nested ZIP
  private byte[] createNestedZip(int depth) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      if (depth > 0) {
        // Recursively add a nested zip entry
        byte[] nested = createNestedZip(depth - 1);
        ZipEntry nestedEntry = new ZipEntry("nested" + depth + ".zip");
        zos.putNextEntry(nestedEntry);
        zos.write(nested);
        zos.closeEntry();
      }
      // Add a simple file at the deepest level
      ZipEntry fileEntry = new ZipEntry("README.md");
      zos.putNextEntry(fileEntry);
      zos.write("test".getBytes());
      zos.closeEntry();
    }
    return baos.toByteArray();
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