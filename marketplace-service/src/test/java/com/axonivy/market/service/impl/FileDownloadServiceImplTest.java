package com.axonivy.market.service.impl;

import com.axonivy.market.repository.ExternalDocumentMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileDownloadServiceImplTest {
  private static final String ZIP_FILE_PATH = "src/test/resources/zip/text.zip";
  private static final String EXTRACT_DIR_LOCATION = "src/test/resources/zip/data";
  private static final String EXTRACTED_DIR_LOCATION = "src/test/resources/zip/data/text";
  private static final String DOWNLOAD_URL = "https://repo/axonivy/portal/portal-guide/10.0.0/portal-guide-10.0.0.zip";
  @Mock
  ProductRepository productRepository;
  @Mock
  ExternalDocumentMetaRepository externalDocumentMetaRepository;
  @Mock
  private RestTemplate restTemplate;
  @InjectMocks
  private FileDownloadServiceImpl fileDownloadService;

  @Test
  void testDownloadAndUnzipFileWithEmptyResult() throws IOException {
    var result = fileDownloadService.downloadAndUnzipFile("", false);
    assertTrue(result.isEmpty());
  }

  @Test
  void testDownloadAndUnzipFileWithIssue() {
    assertThrows(ResourceAccessException.class, () -> fileDownloadService.downloadAndUnzipFile(DOWNLOAD_URL, true));
  }

  @Test
  void testSupportFunctions() throws IOException {
    var mockFile = fileDownloadService.createFolder("unzip");
    var grantedPath = fileDownloadService.grantNecessaryPermissionsFor(mockFile.toString());
    assertNotNull(grantedPath);

    var mockZipFile = new File("src/test/resources/mock-doc.zip");
    var totalSizeArchive = fileDownloadService.unzipFile(mockZipFile.getPath(), mockFile.toString());
    assertTrue(totalSizeArchive > 0);
  }

  @Test
  void createFolder() {
    String location = "testFolder";
    Path mockPath = Paths.get(location);

    try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.createDirectories(mockPath)).thenReturn(mockPath);

      Path resultPath = fileDownloadService.createFolder(location);

      mockedFiles.verify(() -> Files.createDirectories(mockPath), Mockito.times(1));
      assertEquals(mockPath, resultPath);
    }
  }

  @Test
  void testCreateFolderShouldNotThrowExceptionWhenIOExceptionOccurs() {
    String location = "testFolder";
    Path mockPath = Paths.get(location);

    try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.createDirectories(mockPath)).thenThrow(
          new IOException("Failed to create directory"));

      Path resultPath = fileDownloadService.createFolder(location);

      mockedFiles.verify(() -> Files.createDirectories(mockPath), Mockito.times(1));
      assertEquals(mockPath, resultPath);
    }
  }

  @Test
  void deleteDirectory_shouldDeleteAllFilesAndDirectories() {
    // Arrange
    Path mockPath = Mockito.mock(Path.class);
    Path file1 = Mockito.mock(Path.class);
    Path file2 = Mockito.mock(Path.class);
    Stream<Path> mockStream = Stream.of(file1, file2);

    try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.walk(mockPath)).thenReturn(mockStream);

      fileDownloadService.deleteDirectory(mockPath);

      mockedFiles.verify(() -> Files.delete(file1), Mockito.times(1));
      mockedFiles.verify(() -> Files.delete(file2), Mockito.times(1));
    }
  }

  @Test
  void testUnzipFile() throws IOException {
    int result = fileDownloadService.unzipFile(ZIP_FILE_PATH, EXTRACT_DIR_LOCATION);
    assertEquals(7, result);
    fileDownloadService.deleteDirectory(Paths.get(EXTRACTED_DIR_LOCATION));
  }
}
