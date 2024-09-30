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
  @Mock
  private File mockFile;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGrantPermissionForNonUnixSystem() {
    Mockito.when(mockFile.setReadable(true, false)).thenReturn(false);
    Mockito.when(mockFile.setWritable(true, false)).thenReturn(false);
    Mockito.when(mockFile.setExecutable(true, false)).thenReturn(false);

    fileDownloadService.grantPermissionForNonUnixSystem(mockFile);

    Mockito.verify(mockFile).setReadable(true, false);
    Mockito.verify(mockFile).setWritable(true, false);
    Mockito.verify(mockFile).setExecutable(true, false);
  }

  @Test
  void createFolder() {
    String location = "testFolder";
    Path mockPath = Paths.get(location);

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.createDirectories(mockPath)).thenReturn(mockPath);

      Path resultPath = fileDownloadService.createFolder(location);

      mockedFiles.verify(() -> Files.createDirectories(mockPath), times(1));
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

      mockedFiles.verify(() -> Files.createDirectories(mockPath), times(1));
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

    // Mock Files.walk to return the stream of files
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.walk(mockPath)).thenReturn(mockStream);

      // Act
      fileDownloadService.deleteDirectory(mockPath);

      // Assert
      mockedFiles.verify(() -> Files.delete(file1), times(1));
      mockedFiles.verify(() -> Files.delete(file2), times(1));
    }
  }


}
