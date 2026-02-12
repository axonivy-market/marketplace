package com.axonivy.market.controller;

import com.axonivy.market.service.ImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

  @Mock
  private ImageService imageService;

  @Mock
  private MultipartFile multipartFile;

  @InjectMocks
  private ImageController imageController;

  @Test
  void testGetImageFromId() {
    byte[] mockImageData = "image data".getBytes();
    when(imageService.readImage("66e2b14868f2f95b2f95549a")).thenReturn(mockImageData);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_PNG);
    ResponseEntity<byte[]> expectedResult = new ResponseEntity<>(mockImageData, headers, HttpStatus.OK);

    ResponseEntity<byte[]> result = imageController.findImageById("66e2b14868f2f95b2f95549a");

    assertEquals(expectedResult, result,
        "ResponseEntity should match the expected result, including image data, headers, and status");
  }

  @Test
  void testGetImageFromIdWhenImageNotFound() {
    when(imageService.readImage("missing-id")).thenReturn(null);

    ResponseEntity<byte[]> result = imageController.findImageById("missing-id");

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode(),
        "Status should be 404 NOT_FOUND when imageService returns null");
    assertNull(result.getBody(),
        "Response body should be null when image is not found");
  }

  @Test
  void testGetImageFromIdWhenImageEmpty() {
    when(imageService.readImage("empty-id")).thenReturn(new byte[0]);

    ResponseEntity<byte[]> result = imageController.findImageById("empty-id");

    assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode(),
        "Status should be 204 NO_CONTENT when imageService returns an empty array");
    assertNull(result.getBody(),
        "Response body should be null when image is empty");
  }

  @Test
  void testFindPreviewImageByNameWhenImageExists() {
    byte[] mockImageData = "preview data".getBytes();
    when(imageService.readPreviewImageByName("sample.png")).thenReturn(mockImageData);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_PNG);
    ResponseEntity<byte[]> expectedResult = new ResponseEntity<>(mockImageData, headers, HttpStatus.OK);

    ResponseEntity<byte[]> result = imageController.findPreviewImageByName("sample.png");

    assertEquals(expectedResult, result,
        "Preview image should be returned with correct headers and HTTP 200 status when data exists");
  }

  @Test
  void testFindPreviewImageByNameWhenImageEmpty() {
    when(imageService.readPreviewImageByName("empty.png")).thenReturn(new byte[0]);

    ResponseEntity<byte[]> result = imageController.findPreviewImageByName("empty.png");

    assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode(),
        "Status should be 204 NO_CONTENT when preview image data is empty");
    assertNull(result.getBody(),
        "Response body should be null when preview image data is empty");
  }

  @Test
  void testGetImageByCustomIdWhenImageExists() {
    byte[] mockImageData = "custom image data".getBytes();
    when(imageService.getImageByCustomId("custom-123")).thenReturn(mockImageData);

    ResponseEntity<byte[]> result = imageController.getImageByCustomId("custom-123");

    assertEquals(HttpStatus.OK, result.getStatusCode(),
        "Status should be 200 OK when image with custom ID exists");
    assertEquals(mockImageData, result.getBody(),
        "Response body should contain the image data");
    assertEquals(MediaType.IMAGE_JPEG, result.getHeaders().getContentType(),
        "Content-Type should be IMAGE_JPEG");
  }

  @Test
  void testGetImageByCustomIdWhenImageNotFound() {
    when(imageService.getImageByCustomId("nonexistent")).thenReturn(null);

    ResponseEntity<byte[]> result = imageController.getImageByCustomId("nonexistent");

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode(),
        "Status should be 404 NOT_FOUND when image with custom ID is not found");
    assertNull(result.getBody(),
        "Response body should be null when image is not found");
  }

  @Test
  void testGetImageByCustomIdWhenImageEmpty() {
    when(imageService.getImageByCustomId("empty-custom")).thenReturn(new byte[0]);

    ResponseEntity<byte[]> result = imageController.getImageByCustomId("empty-custom");

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode(),
        "Status should be 404 NOT_FOUND when image data is empty");
    assertNull(result.getBody(),
        "Response body should be null when image data is empty");
  }

  @Test
  void testUpdateImageSuccessfully() throws IOException {
    when(imageService.saveImageWithCustomId("custom-456", multipartFile))
        .thenReturn("image-id-123");

    ResponseEntity<String> result = imageController.updateImage("custom-456", multipartFile);

    assertEquals(HttpStatus.OK, result.getStatusCode(),
        "Status should be 200 OK when image is updated successfully");
    assertEquals("Image updated successfully", result.getBody(),
        "Response message should indicate successful update");
  }

  @Test
  void testUpdateImageWithInvalidFile() throws IOException {
    when(imageService.saveImageWithCustomId("custom-789", multipartFile))
        .thenThrow(new IOException("File validation failed"));

    ResponseEntity<String> result = imageController.updateImage("custom-789", multipartFile);

    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode(),
        "Status should be 400 BAD_REQUEST when file validation fails");
    assertEquals("File validation failed", result.getBody(),
        "Response message should indicate validation failure");
  }
}
