package com.axonivy.market.controller;

import com.axonivy.market.model.ReleasePreview;
import com.axonivy.market.service.ReleasePreviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static com.axonivy.market.constants.RequestMappingConstants.RELEASE_PREVIEW;

@Log4j2
@RestController
@RequestMapping(RELEASE_PREVIEW)
@Tag(name = "Release Preview Controller", description = "API to extract zip file and return README data.")
@AllArgsConstructor
public class ReleasePreviewController {

  private final ReleasePreviewService previewService;

  @PostMapping
  @Operation(hidden = true)
  public ResponseEntity<Object> extractZipFile(@RequestParam(value = "file") MultipartFile file) {
    var baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
    ReleasePreview preview = previewService.extract(file, baseUrl);
    if (preview == null) {
      return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    return ResponseEntity.ok(preview);
  }
}
