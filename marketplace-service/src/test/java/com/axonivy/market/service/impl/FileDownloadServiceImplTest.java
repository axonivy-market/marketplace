package com.axonivy.market.service.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;


class FileDownloadServiceImplTest {

  @InjectMocks
  private FileDownloadServiceImpl fileDownloadService;

  @Mock
  private RestTemplate restTemplate;

  private static final String ZIP_FILE_PATH = "src/test/resources/zip/text.zip";
  private static final String EXTRACT_LOCATION = "src/test/resources/zip/data";

  @BeforeEach
  void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void createFolder() {
    String location = "testFolder";
    Path mockPath = Paths.get(location);

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.createDirectories(mockPath)).thenReturn(mockPath);

      Path resultPath = fileDownloadService.createFolder(location);

      mockedFiles.verify(() -> Files.createDirectories(mockPath), Mockito.times(1));
      Assertions.assertEquals(mockPath, resultPath);
    }
  }

  @Test
  void testCreateFolderShouldNotThrowExceptionWhenIOExceptionOccurs() {
    String location = "testFolder";
    Path mockPath = Paths.get(location);

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.createDirectories(mockPath)).thenThrow(
          new IOException("Failed to create directory"));

      Path resultPath = fileDownloadService.createFolder(location);

      mockedFiles.verify(() -> Files.createDirectories(mockPath), Mockito.times(1));
      Assertions.assertEquals(mockPath, resultPath);
    }
  }

  @Test
  void deleteDirectory_shouldDeleteAllFilesAndDirectories() throws IOException {
    // Arrange
    Path mockPath = mock(Path.class);
    Path file1 = mock(Path.class);
    Path file2 = mock(Path.class);
    Stream<Path> mockStream = Stream.of(file1, file2);

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.walk(mockPath)).thenReturn(mockStream);

      fileDownloadService.deleteDirectory(mockPath);

      mockedFiles.verify(() -> Files.delete(file1), Mockito.times(1));
      mockedFiles.verify(() -> Files.delete(file2), Mockito.times(1));
    }
  }

  @Test
  void testUnzipFile() throws IOException {
    int result = fileDownloadService.unzipFile(ZIP_FILE_PATH, EXTRACT_LOCATION);
    Assertions.assertEquals(7, result);
    fileDownloadService.deleteDirectory(Paths.get("src/test/resources/zip/data/text"));
  }
}
