package com.axonivy.market.service;

import com.axonivy.market.core.entity.Image;
import com.axonivy.market.core.service.CoreImageService;
import org.kohsuke.github.GHContent;

import java.nio.file.Path;

public interface ImageService extends CoreImageService {
  byte[] getImageBinary(GHContent ghContent, String downloadUrl);

  Image mappingImageFromGHContent(String productId, GHContent ghContent);

  Image mappingImageFromDownloadedFolder(String productId, Path imagePath);

  byte[] readImage(String id);

  byte[] readPreviewImageByName(String imageName);
}
