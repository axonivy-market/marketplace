package com.axonivy.market.service;

import com.axonivy.market.model.ReleasePreview;
import org.springframework.web.multipart.MultipartFile;

public interface ReleasePreviewService {

  /**
   * <p>
   * extract readme content
   * </p>
   *
   * @param  file
   *              type {@link MultipartFile}
   * @param  baseUrl
   *              type {@link String}
   * @return {@link ReleasePreview}
   * @author thxhuy
   */
  ReleasePreview extract(MultipartFile file, String baseUrl);

}
