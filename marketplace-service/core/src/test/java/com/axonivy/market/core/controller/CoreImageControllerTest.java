package com.axonivy.market.core.controller;

import com.axonivy.market.core.service.CoreImageService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.when;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class CoreImageControllerTest {

  @Mock
  private CoreImageService coreImageService;

  @InjectMocks
  private CoreImageController coreImageController;

  @Test
  void testGetImageFromId() {
    byte[] mockImageData = "image data".getBytes();
    when(coreImageService.readImage("66e2b14868f2f95b2f95549a")).thenReturn(mockImageData);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_PNG);
    ResponseEntity<byte[]> expectedResult = new ResponseEntity<>(mockImageData, headers, HttpStatus.OK);

    ResponseEntity<byte[]> result = coreImageController.findImageById("66e2b14868f2f95b2f95549a");

    assertEquals(expectedResult, result,
        "ResponseEntity should match the expected result, including image data, headers, and status");
  }

  @Test
  void testGetImageFromIdWhenImageNotFound() {
    when(coreImageService.readImage("missing-id")).thenReturn(null);

    ResponseEntity<byte[]> result = coreImageController.findImageById("missing-id");

    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode(),
        "Status should be 404 NOT_FOUND when imageService returns null");
    assertNull(result.getBody(),
        "Response body should be null when image is not found");
  }

  @Test
  void testGetImageFromIdWhenImageEmpty() {
    when(coreImageService.readImage("empty-id")).thenReturn(new byte[0]);

    ResponseEntity<byte[]> result = coreImageController.findImageById("empty-id");

    assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode(),
        "Status should be 204 NO_CONTENT when imageService returns an empty array");
    assertNull(result.getBody(),
        "Response body should be null when image is empty");
  }
}
