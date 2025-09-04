package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.entity.Image;
import com.axonivy.market.repository.ImageRepository;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.util.MavenUtils;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.axonivy.market.constants.CommonConstants.SLASH;
import static com.axonivy.market.constants.MetaConstants.META_FILE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest extends BaseSetup {
  @Captor
  ArgumentCaptor<Image> argumentCaptor = ArgumentCaptor.forClass(Image.class);
  @InjectMocks
  private ImageServiceImpl imageService;
  @Mock
  private ImageRepository imageRepository;
  @Mock
  private FileDownloadService fileDownloadService;
  public static final String GOOGLE_MAPS_CONNECTOR = "google-maps-connector";

  @Test
  void testMappingImageFromGHContent() throws IOException {
    GHContent content = mock(GHContent.class);
    when(content.getSha()).thenReturn("914d9b6956db7a1404622f14265e435f36db81fa");
    when(content.getDownloadUrl()).thenReturn("https://raw.githubusercontent.com/images/comprehend-demo-sentiment.png");

    InputStream inputStream = this.getClass().getResourceAsStream(SLASH.concat(META_FILE));
    when(content.read()).thenReturn(inputStream);

    imageService.mappingImageFromGHContent(GOOGLE_MAPS_CONNECTOR, content);

    Image expectedImage = new Image();
    expectedImage.setProductId("google-maps-connector");
    expectedImage.setSha("914d9b6956db7a1404622f14265e435f36db81fa");
    expectedImage.setImageUrl("https://raw.githubusercontent.com/images/comprehend-demo-sentiment.png");

    verify(imageRepository).save(argumentCaptor.capture());

    assertEquals(expectedImage.getProductId(), argumentCaptor.getValue().getProductId(),
        "Saved image should have the same productId as the expected image");
    assertEquals(expectedImage.getSha(), argumentCaptor.getValue().getSha(),
        "Saved image should have the same SHA as the expected image");
    assertEquals(expectedImage.getImageUrl(), argumentCaptor.getValue().getImageUrl(),
        "Saved image should have the same image URL as the expected image");

    when(imageRepository.findByProductIdAndSha(anyString(), anyString())).thenReturn(List.of(expectedImage));
    Image result = imageService.mappingImageFromGHContent(GOOGLE_MAPS_CONNECTOR, content);

    assertEquals(expectedImage, result,
        "When an image with the same productId and SHA already exists, the method should return that image");
  }

  @Test
  void testMappingImageFromGHContentGetImageFromDownloadUrl() throws IOException {
    GHContent content = mock(GHContent.class);
    when(content.getSha()).thenReturn("914d9b6956db7a1404622f14265e435f36db81fa");
    when(content.getDownloadUrl()).thenReturn(MOCK_MAVEN_URL);

    byte[] mockResult = "content".getBytes();
    when(content.read()).thenThrow(new UnsupportedOperationException("Unrecognized encoding"));
    when(fileDownloadService.downloadFile(MOCK_MAVEN_URL)).thenReturn(mockResult);

    imageService.mappingImageFromGHContent(GOOGLE_MAPS_CONNECTOR, content);

    verify(imageRepository).save(argumentCaptor.capture());
    verify(fileDownloadService, times(1)).downloadFile(MOCK_MAVEN_URL);

    assertEquals(mockResult, argumentCaptor.getValue().getImageData(),
        "Image data saved in the repository should match the bytes returned by fileDownloadService when GHContent.read() fails");
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
      newImage.setImageData(newImageData);
      newImage.setProductId(productId);

      when(imageRepository.save(any(Image.class))).thenReturn(newImage);

      Image result = imageService.mappingImageFromDownloadedFolder(productId, imagePath);

      assertNotNull(result, "Returned image should not be null when mapping from a valid downloaded folder");
      assertEquals(newImage, result, "Returned image should match the newly saved image for the given productId");
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
      existingImage.setImageData(existingImageData);
      existingImage.setProductId(productId);

      when(imageRepository.findByProductId(productId)).thenReturn(List.of(existingImage));

      Image result = imageService.mappingImageFromDownloadedFolder(productId, imagePath);

      assertNotNull(result,
          "Returned image should not be null when an existing image is already stored in the repository");
      assertEquals(existingImage, result,
          "Returned image should match the already existing image instead of saving a new one");
      verify(imageRepository, never()).save(any(Image.class));
    }
  }

  @Test
  void testMappingImageFromDownloadedFolderReturnNull() {
    try (MockedStatic<MavenUtils> mockedMavenUtils = Mockito.mockStatic(MavenUtils.class)) {
      String productId = "connectivity-demo";
      Path imagePath = Path.of("connectivity-image.png");
      mockedMavenUtils.when(() -> MavenUtils.extractedContentStream(imagePath))
          .thenThrow(new NullPointerException("File not found"));

      Image result = imageService.mappingImageFromDownloadedFolder(productId, imagePath);

      assertNull(result,
          "Result should be null when the image stream cannot be extracted (e.g., file not found)");
      verify(imageRepository, times(0)).save(any(Image.class));
    }
  }

  @Test
  void testMappingImageFromGHContentNoGhContent() {
    var result = imageService.mappingImageFromGHContent(GOOGLE_MAPS_CONNECTOR, null);
    assertNull(result, "Result should be null when GHContent is not provided (null input)");
  }

  @Test
  void testReadPreviewImageByNameImageExists() {
    Path imagePath = Path.of(IMAGE_NAME);

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
         MockedStatic<MavenUtils> mockedMavenUtils = mockStatic(MavenUtils.class)) {
      mockedFiles.when(() -> Files.exists(any())).thenReturn(true);
      mockedFiles.when(() -> Files.isDirectory(any())).thenReturn(true);
      mockedFiles.when(() -> Files.isRegularFile(any())).thenReturn(true);
      mockedFiles.when(() -> Files.walk(any())).thenReturn(Stream.of(imagePath));

      InputStream mockedInputStream = new ByteArrayInputStream("mocked image content".getBytes());
      mockedMavenUtils.when(() -> MavenUtils.extractedContentStream(imagePath)).thenReturn(mockedInputStream);

      byte[] result = imageService.readPreviewImageByName(IMAGE_NAME);

      assertNotNull(result,
          "Result should not be null when the image file exists and can be read successfully");
      assertArrayEquals("mocked image content".getBytes(), result,
          "Returned image bytes should match the expected mocked image content");
    }
  }

  @Test
  void testReadPreviewImageByNameNotFoundDirectory() {
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
      mockedFiles.when(() -> Files.exists(any())).thenReturn(false);

      byte[] result = imageService.readPreviewImageByName(IMAGE_NAME);

      assertEquals(0, result.length,
          "When the image directory does not exist, the service should return an empty byte array");
    }
  }


  @Test
  void testReadPreviewImageByNameNotFoundImage() {
    Path imagePath = Path.of(IMAGE_NAME);

    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class);
         MockedStatic<MavenUtils> mockedMavenUtils = mockStatic(MavenUtils.class)) {

      mockedFiles.when(() -> Files.exists(any())).thenReturn(true);
      mockedFiles.when(() -> Files.isDirectory(any())).thenReturn(true);
      mockedFiles.when(() -> Files.isRegularFile(any())).thenReturn(true);
      mockedFiles.when(() -> Files.walk(any())).thenReturn(Stream.of(imagePath));

      byte[] result = imageService.readPreviewImageByName("wrong.png");

      assertEquals(0, result.length,
          "When the requested image name does not match any existing file, the service should return an empty byte array");
    }
  }


  @Test
  void testReadPreviewImageByNameIOException() {
    try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {

      mockedFiles.when(() -> Files.exists(any())).thenReturn(true);
      mockedFiles.when(() -> Files.isDirectory(any())).thenReturn(true);
      mockedFiles.when(() -> Files.isRegularFile(any())).thenReturn(true);
      mockedFiles.when(() -> Files.walk(any())).thenThrow(new IOException("Exception!!"));

      assertDoesNotThrow(
          () -> imageService.readPreviewImageByName(IMAGE_NAME),
          "The service should handle IOExceptions gracefully without throwing them to the caller"
      );
    }
  }

}
