package com.axonivy.market.service;

import com.axonivy.market.entity.Metadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.axonivy.market.constants.DirectoryConstants.DATA_DIR;
import static com.axonivy.market.constants.DirectoryConstants.WORK_DIR;

public interface FileDownloadService {
  String ROOT_STORAGE_FOR_PRODUCT_CONTENT = String.join(File.separator, DATA_DIR, WORK_DIR);

  String downloadAndUnzipProductContentFile(String url, Metadata snapShotMetadata) throws IOException;

  void deleteDirectory(Path path);
}