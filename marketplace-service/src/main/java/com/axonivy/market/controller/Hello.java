package com.axonivy.market.controller;

import com.axonivy.market.entity.Image;
import com.axonivy.market.entity.MavenArtifactVersion;
import com.axonivy.market.repository.MavenArtifactVersionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.axonivy.market.constants.RequestMappingConstants.BY_ID;
import static com.axonivy.market.constants.RequestMappingConstants.IMAGE;
import static com.axonivy.market.constants.RequestParamConstants.ID;

@RestController
@RequestMapping("hello")
public class Hello {
  private final MavenArtifactVersionRepository mavenArtifactVersionRepository;

  public Hello(MavenArtifactVersionRepository mavenArtifactVersionRepository) {
    this.mavenArtifactVersionRepository = mavenArtifactVersionRepository;
  }


  @GetMapping(BY_ID)
  public ResponseEntity<MavenArtifactVersion> findImageById(@PathVariable(ID) String id) {
  MavenArtifactVersion mavenArtifactVersion = mavenArtifactVersionRepository.findById(id).orElse(null);
    return new ResponseEntity<>(mavenArtifactVersion, HttpStatus.OK);
  }
}
