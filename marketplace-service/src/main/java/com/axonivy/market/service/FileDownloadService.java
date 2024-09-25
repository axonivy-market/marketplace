package com.axonivy.market.service;

import java.io.File;

import static com.axonivy.market.constants.DirectoryConstants.CACHE_DIR;
import static com.axonivy.market.constants.DirectoryConstants.DATA_DIR;

public interface FileDownloadService {
  String ROOT_STORAGE = String.join(File.separator, DATA_DIR, CACHE_DIR);

  String downloadAndUnzipFile(String url, boolean isForce) throws Exception;
}
