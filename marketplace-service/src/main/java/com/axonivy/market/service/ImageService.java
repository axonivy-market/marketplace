package com.axonivy.market.service;

import com.axonivy.market.entity.Image;
import org.bson.types.Binary;
import org.kohsuke.github.GHContent;

import java.nio.file.Path;

public interface ImageService {
  Binary getImageBinary(GHContent ghContent);

  Image mappingImageFromGHContent(String productId, GHContent ghContent);

  Image mappingImageFromDownloadedFolder(String productId, Path imagePath);

  byte[] readImage(String id);

  byte[] readPreviewImageByName(String imageName);
}
