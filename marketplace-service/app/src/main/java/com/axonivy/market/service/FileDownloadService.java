package com.axonivy.market.service;

import com.axonivy.market.bo.DownloadOption;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static com.axonivy.market.constants.DirectoryConstants.*;

public interface FileDownloadService {
  String ROOT_STORAGE_FOR_PRODUCT_CONTENT = String.join(File.separator, DATA_DIR, WORK_DIR);
  String ROOT_STORAGE_FOR_CACHE = String.join(File.separator, DATA_DIR, CACHE_DIR);

  /**
   * <p>
   * Download file by url
   * </p>
   *
   * @param  url
   *              type {@link String}
   * @return {@link byte}
   * @author nqhoan
   */
  byte[] downloadFile(String url);

  /**
   * <p>
   * Delete directory
   * </p>
   *
   * @param  path
   *              type {@link Path}
   * @return {@link }
   * @author ntqdinh
   */
  void deleteDirectory(Path path);

  /**
   * <p>
   * Get file into string type
   * </p>
   *
   * @param  url
   *              type {@link String}
   * @return {@link String}
   * @author ntqdinh
   */
  String getFileAsString(String url);

  /**
   * <p>
   * Fetch url resource by url
   * </p>
   *
   * @param  url
   *              type {@link String}
   * @return {@link ResponseEntity<Resource>}
   * @author ntqdinh
   */
  ResponseEntity<Resource> fetchUrlResource(String url);

  /**
   * <p>
   * Download and unzip file by url
   * </p>
   *
   * @param  url
   *              type {@link String}
   * @param  downloadOption
   *              type {@link DownloadOption}
   * @return {@link String}
   * @author nqhoan
   */
  String downloadAndUnzipFile(String url, DownloadOption downloadOption) throws IOException;

  /**
   * <p>
   * generateCacheStorageDirectory
   * </p>
   *
   * @param  url
   *              type {@link String}
   * @return {@link String}
   * @author nqhoan
   */
  String generateCacheStorageDirectory(String url);
}
