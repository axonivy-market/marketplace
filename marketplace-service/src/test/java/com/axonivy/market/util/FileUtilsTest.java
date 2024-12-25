package com.axonivy.market.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FileUtilsTest {

  private static final String FILE_PATH = "src/test/resources/test-file.xml";

  @Test
  void testCreateFile() throws IOException {
    File createdFile = FileUtils.createFile(FILE_PATH);
    assertTrue(createdFile.exists(), "File should exist");
    assertTrue(createdFile.isFile(), "Should be a file");
    createdFile.delete();
  }

  @Test
  void testFailedToCreateDirectory() {
    File createdFile = new File("testDirAsFile");
    try {
      if (!createdFile.exists()) {
        assertTrue(createdFile.createNewFile(), "Setup failed: could not create file");
      }

      IOException exception = assertThrows(IOException.class, () ->
          FileUtils.createFile("testDirAsFile/subDir/testFile.txt")
      );
      assertTrue(exception.getMessage().contains("Failed to create directory"),
          "Exception message does not contain expected text");
    } catch (IOException e) {
      fail("Setup failed: " + e.getMessage());
    } finally {
      createdFile.delete();
    }
  }

  @Test
  void testWriteFile() throws IOException {
    File createdFile = FileUtils.createFile(FILE_PATH);
    String content = "Hello, world!";
    FileUtils.writeToFile(createdFile, content);
    String fileContent = Files.readString(createdFile.toPath());
    assertEquals(content, fileContent, "File content should match the written content");
    createdFile.delete();

  }

  @Test
  void testPrepareUnZipDirectory() throws IOException {
    Path tempDirectory = Files.createTempDirectory("test-dir");
    FileUtils.prepareUnZipDirectory(tempDirectory);
    assertTrue(Files.exists(tempDirectory));
    FileUtils.clearDirectory(tempDirectory);
  }

  @Test
  void testPrepareUnZipDirectory_IOException() throws IOException {
    Path tempDirectory = Files.createTempDirectory("test-dir");
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.exists(tempDirectory)).thenReturn(true);
    }
    assertDoesNotThrow(() -> FileUtils.prepareUnZipDirectory(tempDirectory));
    FileUtils.clearDirectory(tempDirectory);
  }

  @Test
  void testUnzip_Success() throws Exception {
    Path tempDirectory = Files.createTempDirectory("test-dir");
    String mockFileName = "test.zip";
    byte[] zipContent = createMockZipContent();
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", mockFileName,
        "application/zip", zipContent);
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.exists(tempDirectory)).thenReturn(false);
      mockedFiles.when(() -> Files.createDirectories(tempDirectory)).thenReturn(null);
      FileUtils.unzip(mockMultipartFile, String.valueOf(tempDirectory));
      mockedFiles.verify(() -> Files.exists(tempDirectory), times(1));
      mockedFiles.verify(() -> Files.createDirectories(tempDirectory), times(1));
    }
    FileUtils.clearDirectory(tempDirectory);
  }

  private byte[] createMockZipContent() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ZipOutputStream zos = new ZipOutputStream(baos)) {
      ZipEntry entry = new ZipEntry("mockFile.txt");
      zos.putNextEntry(entry);
      zos.write("Mock file content".getBytes());
      zos.closeEntry();
    }
    return baos.toByteArray();
  }

}
