package com.axonivy.market.service;

import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.Product;
import com.axonivy.market.entity.ProductModuleContent;
import org.bson.types.Binary;
import org.kohsuke.github.GHContent;

public interface ImageService {
  Binary getBinaryImage(GHContent ghContent);

  Image mappingImageFromGHContent(Product product, GHContent ghContent);

}
