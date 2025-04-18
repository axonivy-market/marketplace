package com.axonivy.market.service.impl;

import com.axonivy.market.model.ReleasePreview;
import com.axonivy.market.util.FileUtils;
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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static com.axonivy.market.constants.PreviewConstants.IMAGE_DOWNLOAD_URL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReleasePreviewServiceImplTest {

    private ReleasePreviewServiceImpl releasePreviewService;

    private Path tempDirectory;

    private static final String BASE_URL = "http://example.com";

    private static final String README_CONTENT = "# Sample README Content\n![image](image1.png)";

    private static final String UPDATED_README_CONTENT = "# Sample README Content\n![image](http://example" +
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
        Files.writeString(tempReadmeFile, README_CONTENT);
        Map<String, Map<String, String>> moduleContents = new HashMap<>();

        doReturn(UPDATED_README_CONTENT).when(releasePreviewService)
                .updateImagesWithDownloadUrl(tempDirectory.toString(), README_CONTENT, BASE_URL);
        releasePreviewService.processReadme(tempReadmeFile, moduleContents, BASE_URL, tempDirectory.toString());

        assertEquals(3, moduleContents.size());
        Files.deleteIfExists(tempReadmeFile);
    }

    @Test
    void testUpdateImagesWithDownloadUrl_Success() throws IOException {
        Path tempReadmeFile = Files.createTempFile("README", ".md");
        Files.writeString(tempReadmeFile, README_CONTENT);
        String parentPath = tempReadmeFile.getParent().toString();

        Path imagePath1 = Paths.get(parentPath + "/image1.png");
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.walk(Paths.get(parentPath)))
                    .thenReturn(Stream.of(imagePath1));
            mockedFiles.when(() -> Files.isRegularFile(any()))
                    .thenReturn(true);
            String result = releasePreviewService.updateImagesWithDownloadUrl(parentPath,
                    README_CONTENT
                    , BASE_URL);

            assertNotNull(result);
            assertTrue(result.contains(String.format(IMAGE_DOWNLOAD_URL, BASE_URL, "image1.png")));
        }
        Files.deleteIfExists(tempReadmeFile);
    }

    @Test
    void testUpdateImagesWithDownloadUrl_IOException() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.walk(tempDirectory))
                    .thenThrow(new IOException("Simulated IOException"));
            assertDoesNotThrow(
                    () -> releasePreviewService.updateImagesWithDownloadUrl(tempDirectory.toString()
                            , README_CONTENT, BASE_URL));
        }
    }

    @Test
    void testExtractReadme_Success() throws IOException {
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
            when(releasePreviewService.updateImagesWithDownloadUrl(any(), anyString(), anyString())).thenReturn(
                    UPDATED_README_CONTENT);

            ReleasePreview result = releasePreviewService.extractReadme(BASE_URL, tempDirectory.toString());
            assertNotNull(result);
        }
        Files.deleteIfExists(readmeFile);
    }

    @Test
    void testExtractREADME_NoReadmeFiles() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.walk(tempDirectory))
                    .thenReturn(Stream.empty());

            ReleasePreview result = releasePreviewService.extractReadme(BASE_URL, tempDirectory.toString());
            assertNull(result);
        }
    }

    @Test
    void testExtractReadme_IOException() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.walk(tempDirectory))
                    .thenThrow(new IOException("Simulated IOException"));

            ReleasePreview result = releasePreviewService.extractReadme(BASE_URL, tempDirectory.toString());
            assertNull(result);
            assertDoesNotThrow(
                    () -> releasePreviewService.extractReadme(BASE_URL, tempDirectory.toString()));
        }
    }

    @Test
    void testExtract_Success() {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "mockFileName",
                "application/zip", "test".getBytes());
        try (MockedStatic<FileUtils> fileUtils = Mockito.mockStatic(FileUtils.class)) {
            fileUtils.when(() -> FileUtils.unzip(any(), anyString())).thenAnswer(invocation -> null);
        }
        when(releasePreviewService.extractReadme(anyString(), anyString())).thenReturn(new ReleasePreview());
        ReleasePreview result = releasePreviewService.extract(mockMultipartFile, tempDirectory.toString());
        assertNotNull(result);
    }

    @Test
    void testExtract_IOException() {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", "mockFileName",
                "application/zip", "test".getBytes());
        try (MockedStatic<FileUtils> fileUtils = Mockito.mockStatic(FileUtils.class)) {
            fileUtils.when(() -> FileUtils.unzip(any(), anyString())).thenThrow(new IOException());
        }
        ReleasePreview result = releasePreviewService.extract(mockMultipartFile, tempDirectory.toString());
        assertNull(result);
        assertDoesNotThrow(
                () -> releasePreviewService.extract(mockMultipartFile, tempDirectory.toString()));
    }

}
