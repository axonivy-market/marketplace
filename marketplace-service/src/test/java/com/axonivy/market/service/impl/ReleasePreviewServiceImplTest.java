package com.axonivy.market.service.impl;

import com.axonivy.market.model.ReleasePreview;
import com.axonivy.market.util.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.axonivy.market.constants.PreviewConstants.IMAGE_DOWNLOAD_URL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

;

@ExtendWith(MockitoExtension.class)
class ReleasePreviewServiceImplTest {

  private ReleasePreviewServiceImpl releasePreviewService;

  private Path tempDirectory;

  private final String baseUrl = "http://example.com";

  private final String readmeContent = "# Sample README Content\n![image](image1.png)";

  private final String updatedReadme = "# Sample README Content\n![image](http://example" +
      ".com/api/image/preview/image1.png)";

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
    Files.writeString(tempReadmeFile, readmeContent);
    Map<String, Map<String, String>> moduleContents = new HashMap<>();
    doReturn(updatedReadme).when(releasePreviewService)
        .updateImagesWithDownloadUrl(tempDirectory.toString(), readmeContent, baseUrl);
    releasePreviewService.processReadme(tempReadmeFile, moduleContents, baseUrl, tempDirectory.toString());
    assertEquals(3, moduleContents.size());
    Files.deleteIfExists(tempReadmeFile);
  }

  @Test
  void testUpdateImagesWithDownloadUrl_Success() throws IOException {
    Path tempReadmeFile = Files.createTempFile("README", ".md");
    Files.writeString(tempReadmeFile, readmeContent);
    String parentPath = tempReadmeFile.getParent().toString();

    Path imagePath1 = Paths.get(parentPath + "/image1.png");
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.walk(Paths.get(parentPath)))
          .thenReturn(Stream.of(imagePath1));
      mockedFiles.when(() -> Files.isRegularFile(any()))
          .thenReturn(true);
      String result = releasePreviewService.updateImagesWithDownloadUrl(parentPath,
          readmeContent
          , baseUrl);

      assertNotNull(result);
      assertTrue(result.contains(String.format(IMAGE_DOWNLOAD_URL, baseUrl, "image1.png")));
    }
  }

  @Test
  void testUpdateImagesWithDownloadUrl_IOException() {
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.walk(tempDirectory))
          .thenThrow(new IOException("Simulated IOException"));
      String result = releasePreviewService.updateImagesWithDownloadUrl(tempDirectory.toString(), readmeContent,
          baseUrl);
      assertNull(result);
      assertDoesNotThrow(
          () -> releasePreviewService.updateImagesWithDownloadUrl(tempDirectory.toString(), readmeContent, baseUrl));
    }
  }

  @Test
  void testExtractREADME_Success() throws IOException {
    String parentPath = tempDirectory.getParent().toString();
    Path readmeFile1 = FileUtils.createFile(parentPath + "/README.md").toPath();
    Files.writeString(readmeFile1, readmeContent);

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.walk(tempDirectory))
          .thenReturn(Stream.of(readmeFile1));
      mockedFiles.when(() -> Files.isRegularFile(any()))
          .thenReturn(true);
      mockedFiles.when(() -> Files.readString(any()))
          .thenReturn(readmeContent);
      when(releasePreviewService.updateImagesWithDownloadUrl(any(), anyString(), anyString())).thenReturn(
          updatedReadme);
      ReleasePreview result = releasePreviewService.extractREADME(baseUrl, tempDirectory.toString());
      assertNotNull(result);
    }
  }

  @Test
  void testExtractREADME_NoReadmeFiles() {
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.walk(tempDirectory))
          .thenReturn(Stream.empty());
      ReleasePreview result = releasePreviewService.extractREADME(baseUrl, tempDirectory.toString());
      assertNull(result);
      mockedFiles.verify(() -> Files.walk(tempDirectory), times(1));
    }
  }

  @Test
  void testExtractREADME_IOException() {
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.walk(tempDirectory))
          .thenThrow(new IOException("Simulated IOException"));
      ReleasePreview result = releasePreviewService.extractREADME(baseUrl, tempDirectory.toString());
      assertNull(result);
      assertDoesNotThrow(
          () -> releasePreviewService.extractREADME(baseUrl, tempDirectory.toString()));
    }
  }

}
