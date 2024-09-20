package com.axonivy.market.service;

import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.Product;
import org.bson.types.Binary;
import org.kohsuke.github.GHContent;

public interface ImageService {
  Binary getImageBinary(GHContent ghContent);

  Image mappingImageFromGHContent(Product product, GHContent ghContent, boolean isLogo);

  byte[] readImage(String id);
}
