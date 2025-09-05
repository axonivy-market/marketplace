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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

  @Mock
  private ImageService imageService;

  @InjectMocks
  private ImageController imageController;

  @Test
  void test_getImageFromId() {
    byte[] mockImageData = "image data".getBytes();
    when(imageService.readImage("66e2b14868f2f95b2f95549a")).thenReturn(mockImageData);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_PNG);
    ResponseEntity<byte[]> expectedResult = new ResponseEntity<>(mockImageData, headers, HttpStatus.OK);

    ResponseEntity<byte[]> result = imageController.findImageById("66e2b14868f2f95b2f95549a");

    assertEquals(expectedResult, result,
        "ResponseEntity should match the expected result, including image data, headers, and status");
  }
}
