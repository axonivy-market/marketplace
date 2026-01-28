package com.axonivy.market.core.controller;

import com.axonivy.market.core.entity.Image;
import com.axonivy.market.core.service.CoreImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import static com.axonivy.market.core.constants.CoreRequestMappingConstants.BY_ID;
import static com.axonivy.market.core.constants.CoreRequestMappingConstants.IMAGE;
import static com.axonivy.market.core.constants.CoreRequestParamConstants.ID;

@AllArgsConstructor
@Tag(name = "Image Controllers", description = "API collection to get image's detail.")
public class CoreImageController {
  private final CoreImageService coreImageService;

  @GetMapping(BY_ID)
  @Operation(summary = "Get the image content by id",
      description = "Collect the byte[] of image with contentType in header is PNG")
  @ApiResponse(responseCode = "200", description = "Image found and returned",
      content = @Content(mediaType = MediaType.IMAGE_PNG_VALUE, schema = @Schema(implementation = Image.class)))
  @ApiResponse(responseCode = "404", description = "Image not found")
  @ApiResponse(responseCode = "204", description = "No content (image empty)")
  public ResponseEntity<byte[]> findImageById(
      @PathVariable(ID) @Parameter(description = "The image id", example = "66e7efc8a24f36158df06fc7",
          in = ParameterIn.PATH) String id) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_PNG);
    byte[] imageData = coreImageService.readImage(id);
    if (imageData == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    if (imageData.length == 0) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
  }
}
