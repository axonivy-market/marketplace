package com.axonivy.market.stable.service.impl;

import com.axonivy.market.core.service.CoreImageService;
import com.axonivy.market.stable.service.ImageService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ImageServiceImpl implements ImageService {

  private final CoreImageService coreImageService;

  public ImageServiceImpl(
      @Qualifier("CoreImageService") CoreImageService coreImageService) {this.coreImageService = coreImageService;}

  @Override
  public byte[] readImage(String id) {
    return coreImageService.readImage(id);
  }
}
