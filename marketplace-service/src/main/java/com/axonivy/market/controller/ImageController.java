package com.axonivy.market.controller;

import com.axonivy.market.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.axonivy.market.constants.RequestMappingConstants.IMAGE;
import static com.axonivy.market.constants.RequestMappingConstants.BY_ID;
import static com.axonivy.market.constants.RequestParamConstants.ID;

@RestController
@RequestMapping(IMAGE)
@Tag(name = "Image Controllers", description = "API collection to get image's detail.")
public class ImageController {
  private final ImageService imageService;

  public ImageController(ImageService imageService) {
    this.imageService = imageService;
  }

  @GetMapping(BY_ID)
  @Operation(summary = "Get the image content by id", description = "Collect the byte[] of image with contentType in header is PNG")
  public ResponseEntity<byte[]> findImageById(@PathVariable(ID) String id) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_PNG);
    byte[] imageData = imageService.readImage(id);
    if (imageData == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    if (imageData.length == 0) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
  }
}
