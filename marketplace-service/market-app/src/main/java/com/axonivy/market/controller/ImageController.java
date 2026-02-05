package com.axonivy.market.controller;

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

import static com.axonivy.market.constants.RequestMappingConstants.BY_FILE_NAME;
import static com.axonivy.market.constants.RequestMappingConstants.IMAGE;

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

  @GetMapping("/custom/{customId}")
  @Operation(summary = "Get an image by custom ID")
  public ResponseEntity<byte[]> getImageByCustomId(
      @PathVariable("customId") String customId) {
    byte[] imageData = imageService.getImageByCustomId(customId);
    if (imageData == null || imageData.length == 0) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG);
    return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an image by ID with multipart file")
  public ResponseEntity<String> updateImage(
      @PathVariable("id") String id,
      @RequestParam("file") MultipartFile file) {
    try {
      String savedId = imageService.saveImageWithCustomId(id, file);
      return new ResponseEntity<>(savedId, HttpStatus.OK);
    } catch (IOException ioException) {
      return new ResponseEntity<>("File validation failed: " + ioException.getMessage(), HttpStatus.BAD_REQUEST);
    } catch (Exception e) {
      return new ResponseEntity<>("Failed to update image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
