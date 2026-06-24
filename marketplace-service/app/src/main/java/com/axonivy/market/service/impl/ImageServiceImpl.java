package com.axonivy.market.service.impl;

import com.axonivy.market.core.entity.Image;
import com.axonivy.market.github.util.GitHubUtils;
import com.axonivy.market.repository.ImageRepository;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.service.ImageService;
import com.axonivy.market.util.MavenUtils;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.kohsuke.github.GHContent;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.axonivy.market.constants.CommonConstants.IMAGE_EXTENSION;
import static com.axonivy.market.constants.PreviewConstants.PREVIEW_DIR;
import static com.axonivy.market.constants.RegexConstants.SAFE_PATH_PATTERN;

@Service
@Log4j2
@AllArgsConstructor
public class ImageServiceImpl implements ImageService {

  private static final Pattern IMAGE_EXTENSION_PATTERN = Pattern.compile(IMAGE_EXTENSION);

  private final ImageRepository imageRepository;
  private final FileDownloadService fileDownloadService;

  @Override
  public byte[] getImageBinary(GHContent ghContent, String downloadUrl) {
    try (InputStream contentStream = ghContent.read()) {
      return IOUtils.toByteArray(contentStream);
    } catch (IOException | UnsupportedOperationException exception) {
      log.error("Cannot get content of product image {}: {}", ghContent.getName(), exception.getMessage(), exception);
      return getImageByDownloadUrl(downloadUrl);
    }
  }

  private byte[] getImageByDownloadUrl(String downloadUrl) {
    return fileDownloadService.downloadFile(downloadUrl);
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
      return existedImages.getFirst();
    }

    String currentImageUrl = GitHubUtils.getDownloadUrl(ghContent);
    byte[] imageContent = Optional.of(getImageBinary(ghContent, currentImageUrl))
        .filter(ObjectUtils::isNotEmpty).orElse(null);

    var image = new Image();
    image.setProductId(productId);
    image.setImageUrl(currentImageUrl);
    image.setImageData(imageContent);
    image.setSha(ghContent.getSha());
    return imageRepository.save(image);
  }

  @Override
  public Image mappingImageFromDownloadedFolder(String productId, Path imagePath) {
    List<Image> existingImages = imageRepository.findByProductId(productId);
    existingImages.forEach(image -> Hibernate.initialize(image.getImageData()));
    try {
      InputStream contentStream = MavenUtils.extractedContentStream(imagePath);
        assert contentStream != null;
        byte[] sourceBytes = IOUtils.toByteArray(contentStream);

      var existedImage = existingImages.stream().filter((Image image) -> {
        byte[] imageData = Optional.of(image).map(Image::getImageData).orElse(null);
        return ObjectUtils.isNotEmpty(imageData) && Arrays.equals(imageData, sourceBytes);
      }).findAny().orElse(null);

      if (ObjectUtils.isEmpty(existedImage)) {
        var image = new Image();
        image.setImageData(sourceBytes);
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
    return imageRepository.findById(id).map(Image::getImageData).orElse(null);
  }
}
