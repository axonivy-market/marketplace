package com.axonivy.market.controller;

import com.axonivy.market.aop.annotation.Authorized;
import com.axonivy.market.core.controller.CoreImageController;
import com.axonivy.market.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.constants.RequestParamConstants.CUSTOM_ID;
import static com.axonivy.market.constants.RequestParamConstants.FILE;

import static com.axonivy.market.constants.RequestMappingConstants.BY_FILE_NAME;
import static com.axonivy.market.core.constants.CoreRequestMappingConstants.IMAGE;

@RestController
@RequestMapping(IMAGE)
@Tag(name = "Image Controllers", description = "API collection to get image's detail.")
public class ImageController extends CoreImageController {
  private final ImageService imageService;

  public ImageController(ImageService imageService) {
    super(imageService);
    this.imageService = imageService;
  }

  @GetMapping(BY_FILE_NAME)
  @Operation(hidden = true)
  public ResponseEntity<byte[]> findPreviewImageByName(
      @PathVariable("imageName") String imageName) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_PNG);
    byte[] imageData = imageService.readPreviewImageByName(imageName);
    if (imageData.length == 0) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
  }

  @GetMapping(BY_CUSTOM_ID)
  @Operation(summary = "Get an image by custom ID")
  public ResponseEntity<byte[]> getImageByCustomId(
      @PathVariable(CUSTOM_ID) String customId) {
    byte[] imageData = imageService.getImageByCustomId(customId);
    if (imageData == null || imageData.length == 0) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG);
    return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
  }

  @PutMapping(BY_CUSTOM_ID)
  @Operation(hidden = true)
  @Authorized
  public ResponseEntity<String> updateImage(
      @PathVariable(CUSTOM_ID) String customId,
      @RequestParam(FILE) MultipartFile file) {
    String message;
    HttpStatus status;
    try {
      imageService.saveImageWithCustomId(customId, file);
      message = "Image updated successfully: ";
      status = HttpStatus.OK;
    } catch (IOException ioException) {
      message = "File validation failed";
      status = HttpStatus.BAD_REQUEST;
    } catch (Exception e) {
      message = "Failed to update image with given custom id";
      status = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    return new ResponseEntity<>(message, status);
  }
}
