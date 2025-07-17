package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileDownloadServiceImplTest extends BaseSetup {
  private static final String ZIP_FILE_PATH = "src/test/resources/zip/text.zip";
  private static final String EXTRACT_DIR_LOCATION = "src/test/resources/zip/data";
  private static final String EXTRACTED_DIR_LOCATION = "src/test/resources/zip/data/text";
  private static final String DOWNLOAD_URL = "https://repo/axonivy/portal/portal-guide/10.0.0/portal-guide-10.0.0.zip";
  @InjectMocks
  private FileDownloadServiceImpl fileDownloadService;
  @Mock
  private AuthorizationUtils authorizationUtils;
  @Mock
  private RestTemplate restTemplate;

  @Test
  void testDownloadAndUnzipFileWithEmptyResult() throws IOException {
    var result = fileDownloadService.downloadAndUnzipFile("", new DownloadOption(false, "", false));
    assertTrue(result.isEmpty());
  }

  @Test
  void testDownloadAndUnzipFileWithIssue() {
    byte[] result = fileDownloadService.downloadFile(DOWNLOAD_URL);
    assertNull(result, "Expected empty null when URL is valid but can not download file");
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
    doReturn(mockFileContent).when(spyService).downloadFile(DOWNLOAD_URL);
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
      verify(spyService).downloadFile(DOWNLOAD_URL);
      mockedFiles.verify(() -> Files.write(mockTempPath, mockFileContent), times(1));
      verify(spyService).unzipFile(mockTempPath.toString(), EXTRACT_DIR_LOCATION);
      mockedFiles.verify(() -> Files.delete(mockTempPath), times(1));
    }
  }

  @Test
  void testDownloadAndUnzipFileWithNullFileContent() throws IOException {
    FileDownloadServiceImpl spyService = Mockito.spy(fileDownloadService);
    doReturn(null).when(spyService).downloadFile(DOWNLOAD_URL);
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
      verify(spyService).downloadFile(DOWNLOAD_URL);
      mockedFiles.verify(() -> Files.write(any(Path.class), any(byte[].class)), never());
    }
  }

  @Test
  void testDownloadFile() {
    when(restTemplate.getForObject(MOCK_DOWNLOAD_URL, byte[].class)).thenReturn(getMockBytes());
    byte[] result = fileDownloadService.downloadFile(MOCK_DOWNLOAD_URL);
    assertArrayEquals(getMockBytes(), result, "Content of file download should be the same with original file.");
  }

  @Test
  void testGetFileAsString() {
    when(restTemplate.getForObject(MOCK_DOWNLOAD_URL, String.class)).thenReturn(MOCK_PRODUCT_NAME);
    String result = fileDownloadService.getFileAsString(MOCK_DOWNLOAD_URL);
    assertEquals(MOCK_PRODUCT_NAME, result, "Content of file download should be the same with original file.");
    verify(restTemplate).getForObject(MOCK_DOWNLOAD_URL, String.class);

  }

  @Test
  void testFetchResourceUrl() {
    ResponseEntity<Resource> mockedResponse = ResponseEntity.ok(mock(Resource.class));
    when(restTemplate.exchange(MOCK_DOWNLOAD_URL, HttpMethod.GET, null, Resource.class)).thenReturn(mockedResponse);
    ResponseEntity<Resource> result = fileDownloadService.fetchUrlResource(MOCK_DOWNLOAD_URL);
    assertEquals(mockedResponse, result, "Content of stream should be the same with original stream.");
    verify(restTemplate).exchange(MOCK_DOWNLOAD_URL, HttpMethod.GET, null, Resource.class);
  }

  @Test
  void testFetchUrlResource() {
    ByteArrayResource resource = new ByteArrayResource("hello".getBytes(StandardCharsets.UTF_8));
    ResponseEntity<Resource> responseEntity = ResponseEntity.ok(resource);
    when(restTemplate.exchange(DOWNLOAD_URL, HttpMethod.GET, null, Resource.class)).thenReturn(responseEntity);
    ResponseEntity<Resource> result = fileDownloadService.fetchUrlResource(DOWNLOAD_URL);

    assertEquals(result, responseEntity, "Stream resource should come from valid stream");
    verify(restTemplate).exchange(DOWNLOAD_URL, HttpMethod.GET, null, Resource.class);

    when(restTemplate.exchange(DOWNLOAD_URL, HttpMethod.GET, null, Resource.class)).thenReturn(null);
    result = fileDownloadService.fetchUrlResource(DOWNLOAD_URL);

    assertNull(result, "Result should come from the rest template");
    verify(restTemplate, times(2)).exchange(DOWNLOAD_URL, HttpMethod.GET, null, Resource.class);
  }
}