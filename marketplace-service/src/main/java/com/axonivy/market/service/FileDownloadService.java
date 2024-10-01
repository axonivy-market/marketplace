package com.axonivy.market.service;

import com.axonivy.market.entity.Metadata;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.axonivy.market.constants.DirectoryConstants.DATA_DIR;
import static com.axonivy.market.constants.DirectoryConstants.WORK_DIR;

import java.io.File;
import java.io.IOException;

import static com.axonivy.market.constants.DirectoryConstants.CACHE_DIR;
import static com.axonivy.market.constants.DirectoryConstants.DATA_DIR;

public interface FileDownloadService {
  String ROOT_STORAGE_FOR_PRODUCT_CONTENT = String.join(File.separator, DATA_DIR, WORK_DIR);
  String ROOT_STORAGE = String.join(File.separator, DATA_DIR, CACHE_DIR);

  byte[] downloadFile(String url);

  String downloadAndUnzipProductContentFile(String url, Metadata snapShotMetadata) throws IOException;

  void deleteDirectory(Path path);

  String downloadAndUnzipFile(String url, boolean isForce) throws IOException;
}
