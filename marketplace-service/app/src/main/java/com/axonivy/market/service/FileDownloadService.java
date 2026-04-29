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
   * Downloads the content of a file from the specified URL and returns it as a byte array.
   * </p>
   *
   * @param url
   *              type {@link String} - the URL of the file to download
   * @return {@link byte[]} - the content of the downloaded file as a byte array
   * @author nqhoan
   */
  byte[] downloadFile(String url);

  /**
   * <p>
   * Recursively deletes a directory and all its contents from the filesystem. Used for cleanup of cached downloads,
   * temporary files, and extracted artifacts. The operation is permanent and cannot be undone.
   * </p>
   *
   * @param path
   *              type {@link Path} - the file system path of the directory to delete
   * @author ntqdinh
   */
  void deleteDirectory(Path path);

  /**
   * <p>
   * Downloads the content of a file from the specified URL and returns it as a string.
   * </p>
   *
   * @param url
   *              type {@link String} - the URL of the file to download
   * @return {@link String} - the content of the downloaded file as a string
   * @author ntqdinh
   */
  String getFileAsString(String url);

  /**
   * <p>
   * Fetches a resource from the specified URL and returns it as a ResponseEntity containing a Resource object.
   * </p>
   *
   * @param url
   *              type {@link String} - the URL of the resource to fetch
   * @return {@link ResponseEntity<Resource>} - the HTTP response containing the fetched resource
   * @author ntqdinh
   */
  ResponseEntity<Resource> fetchUrlResource(String url);

  /**
   * <p>
   * Downloads a file from the specified URL and unzips it to a local directory based on the provided download
   * options, returning the path to the extracted content. Only if: URL ends with .zip and .iar, file is not empty,
   * directory handling allows it.
   * </p>
   *
   * @param url
   *              type {@link String} - the URL of the file to download and unzip
   * @param downloadOption
   *              type {@link DownloadOption} - options specifying how to handle the download and extraction
   * @return {@link String} - the path to the directory where the content was extracted
   * @throws IOException if an I/O error occurs during download or unzip
   * @author nqhoan
   */
  String downloadAndUnzipFile(String url, DownloadOption downloadOption) throws IOException;

  /**
   * <p>
   * Generates a unique cache storage directory path based on the provided URL. Creates a deterministic
   * directory structure that allows caching of downloaded artifacts without hash collisions. The path
   * is created in the configured cache directory.
   * </p>
   *
   * @param  url
   *              type {@link String} - the source URL to generate a cache path for
   * @return {@link String} - the absolute filesystem path for caching the downloaded content; directory
   *         may not exist yet but is guaranteed to be unique per URL
   * @author nqhoan
   */
  String generateCacheStorageDirectory(String url);
}
