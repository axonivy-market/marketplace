package com.axonivy.market.service;

import com.axonivy.market.bo.DownloadOption;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.axonivy.market.constants.DirectoryConstants.*;

public interface FileDownloadService {
  String ROOT_STORAGE_FOR_PRODUCT_CONTENT = String.join(File.separator, DATA_DIR, WORK_DIR);
  String ROOT_STORAGE_FOR_CACHE = String.join(File.separator, DATA_DIR, CACHE_DIR);

  byte[] downloadFile(String url);

  void deleteDirectory(Path path);

  String getFileAsString(String url);

  ResponseEntity<Resource> fetchUrlResource(String url);

  String downloadAndUnzipFile(String url, DownloadOption downloadOption) throws IOException;
}
