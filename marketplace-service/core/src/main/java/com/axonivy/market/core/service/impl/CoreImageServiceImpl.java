package com.axonivy.market.core.service.impl;

import com.axonivy.market.core.constants.CacheNameConstants;
import com.axonivy.market.core.entity.Image;
import com.axonivy.market.core.repository.CoreImageRepository;
import com.axonivy.market.core.service.CoreImageService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service("coreImageService")
@Log4j2
@AllArgsConstructor
public class CoreImageServiceImpl implements CoreImageService {
  private final CoreImageRepository coreImageRepository;

  @Override
  @Cacheable(value = CacheNameConstants.FIND_IMAGE)
  public byte[] readImage(String id) {
    return coreImageRepository.findById(id).map(Image::getImageData).orElse(null);
  }
}
