package com.axonivy.market.service;

import com.axonivy.market.model.LogFileModel;
import java.io.OutputStream;
import java.util.List;

public interface LogService {
  List<LogFileModel> listGzLogNamesByDate(String date);

  void streamLogContent(String fileName, OutputStream outputStream);

  boolean isLogFileExisted(String fileName);
}
