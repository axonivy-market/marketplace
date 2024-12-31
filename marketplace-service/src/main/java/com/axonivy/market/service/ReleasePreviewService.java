package com.axonivy.market.service;

import com.axonivy.market.model.ReleasePreview;
import org.springframework.web.multipart.MultipartFile;

public interface ReleasePreviewService {

  ReleasePreview extract(MultipartFile file, String baseUrl);

}
