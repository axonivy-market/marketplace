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
import java.util.Optional;

import static com.axonivy.market.constants.PreviewConstants.PREVIEW_DIR;

@Service
@Log4j2
@AllArgsConstructor
public class ImageServiceImpl implements ImageService {

  private final ImageRepository imageRepository;
  private final FileDownloadService fileDownloadService;

  @Override
  public byte[] getImageBinary(GHContent ghContent, String downloadUrl) {
    try {
      InputStream contentStream = ghContent.read();
      return IOUtils.toByteArray(contentStream);
    } catch (Exception exception) {
      log.error("Cannot get content of product image {} ", ghContent.getName());
      return getImageByDownloadUrl(downloadUrl);
    }
  }

  private byte[] getImageByDownloadUrl(String downloadUrl) {
    try {
      return fileDownloadService.downloadFile(downloadUrl);
    } catch (Exception exception) {
      log.error("Cannot download the image from the url: {} with error {}", downloadUrl, exception.getMessage());
      return new byte[0];
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
    byte[] imageContent = Optional.of(getImageBinary(ghContent, currentImageUrl))
        .filter(ObjectUtils::isNotEmpty).orElse(null);

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
    existingImages.forEach(image -> Hibernate.initialize(image.getImageData()));
    try {
      InputStream contentStream = MavenUtils.extractedContentStream(imagePath);
        assert contentStream != null;
        byte[] sourceBytes = IOUtils.toByteArray(contentStream);

      Image existedImage = existingImages.stream().filter(image -> {
        byte[] imageData = Optional.of(image).map(Image::getImageData).orElse(null);
        return ObjectUtils.isNotEmpty(imageData) && Arrays.equals(imageData, sourceBytes);
      }).findAny().orElse(null);

      if (ObjectUtils.isEmpty(existedImage)) {
        Image image = new Image();
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

  @Override
  public byte[] readPreviewImageByName(String imageName) {
    Path previewPath = Paths.get(PREVIEW_DIR);
    if (!Files.exists(previewPath) || !Files.isDirectory(previewPath)) {
      log.info("#readPreviewImageByName: Preview folder not found");
    }
    try {
      Optional<Path> imagePath = Files.walk(previewPath)
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().equalsIgnoreCase(imageName))
          .findFirst();
      if (imagePath.isEmpty()) {
        log.info("#readPreviewImageByName: Image with name {} is missing", imageName);
        return new byte[0];
      }
      InputStream contentStream = MavenUtils.extractedContentStream(imagePath.get());
        assert contentStream != null;
        return IOUtils.toByteArray(contentStream);
    } catch (IOException e) {
      log.error("#readPreviewImageByName: Error when read preview image {}: {}", imageName, e.getMessage());
      return new byte[0];
    }
  }

}
