package com.axonivy.market.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FileUtilsTest {

  private static final String FILE_PATH = "src/test/resources/test-file.xml";
  private static final String TEST_DIR = "testDir";
  private static final String SAMPLE_DOWNLOAD_URL_1 = "https://example.com/file1.txt";
  private static final String SAMPLE_DOWNLOAD_URL_2 = "https://example.com/file2.txt";
  private static final String DEPLOY_OPTION_YAML_FILE_NAME = "deploy.options.yaml";

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
    Path tempDirectory = Files.createTempDirectory(TEST_DIR);
    FileUtils.prepareUnZipDirectory(tempDirectory);
    assertTrue(Files.exists(tempDirectory));
    FileUtils.clearDirectory(tempDirectory);
  }

  @Test
  void testPrepareUnZipDirectoryWithException() throws IOException {
    Path tempDirectory = Files.createTempDirectory(TEST_DIR);
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.exists(tempDirectory)).thenReturn(true);
    }
    assertDoesNotThrow(() -> FileUtils.prepareUnZipDirectory(tempDirectory));
    FileUtils.clearDirectory(tempDirectory);
  }

  @Test
  void testUnzip() throws Exception {
    Path tempDirectory = Files.createTempDirectory(TEST_DIR);
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

  @Test
  void testBuildArtifactStreamFromArtifactUrls() throws Exception {
    // Given
    String content1 = "Content of file 1";
    String content2 = "Content of file 2";

    Resource resource1 = new ByteArrayResource(content1.getBytes());
    Resource resource2 = new ByteArrayResource(content2.getBytes());

    try (MockedStatic<HttpFetchingUtils> utilities = mockStatic(HttpFetchingUtils.class)) {
      utilities.when(() -> HttpFetchingUtils.fetchResourceUrl(SAMPLE_DOWNLOAD_URL_1))
          .thenReturn(ResponseEntity.ok(resource1));
      utilities.when(() -> HttpFetchingUtils.fetchResourceUrl(SAMPLE_DOWNLOAD_URL_2))
          .thenReturn(ResponseEntity.ok(resource2));
      utilities.when(() -> HttpFetchingUtils.extractFileNameFromUrl(SAMPLE_DOWNLOAD_URL_1))
          .thenReturn("file1.txt");
      utilities.when(() -> HttpFetchingUtils.extractFileNameFromUrl(SAMPLE_DOWNLOAD_URL_2))
          .thenReturn("file2.txt");

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      // When
      OutputStream returnedStream = FileUtils.buildArtifactStreamFromArtifactUrls(List.of(SAMPLE_DOWNLOAD_URL_1, SAMPLE_DOWNLOAD_URL_2), baos);

      // Then
      assertSame(baos, returnedStream, "the result stream should come from the input");

      // Verify ZIP content
      try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
        ZipEntry entry;
        boolean foundFile1 = false;
        boolean foundFile2 = false;
        boolean foundConfig = false;
        while ((entry = zis.getNextEntry()) != null) {
          String entryName = entry.getName();
          if (entryName.equals("file1.txt")) {
            foundFile1 = true;
            String extracted = new String(zis.readAllBytes());
            assertEquals(content1, extracted, "The extracted value should equal given input text's stream");
          } else if (entryName.equals("file2.txt")) {
            foundFile2 = true;
            String extracted = new String(zis.readAllBytes());
            assertEquals(content2, extracted, "The extracted value should equal given input text's stream");
          } else if (entryName.equals(DEPLOY_OPTION_YAML_FILE_NAME)) {
            foundConfig = true;
            assertTrue(zis.readAllBytes().length > 0, "Config file should not be empty");
          }
        }
        assertTrue(foundFile1, "file1.txt should be present in ZIP");
        assertTrue(foundFile2, "file2.txt should be present in ZIP");
        assertTrue(foundConfig, "Configuration file should be present in ZIP");
      }
    }
  }

  @Test
  void testBuildArtifactStreamFromArtifactUrlsWithBadUrl() throws Exception {
    try (MockedStatic<HttpFetchingUtils> utilities = mockStatic(HttpFetchingUtils.class)) {
      utilities.when(() -> HttpFetchingUtils.fetchResourceUrl(SAMPLE_DOWNLOAD_URL_1))
          .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      OutputStream returnedStream = FileUtils.buildArtifactStreamFromArtifactUrls(List.of(SAMPLE_DOWNLOAD_URL_1), baos);
      assertSame(baos, returnedStream, "The returned stream should come from the param");

      try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(baos.toByteArray()))) {
        ZipEntry entry;
        boolean foundConfig = false;
        int entryCount = 0;
        while ((entry = zis.getNextEntry()) != null) {
          entryCount++;
          System.out.println(entry.getName());
          if (entry.getName().equals(DEPLOY_OPTION_YAML_FILE_NAME)) {
            foundConfig = true;
          }
        }
        assertEquals(1, entryCount, "ZIP should contain only the configuration file");
        assertTrue(foundConfig, "Configuration file should be present in ZIP");
      }
    }
  }
}
