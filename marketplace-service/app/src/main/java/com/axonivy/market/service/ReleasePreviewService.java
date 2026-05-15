package com.axonivy.market.service;

import com.axonivy.market.model.ReleasePreview;
import org.springframework.web.multipart.MultipartFile;

public interface ReleasePreviewService {

  /**
   * <p>
   * Extracts and previews README content from an uploaded ZIP or archive file. Parses the archive, locates
   * README files, extracts markdown content, converts relative image URLs to absolute URLs using the base URL,
   * and returns a preview of the release documentation.
   * </p>
   *
   * @param  file
   *              type {@link MultipartFile} - the uploaded ZIP or JAR file containing product artifacts and README
   * @param  baseUrl
   *              type {@link String} - the base URL to prepend to relative image URLs found in the README
   *              (e.g., "https://github.com/repo/master/raw/")
   * @return {@link ReleasePreview} - extracted README content with processed image URLs, title, and summary
   * @author thxhuy
   */
  ReleasePreview extract(MultipartFile file, String baseUrl);

}
