package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.Product;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.repository.ImageRepository;
import com.axonivy.market.service.ImageService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.types.Binary;
import org.kohsuke.github.GHContent;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
@Log4j2
public class ImageServiceImpl implements ImageService {

  private final ImageRepository imageRepository;

  public ImageServiceImpl(ImageRepository imageRepository) {
    this.imageRepository = imageRepository;
  }

  @Override
  public Binary getBinaryImage(GHContent ghContent) {
    try {
      InputStream contentStream = ghContent.read();
      byte[] sourceBytes = IOUtils.toByteArray(contentStream);
      return new Binary(sourceBytes);
    } catch (Exception exception) {
      log.error(exception.getMessage());
      log.error("Cannot get content of product logo {} ", ghContent.getName());
      return null;
    }
  }

  @Override
  public Image mappingImageFromGHContent(Product product, GHContent ghContent, boolean isLogo) {
    String currentLogoUrl = GitHubUtils.getDownloadUrl(ghContent);
    if (BooleanUtils.isNotTrue(isLogo)) {
      Image existsImage = imageRepository.findByLogoUrlAndSha(currentLogoUrl, ghContent.getSha());
      if (ObjectUtils.isNotEmpty(existsImage)) {
        return existsImage;
      }
    }
    Image image = new Image();
    image.setProductId(product.getId());
    image.setLogoUrl(currentLogoUrl);
    image.setImageData(this.getBinaryImage(ghContent));
    image.setSha(ghContent.getSha());
    return imageRepository.save(image);
  }
}
