package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Image;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.repository.ImageRepository;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.util.MavenUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.bson.types.Binary;
import org.kohsuke.github.GHContent;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@AllArgsConstructor
public class ImageServiceImpl implements ImageService {

  private final ImageRepository imageRepository;
  private final FileDownloadService fileDownloadService;

  @Override
  public Binary getImageBinary(GHContent ghContent) {
    try {
      InputStream contentStream = ghContent.read();
      byte[] sourceBytes = IOUtils.toByteArray(contentStream);
      return new Binary(sourceBytes);
    } catch (Exception exception) {
      log.error("Cannot get content of product image {} ", ghContent.getName());
      return null;
    }
  }

  private Binary getImageByDownloadUrl(String downloadUrl) {
    try {
      byte[] downloadedImage = fileDownloadService.downloadFile(downloadUrl);
      return new Binary(downloadedImage);
    } catch (Exception exception) {
      log.error("Cannot download the image from the url: {} with error {}", downloadUrl, exception.getMessage());
      return null;
    }
  }

  @Override
  public Image mappingImageFromGHContent(String productId, GHContent ghContent) {
    if (ObjectUtils.isEmpty(ghContent)) {
      log.info("There is missing for image content for product {}", productId);
      return null;
    }

    List<Image> existedImages = imageRepository.findByProductIdAndSha(productId, ghContent.getSha());
    if (!CollectionUtils.isEmpty(existedImages)) {
      if (existedImages.size() > 1) {
        List<Image> imagesToDelete = existedImages.subList(1, existedImages.size());
        imageRepository.deleteAll(imagesToDelete);
      }
      return existedImages.get(0);
    }

    String currentImageUrl = GitHubUtils.getDownloadUrl(ghContent);
    Binary imageContent = Optional.ofNullable(getImageBinary(ghContent))
        .orElseGet(() -> getImageByDownloadUrl(currentImageUrl));

    Image image = new Image();
    image.setProductId(productId);
    image.setImageUrl(currentImageUrl);
    image.setImageData(imageContent);
    image.setSha(ghContent.getSha());
    return imageRepository.save(image);
  }

  @Override
  public Image mappingImageFromDownloadedFolder(String productId, Path imagePath) {
    List<Image> existingImages = imageRepository.findByProductId(productId);
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
        image.setProductId(productId);
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
