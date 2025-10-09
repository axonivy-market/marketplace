package com.axonivy.market.controller;

import com.axonivy.market.model.DocumentInfoResponse;
import com.axonivy.market.service.ExternalDocumentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static com.axonivy.market.constants.RequestMappingConstants.DOCUMENT;
import static com.axonivy.market.constants.RequestMappingConstants.DOCUMENT_VERSION_LANGUAGE;

@Log4j2
@RestController
@RequestMapping(DOCUMENT)
@Tag(name = "Document Language Controller",
    description = "API collection for utilizing document versions and languages")
@AllArgsConstructor
public class DocumentLanguageController {

  private final ExternalDocumentService externalDocumentService;

  @GetMapping(DOCUMENT_VERSION_LANGUAGE)
  public ResponseEntity<DocumentInfoResponse> getDocumentByVersionAndLanguage(@PathVariable String artifact,
      @PathVariable String version, @PathVariable String language) {
    var host = ServletUriComponentsBuilder.fromCurrentRequestUri().replacePath(null).build().toUriString();
    DocumentInfoResponse response = externalDocumentService.findDocVersionsAndLanguages(artifact, version, language,
        host);
    if (response == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(response);
  }
}