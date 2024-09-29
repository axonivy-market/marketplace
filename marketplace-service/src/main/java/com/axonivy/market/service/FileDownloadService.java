package com.axonivy.market.service;

import com.axonivy.market.entity.Metadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.axonivy.market.constants.DirectoryConstants.CACHE_DIR;
import static com.axonivy.market.constants.DirectoryConstants.DATA_DIR;

public interface FileDownloadService {
  String ROOT_STORAGE = String.join(File.separator, DATA_DIR, CACHE_DIR);

  String downloadAndUnzipProductContentFile(String url, Metadata snapShotMetadata) throws IOException;

  void deleteDirectory(Path path);
}