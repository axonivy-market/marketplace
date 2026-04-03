package com.axonivy.market.service;

import com.axonivy.market.model.LogFileModel;
import java.io.OutputStream;
import java.util.List;

public interface LogService {

  /**
   * <p>
   * Get list global log name by date
   * </p>
   *
   * @param  date
   *              type {@link String}
   * @return {@link List<LogFileModel>}
   * @author ntqdinh
   */
  List<LogFileModel> listGzLogNamesByDate(String date);

  /**
   * <p>
   * Stream runtime log content
   * </p>
   *
   * @param  fileName
   *              type {@link String}
   * @param  outputStream
   *              type {@link OutputStream}
   * @return {@link }
   * @author ntqdinh
   */
  void streamLogContent(String fileName, OutputStream outputStream);

  /**
   * <p>
   * Check log file existed
   * </p>
   *
   * @param  fileName
   *              type {@link String}
   * @return {@link boolean}
   * @author ntqdinh
   */
  boolean isLogFileExisted(String fileName);
}
