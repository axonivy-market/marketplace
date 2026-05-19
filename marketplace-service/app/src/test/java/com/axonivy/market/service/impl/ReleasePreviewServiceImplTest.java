package com.axonivy.market.service.impl;

import com.axonivy.market.core.enums.ErrorCode;
import com.axonivy.market.exceptions.model.FileProcessingException;
import com.axonivy.market.model.ReleasePreview;
import com.axonivy.market.util.FileUtils;
import com.axonivy.market.util.ZipSafetyScanner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleasePreviewServiceImplTest {

  private ReleasePreviewServiceImpl releasePreviewService;

  private Path tempDirectory;

  private static final String README_CONTENT = "# Sample README Content\n![image](image1.png)";

  private static final String INLINE_IMAGE_URL = "data:image/png;base64," +
      Base64.getEncoder().encodeToString("mocked image content".getBytes());

  private static final String UPDATED_README_CONTENT = "# Sample README Content\n![image](" + INLINE_IMAGE_URL + ")";

  @BeforeEach
  void setUp() throws IOException {
    releasePreviewService = spy(new ReleasePreviewServiceImpl());
    tempDirectory = Files.createTempDirectory("test-dir");
  }

  @AfterEach
  void tearDown() throws IOException {
    FileUtils.clearDirectory(tempDirectory);
  }

  @Test
  void testProcessReadme() throws IOException {
    Path tempReadmeFile = Files.createTempFile("README", ".md");
    Files.writeString(tempReadmeFile, README_CONTENT);
    Map<String, Map<String, String>> moduleContents = new HashMap<>();

    doReturn(UPDATED_README_CONTENT).when(releasePreviewService)
        .updateImagesWithInlineImages(tempDirectory.toString(), README_CONTENT);
    releasePreviewService.processReadme(tempReadmeFile, moduleContents, tempDirectory.toString());

    assertEquals(3, moduleContents.size(),
        "Module contents size should be 3");
    Files.deleteIfExists(tempReadmeFile);
  }

  @Test
  void testUpdateImagesWithInlineImagesSuccess() throws IOException {
    Path tempReadmeFile = Files.createTempFile("README", ".md");
    Files.writeString(tempReadmeFile, README_CONTENT);
    String parentPath = tempReadmeFile.getParent().toString();

    Path imagePath1 = Paths.get(parentPath + "/image1.png");
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.walk(Paths.get(parentPath)))
          .thenReturn(Stream.of(imagePath1));
      mockedFiles.when(() -> Files.isRegularFile(any()))
          .thenReturn(true);
      mockedFiles.when(() -> Files.probeContentType(imagePath1)).thenReturn("image/png");
      mockedFiles.when(() -> Files.readAllBytes(imagePath1)).thenReturn("mocked image content".getBytes());
      String result = releasePreviewService.updateImagesWithInlineImages(parentPath, README_CONTENT);

      assertNotNull(result,
          "Images with download URL should not be null");
      assertTrue(result.contains(INLINE_IMAGE_URL),
          "Preview image references should be replaced with inline data URLs");
    }
    Files.deleteIfExists(tempReadmeFile);
  }

  @Test
  void testUpdateImagesWithInlineImagesIOException() {
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.walk(tempDirectory))
          .thenThrow(new IOException("Simulated IOException"));
      assertThrows(IOException.class, () ->
              releasePreviewService.updateImagesWithInlineImages(tempDirectory.toString(), README_CONTENT),
          "Should not throw error if getting image directory failed");
    }
  }

  @Test
  void testExtractReadmeSuccess() throws IOException {
    String parentPath = tempDirectory.getParent().toString();
    Path readmeFile = FileUtils.createFile(parentPath + "/README.md").toPath();
    Files.writeString(readmeFile, README_CONTENT);

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.walk(tempDirectory))
          .thenReturn(Stream.of(readmeFile));
      mockedFiles.when(() -> Files.isRegularFile(any()))
          .thenReturn(true);
      mockedFiles.when(() -> Files.readString(any()))
          .thenReturn(README_CONTENT);
      doReturn(UPDATED_README_CONTENT).when(releasePreviewService).updateImagesWithInlineImages(any(), anyString());

      ReleasePreview result = releasePreviewService.extractReadme(tempDirectory.toString());
      assertNotNull(result, "Release preview should not be null");
    }
    Files.deleteIfExists(readmeFile);
  }

  @Test
  void testExtractREADMENoReadmeFiles() {
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.walk(tempDirectory))
          .thenReturn(Stream.empty());

      ReleasePreview result = releasePreviewService.extractReadme(tempDirectory.toString());
      assertNull(result, "Release preview should be null");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void testExtractReadmeIOException() {
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.walk(tempDirectory))
          .thenThrow(new FileProcessingException(ErrorCode.FILE_PROCESSING_ERROR));

      String tempDirPath = tempDirectory.toString();
      assertThrows(FileProcessingException.class, () -> releasePreviewService.extractReadme(tempDirPath),
          "Should throw IOException if walking readme directory fails"
      );
    }
  }

  @Test
  void testExtractSuccess() throws IOException {
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "mockFileName",
        "application/zip", "test".getBytes());
    when(releasePreviewService.extractReadme(anyString())).thenReturn(new ReleasePreview());
    try (MockedStatic<FileUtils> fileUtils = Mockito.mockStatic(FileUtils.class);
         MockedStatic<ZipSafetyScanner> zipScanner = Mockito.mockStatic(ZipSafetyScanner.class)) {
      fileUtils.when(() -> FileUtils.unzip(any(), anyString())).thenAnswer(invocation -> null);
      fileUtils.when(() -> FileUtils.clearDirectory(any())).thenAnswer(invocation -> null);

      ReleasePreview result = releasePreviewService.extract(mockMultipartFile);
      assertNotNull(result, "Release preview should NOT be null");
      fileUtils.verify(() -> FileUtils.clearDirectory(any()), times(1));
    }
  }

  @Test
  void testExtractIOException() {
    MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "mockFileName",
        "application/zip", "test".getBytes());
    try (MockedStatic<FileUtils> fileUtils = Mockito.mockStatic(FileUtils.class)) {
      fileUtils.when(() -> FileUtils.unzip(any(), anyString())).thenThrow(new IOException());
    }
    assertThrows(FileProcessingException.class,
        () -> releasePreviewService.extract(mockMultipartFile),
        "Should throw error if unzipping file failed");
  }

}