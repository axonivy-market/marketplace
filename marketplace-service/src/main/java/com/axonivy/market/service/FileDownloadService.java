package com.axonivy.market.service;

public interface FileDownloadService {
  void downloadAndUnzipFile(String url, boolean isForce) throws Exception;
}
