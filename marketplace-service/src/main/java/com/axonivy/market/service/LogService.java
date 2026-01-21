package com.axonivy.market.service;

import java.io.OutputStream;
import java.util.List;

public interface LogService {
  List<String> listGzLogNames();

  void streamLogContent(String fileName, OutputStream outputStream);

  boolean isLogFileExisted(String fileName);
}
