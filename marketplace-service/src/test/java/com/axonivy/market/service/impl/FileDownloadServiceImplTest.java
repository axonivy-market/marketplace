package com.axonivy.market.service.impl;

import com.axonivy.market.bo.DownloadOption;
import com.axonivy.market.util.FileUtils;
import com.axonivy.market.util.validator.AuthorizationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileDownloadServiceImplTest {
  private static final String ZIP_FILE_PATH = "src/test/resources/zip/text.zip";
  private static final String EXTRACT_DIR_LOCATION = "src/test/resources/zip/data";
  private static final String EXTRACTED_DIR_LOCATION = "src/test/resources/zip/data/text";
  private static final String DOWNLOAD_URL = "https://repo/axonivy/portal/portal-guide/10.0.0/portal-guide-10.0.0.zip";
  @InjectMocks
  private FileDownloadServiceImpl fileDownloadService;
  @Mock
  private AuthorizationUtils authorizationUtils;

  @Test
  void testDownloadAndUnzipFileWithEmptyResult() throws IOException {
    var result = fileDownloadService.downloadAndUnzipFile("", new DownloadOption(false, "", false));
    assertTrue(result.isEmpty());
  }

  @Test
  void testDownloadAndUnzipFileWithIssue() {
      when(authorizationUtils.resolveTrustedUrl(DOWNLOAD_URL))
          .thenThrow(new IllegalArgumentException("Invalid URL"));

      byte[] result = fileDownloadService.safeDownload(DOWNLOAD_URL);

      assertArrayEquals("".getBytes(), result, "Expected empty byte array when URL is invalid");
      verify(authorizationUtils).resolveTrustedUrl(DOWNLOAD_URL);
    }

    @Test
  void testSafeDownloadSuccess() {
    byte[] expectedContent = "test content".getBytes();

    when(authorizationUtils.resolveTrustedUrl(DOWNLOAD_URL)).thenReturn(DOWNLOAD_URL);

    FileDownloadServiceImpl spyService = Mockito.spy(fileDownloadService);
    doReturn(expectedContent).when(spyService).downloadFile(DOWNLOAD_URL);

    byte[] result = spyService.safeDownload(DOWNLOAD_URL);

    assertArrayEquals(expectedContent, result);
    verify(spyService).downloadFile(DOWNLOAD_URL);
    verify(authorizationUtils).resolveTrustedUrl(DOWNLOAD_URL);
  }

  @Test
  void testSafeDownloadReturnsEmptyBytes() {
    when(authorizationUtils.resolveTrustedUrl(DOWNLOAD_URL)).thenReturn(DOWNLOAD_URL);

    FileDownloadServiceImpl spyService = Mockito.spy(fileDownloadService);
    doThrow(HttpClientErrorException.create(HttpStatus.UNAUTHORIZED, "Unauthorized", null, null, null))
        .when(spyService).downloadFile(DOWNLOAD_URL);

    byte[] result = spyService.safeDownload(DOWNLOAD_URL);

    assertArrayEquals("".getBytes(), result);
    verify(spyService).downloadFile(DOWNLOAD_URL);
    verify(authorizationUtils).resolveTrustedUrl(DOWNLOAD_URL);
  }

  @Test
  void testDownloadAndUnzipFileWithNullTempZipPath() throws IOException {
    try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class);
         MockedStatic<FileUtils> mockFileUtils = Mockito.mockStatic(FileUtils.class)) {

      File mockChildFile = Mockito.mock(File.class);

      File mockFile = Mockito.mock(File.class);
      Mockito.when(mockFile.exists()).thenReturn(true);
      Mockito.when(mockFile.isDirectory()).thenReturn(true);
      Mockito.when(mockFile.listFiles()).thenReturn(new File[]{mockChildFile});

      mockFileUtils.when(() -> FileUtils.createNewFile(Mockito.anyString())).thenReturn(mockFile);

      var result = fileDownloadService.downloadAndUnzipFile(DOWNLOAD_URL, new DownloadOption(false, "", false));
      assertFalse(result.isEmpty());
      mockedFiles.verify(() -> Files.delete(any()), Mockito.times(0));
    }
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

  @Test
  void testDownloadAndUnzipFileSuccessful() throws IOException {
    FileDownloadServiceImpl spyService = Mockito.spy(fileDownloadService);
    byte[] mockFileContent = "mock zip content".getBytes();
    doReturn(mockFileContent).when(spyService).safeDownload(DOWNLOAD_URL);
    doReturn(100).when(spyService).unzipFile(any(String.class), any(String.class));

    DownloadOption option = DownloadOption.builder()
        .isForced(true)
        .workingDirectory(EXTRACT_DIR_LOCATION)
        .build();

    try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
      Path mockTempPath = Paths.get(ZIP_FILE_PATH);
      mockedFiles.when(() -> Files.createTempFile(any(String.class), any(String.class), any()))
          .thenReturn(mockTempPath);
      mockedFiles.when(() -> Files.createTempFile(any(String.class), any(String.class)))
          .thenReturn(mockTempPath);

      String result = spyService.downloadAndUnzipFile(DOWNLOAD_URL, option);
      assertEquals(EXTRACT_DIR_LOCATION, result);
      verify(spyService).safeDownload(DOWNLOAD_URL);
      mockedFiles.verify(() -> Files.write(mockTempPath, mockFileContent), times(1));
      verify(spyService).unzipFile(mockTempPath.toString(), EXTRACT_DIR_LOCATION);
      mockedFiles.verify(() -> Files.delete(mockTempPath), times(1));
    }
  }
  @Test
  void testDownloadAndUnzipFileWithNullFileContent() throws IOException {
    FileDownloadServiceImpl spyService = Mockito.spy(fileDownloadService);
    doReturn(null).when(spyService).safeDownload(DOWNLOAD_URL);
    DownloadOption option = DownloadOption.builder()
        .isForced(true)
        .workingDirectory(EXTRACT_DIR_LOCATION)
        .build();

    try (MockedStatic<Files> mockedFiles = Mockito.mockStatic(Files.class)) {
      Path mockTempPath = Paths.get(ZIP_FILE_PATH);
      mockedFiles.when(() -> Files.createTempFile(any(String.class), any(String.class), any()))
          .thenReturn(mockTempPath);
      mockedFiles.when(() -> Files.createTempFile(any(String.class), any(String.class)))
          .thenReturn(mockTempPath);

      String result = spyService.downloadAndUnzipFile(DOWNLOAD_URL, option);
      assertEquals(EXTRACT_DIR_LOCATION, result);
      verify(spyService).safeDownload(DOWNLOAD_URL);
      mockedFiles.verify(() -> Files.write(any(Path.class), any(byte[].class)), never());
    }
  }
}