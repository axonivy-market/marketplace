package com.axonivy.market.service;

import com.axonivy.market.core.entity.Image;
import com.axonivy.market.core.service.CoreImageService;
import org.kohsuke.github.GHContent;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

public interface ImageService extends CoreImageService {
  byte[] getImageBinary(GHContent ghContent, String downloadUrl);

  Image mappingImageFromGHContent(String productId, GHContent ghContent);

  Image mappingImageFromDownloadedFolder(String productId, Path imagePath);

  byte[] readImage(String id);

  byte[] readPreviewImageByName(String imageName);

  String saveImageWithCustomId(String id, MultipartFile file) throws IOException;
}
