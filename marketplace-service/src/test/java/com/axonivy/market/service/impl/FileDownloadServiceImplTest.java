package com.axonivy.market.service.impl;

import com.axonivy.market.repository.ExternalDocumentMetaRepository;
import com.axonivy.market.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileDownloadServiceImplTest {

  private static final String DOWNLOAD_URL = "https://repo/axonivy/portal/portal-guide/10.0.0/portal-guide-10.0.0.zip";
  private static final String EMPTY_SOURCE_URL_META_JSON_FILE = "/emptySourceUrlMeta.json";
  private static final String PORTAL = "portal";

  @Mock
  ProductRepository productRepository;

  @Mock
  ExternalDocumentMetaRepository externalDocumentMetaRepository;

  @Mock
  RestTemplate restTemplate;

  @InjectMocks
  FileDownloadServiceImpl fileDownloadService;

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
}
