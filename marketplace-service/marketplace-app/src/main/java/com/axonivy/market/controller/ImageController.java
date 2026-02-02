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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
