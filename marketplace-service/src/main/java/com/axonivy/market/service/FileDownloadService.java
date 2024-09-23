package com.axonivy.market.service;

public interface FileDownloadService {
  String downloadAndUnzipFile(String url, boolean isForce) throws Exception;
}
