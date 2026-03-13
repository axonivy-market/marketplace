package com.axonivy.market.core.service.impl;

import com.axonivy.market.core.entity.Image;
import com.axonivy.market.core.repository.CoreImageRepository;
import com.axonivy.market.core.service.CoreImageService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
@AllArgsConstructor
public class CoreImageServiceImpl implements CoreImageService {
  private final CoreImageRepository coreImageRepository;

  @Override
  public byte[] readImage(String id) {
    return coreImageRepository.findById(id).map(Image::getImageData).orElse(null);
  }
}
