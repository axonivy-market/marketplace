package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.Product;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.repository.ImageRepository;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.util.MavenUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.types.Binary;
import org.kohsuke.github.GHContent;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class ImageServiceImpl implements ImageService {

  private final ImageRepository imageRepository;

  public ImageServiceImpl(ImageRepository imageRepository) {
    this.imageRepository = imageRepository;
  }

  @Override
  public Binary getImageBinary(GHContent ghContent) {
    try {
      InputStream contentStream = ghContent.read();
      byte[] sourceBytes = IOUtils.toByteArray(contentStream);
      return new Binary(sourceBytes);
    } catch (Exception exception) {
      log.error("Cannot get content of product logo {} ", ghContent.getName());
      return null;
    }
  }

  @Override
  public Image mappingImageFromGHContent(Product product, GHContent ghContent, boolean isLogo) {
    if (ObjectUtils.isEmpty(ghContent)) {
      log.info("There is missing for image content for product {}", product.getId());
      return null;
    }

    if (!isLogo) {
      Image existsImage = imageRepository.findByProductIdAndSha(product.getId(), ghContent.getSha());
      if (ObjectUtils.isNotEmpty(existsImage)) {
        return existsImage;
      }
    }
    Image image = new Image();
    String currentImageUrl = GitHubUtils.getDownloadUrl(ghContent);
    image.setProductId(product.getId());
    image.setImageUrl(currentImageUrl);
    image.setImageData(getImageBinary(ghContent));
    image.setSha(ghContent.getSha());
    return imageRepository.save(image);
  }

  @Override
  public Image mappingImageFromDownloadedFolder(Product product, Path imagePath) {
    List<Image> existingImages = imageRepository.findByProductId(product.getId());
    try {
      InputStream contentStream = MavenUtils.extractedContentStream(imagePath);
      byte[] sourceBytes = IOUtils.toByteArray(contentStream);

      Image existedImage = existingImages.stream().filter(image -> {
        byte[] imageData = Optional.of(image).map(Image::getImageData).map(Binary::getData).orElse(null);
        return ObjectUtils.isNotEmpty(imageData) && Arrays.equals(imageData, sourceBytes);
      }).findAny().orElse(null);

      if (ObjectUtils.isEmpty(existedImage)) {
        Image image = new Image();
        image.setImageData(new Binary(sourceBytes));
        image.setProductId(product.getId());
        return imageRepository.save(image);
      }
      return existedImage;
    } catch (IOException | NullPointerException e) {
      log.error("Cannot get image from downloaded folder {}", e.getMessage());
      return null;
    }
  }

  @Override
  public byte[] readImage(String id) {
    return imageRepository.findById(id).map(Image::getImageData).map(Binary::getData).orElse(null);
  }
}
