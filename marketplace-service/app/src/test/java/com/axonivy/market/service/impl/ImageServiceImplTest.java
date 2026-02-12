package com.axonivy.market.service.impl;

import com.axonivy.market.BaseSetup;
import com.axonivy.market.core.entity.Image;
import com.axonivy.market.repository.ImageRepository;
import com.axonivy.market.service.FileDownloadService;
import com.axonivy.market.util.FileValidator;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.axonivy.market.core.constants.CoreCommonConstants.SLASH;
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
    when(content.read()).thenThrow(new IOException("Unrecognized encoding"));
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

  @Test
  void testGetImageByCustomIdCoveringIfElse() {
    // Test if condition: when images list is empty
    when(imageRepository.findByCustomId("nonexistent-id")).thenReturn(Collections.emptyList());
    byte[] emptyResult = imageService.getImageByCustomId("nonexistent-id");
    assertEquals(0, emptyResult.length,
        "When no images are found for the custom ID, an empty byte array should be returned (if branch)");

    // Test else condition: when images list has data
    Image mockImage = new Image();
    byte[] imageData = "image content".getBytes();
    mockImage.setImageData(imageData);
    when(imageRepository.findByCustomId("existing-id")).thenReturn(List.of(mockImage));
    byte[] result = imageService.getImageByCustomId("existing-id");
    assertArrayEquals(imageData, result,
        "When images are found, the first image's data should be returned (else branch)");
  }

  @Test
  void testSaveImageWithCustomIdNoExistingImages() throws IOException {
    MultipartFile mockFile = mock(MultipartFile.class);
    byte[] fileBytes = "new image content".getBytes();

    when(mockFile.getBytes()).thenReturn(fileBytes);
    when(mockFile.getOriginalFilename()).thenReturn("image.jpg");
    when(imageRepository.findByCustomId("custom-123")).thenReturn(Collections.emptyList());

    Image savedImage = new Image();
    savedImage.setId("saved-image-id");
    savedImage.setImageData(fileBytes);
    savedImage.setImageUrl("image.jpg");
    savedImage.setCustomId("custom-123");
    when(imageRepository.save(any(Image.class))).thenReturn(savedImage);

    try (MockedStatic<FileValidator> mockedValidator = mockStatic(FileValidator.class)) {
      mockedValidator.when(() -> FileValidator.validateImageFile(mockFile)).thenAnswer(invocation -> null);

      String result = imageService.saveImageWithCustomId("custom-123", mockFile);

      assertEquals("saved-image-id", result,
          "When no existing images are found, the service should save and return the new image ID");
      verify(imageRepository, never()).deleteAll(anyIterable());
    }
  }

  @Test
  void testSaveImageWithCustomIdDeletesExistingImages() throws IOException {
    MultipartFile mockFile = mock(MultipartFile.class);
    byte[] fileBytes = "new image content".getBytes();

    Image existingImage = new Image();
    existingImage.setId("old-image-id");
    existingImage.setCustomId("custom-456");

    when(mockFile.getBytes()).thenReturn(fileBytes);
    when(mockFile.getOriginalFilename()).thenReturn("new-image.jpg");
    when(imageRepository.findByCustomId("custom-456")).thenReturn(List.of(existingImage));

    Image savedImage = new Image();
    savedImage.setId("new-saved-image-id");
    savedImage.setImageData(fileBytes);
    savedImage.setImageUrl("new-image.jpg");
    savedImage.setCustomId("custom-456");
    when(imageRepository.save(any(Image.class))).thenReturn(savedImage);

    try (MockedStatic<FileValidator> mockedValidator = mockStatic(FileValidator.class)) {
      mockedValidator.when(() -> FileValidator.validateImageFile(mockFile)).thenAnswer(invocation -> null);

      String result = imageService.saveImageWithCustomId("custom-456", mockFile);

      assertEquals("new-saved-image-id", result,
          "When existing images are found, the service should delete them and return the new image ID");
      verify(imageRepository, times(1)).deleteAll(List.of(existingImage));
    }
  }

}
