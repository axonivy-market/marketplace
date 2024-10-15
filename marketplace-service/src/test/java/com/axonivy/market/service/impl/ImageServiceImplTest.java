package com.axonivy.market.service.impl;

import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.Product;
import com.axonivy.market.repository.ImageRepository;
import com.axonivy.market.util.MavenUtils;
import org.bson.types.Binary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHContent;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.MetaConstants.META_FILE;
import static org.bson.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {
  @Captor
  ArgumentCaptor<Image> argumentCaptor = ArgumentCaptor.forClass(Image.class);
  @InjectMocks
  private ImageServiceImpl imageService;
  @Mock
  private ImageRepository imageRepository;
  public static final String GOOGLE_MAPS_CONNECTOR = "google-maps-connector";

  @Test
  void testMappingImageFromGHContent() throws IOException {
    GHContent content = mock(GHContent.class);
    when(content.getSha()).thenReturn("914d9b6956db7a1404622f14265e435f36db81fa");
    when(content.getDownloadUrl()).thenReturn("https://raw.githubusercontent.com/images/comprehend-demo-sentiment.png");

    InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
    when(content.read()).thenReturn(inputStream);

    imageService.mappingImageFromGHContent(GOOGLE_MAPS_CONNECTOR, content, true);

    Image expectedImage = new Image();
    expectedImage.setProductId("google-maps-connector");
    expectedImage.setSha("914d9b6956db7a1404622f14265e435f36db81fa");
    expectedImage.setImageUrl("https://raw.githubusercontent.com/images/comprehend-demo-sentiment.png");

    verify(imageRepository).save(argumentCaptor.capture());

    assertEquals(argumentCaptor.getValue().getProductId(), expectedImage.getProductId());
    assertEquals(argumentCaptor.getValue().getSha(), expectedImage.getSha());
    assertEquals(argumentCaptor.getValue().getImageUrl(), expectedImage.getImageUrl());

    when(imageRepository.findByProductIdAndSha(anyString(), anyString())).thenReturn(expectedImage);
    Image result  = imageService.mappingImageFromGHContent(GOOGLE_MAPS_CONNECTOR, content, false);
    assertEquals(expectedImage, result);

  }

  @Test
  void testMappingImageFromDownloadedFolder() {
    try (MockedStatic<MavenUtils> mockedMavenUtils = Mockito.mockStatic(MavenUtils.class)) {
      String productId = "connectivity-demo";

      byte[] newImageData = "connectivity-image-data".getBytes();

      Path imagePath = Path.of("connectivity-image.png");
      ByteArrayInputStream inputStream = new ByteArrayInputStream(newImageData);
      mockedMavenUtils.when(() -> MavenUtils.extractedContentStream(imagePath)).thenReturn(inputStream);
      when(imageRepository.findByProductId(productId)).thenReturn(Collections.emptyList());

      Image newImage = new Image();
      newImage.setImageData(new Binary(newImageData));
      newImage.setProductId(productId);

      when(imageRepository.save(any(Image.class))).thenReturn(newImage);

      Image result = imageService.mappingImageFromDownloadedFolder(productId, imagePath);

      assertNotNull(result);
      assertEquals(newImage, result);
      verify(imageRepository).save(any(Image.class));
    }
  }

  @Test
  void testMappingImageFromDownloadedFolderWhenImageExists() {
    try (MockedStatic<MavenUtils> mockedMavenUtils = Mockito.mockStatic(MavenUtils.class)) {
      String productId = "connectivity-demo";

      byte[] existingImageData = "connectivity-image-data".getBytes();
      byte[] newImageData = "connectivity-image-data".getBytes();

      Path imagePath = Path.of("connectivity-image.png");
      ByteArrayInputStream inputStream = new ByteArrayInputStream(newImageData);
      mockedMavenUtils.when(() -> MavenUtils.extractedContentStream(imagePath)).thenReturn(inputStream);

      Image existingImage = new Image();
      existingImage.setImageData(new Binary(existingImageData));
      existingImage.setProductId(productId);

      when(imageRepository.findByProductId(productId)).thenReturn(List.of(existingImage));

      Image result = imageService.mappingImageFromDownloadedFolder(productId, imagePath);

      assertNotNull(result);
      assertEquals(existingImage, result);
      verify(imageRepository, never()).save(any(Image.class));
    }
  }

  @Test
  void testMappingImageFromDownloadedFolder_ReturnNull() {
    try (MockedStatic<MavenUtils> mockedMavenUtils = Mockito.mockStatic(MavenUtils.class)) {
      String productId = "connectivity-demo";
      Path imagePath = Path.of("connectivity-image.png");
      mockedMavenUtils.when(() -> MavenUtils.extractedContentStream(imagePath)).thenThrow(
          new NullPointerException("File not found"));

      Image result = imageService.mappingImageFromDownloadedFolder(productId, imagePath);

      assertNull(result);
      verify(imageRepository, times(0)).save(any(Image.class));
    }
  }

  @Test
  void testMappingImageFromGHContent_noGhContent() {
    var result = imageService.mappingImageFromGHContent(GOOGLE_MAPS_CONNECTOR, null, true);
    assertNull(result);
  }

  private Product mockProduct() {
    return Product.builder().id("google-maps-connector")
        .language("English")
        .build();
  }
}
