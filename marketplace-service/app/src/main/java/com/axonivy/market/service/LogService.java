package com.axonivy.market.service;

import com.axonivy.market.model.LogFileModel;
import java.io.OutputStream;
import java.util.List;

public interface LogService {

  /**
   * <p>
   * Retrieves list of compressed log files (gzip) for a specific date. Returns metadata including filename,
   * file size, and creation date for all log files matching the specified date pattern from the logs directory.
   * </p>
   *
   * @param  date
   *              type {@link String} - the date to filter logs by (format: YYYY-MM-DD)
   * @return {@link List<LogFileModel>} - list of log file metadata for the specified date; returns empty list
   *         if no logs found for the date
   * @author ntqdinh
   */
  List<LogFileModel> listGzLogNamesByDate(String date);

  /**
   * <p>
   * Streams the content of a log file to the provided output stream. Decompresses gzip files if necessary
   * and streams the log content line-by-line to the output stream for efficient reading of large log files.
   * </p>
   *
   * @param  fileName
   *              type {@link String} - the name of the log file to stream (with or without .gz extension)
   * @param  outputStream
   *              type {@link OutputStream} - the output stream to write decompressed log content to
   * @return void - log content is written directly to the output stream
   * @author ntqdinh
   */
  void streamLogContent(String fileName, OutputStream outputStream);

  /**
   * <p>
   * Checks if a log file exists in the logs directory. Verifies file existence by checking the filesystem
   * for the specified log filename.
   * </p>
   *
   * @param  fileName
   *              type {@link String} - the name of the log file to check (e.g., "application.log" or "application.2026-04-08.log.gz")
   * @return {@link boolean} - true if log file exists and is readable; false if file does not exist or cannot be accessed
   * @author ntqdinh
   */
  boolean isLogFileExisted(String fileName);
}
