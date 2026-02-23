package com.axonivy.market.controller;

import com.axonivy.market.aop.annotation.Authorized;
import com.axonivy.market.assembler.ReleaseLetterModelAssembler;
import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.model.ReleaseLetterModel;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import com.axonivy.market.service.ReleaseLetterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static com.axonivy.market.constants.RequestMappingConstants.*;
import static com.axonivy.market.constants.RequestParamConstants.SPRINT;
import static com.axonivy.market.core.constants.CoreRequestParamConstants.ID;

@AllArgsConstructor
@RestController
@RequestMapping(RELEASE_LETTER)
@Tag(name = "Release Letter Controller", description = "API collection to get and search for release letters")
public class ReleaseLetterController {
  private final ReleaseLetterService releaseLetterService;
  private final ReleaseLetterModelAssembler releaseLetterModelAssembler;
  private final PagedResourcesAssembler<ReleaseLetter> pagedResourcesAssembler;

  @GetMapping
  @Operation(summary = "Retrieve a paginated list of all release letter")
  public ResponseEntity<PagedModel<ReleaseLetterModel>> findAllReleaseLetters(@ParameterObject Pageable pageable) {
    Page<ReleaseLetter> releaseLetters = releaseLetterService.findAllReleaseLetters(pageable);
    if (releaseLetters.isEmpty()) {
      return generateEmptyPagedModel();
    }
    var pageResources = pagedResourcesAssembler.toModel(releaseLetters, releaseLetterModelAssembler);
    return ResponseEntity.ok(pageResources);
  }

  @Authorized
  @GetMapping(BY_ID)
  @Operation(hidden = true)
  public ResponseEntity<ReleaseLetterModel> findReleaseLetterById(
      @PathVariable(ID) @Parameter(description = "The release letter id", example = "66e7efc8a24f36158df06fc7",
          in = ParameterIn.PATH) String id) {
    var releaseLetter = releaseLetterService.findReleaseLetterById(id);
    var releaseLetterResource = releaseLetterModelAssembler.toModel(releaseLetter);
    releaseLetterResource.add(linkTo(methodOn(this.getClass()).findReleaseLetterBySprint(releaseLetter.getSprint())).withSelfRel());

    return ResponseEntity.ok(releaseLetterResource);
  }

  @GetMapping(BY_SPRINT)
  @Operation(summary = "Retrieve a release letter by sprint name",
      description = "Get release letter by sprint name")
  public ResponseEntity<ReleaseLetterModel> findReleaseLetterBySprint(
      @PathVariable(SPRINT) @Parameter(description = "The sprint version", example = "S43",
          in = ParameterIn.PATH) String sprint) {
    var releaseLetter = releaseLetterService.findReleaseLetterBySprint(sprint);
    var releaseLetterResource = releaseLetterModelAssembler.toModel(releaseLetter);
    releaseLetterResource.add(linkTo(methodOn(this.getClass()).findReleaseLetterBySprint(releaseLetter.getSprint())).withSelfRel());

    return ResponseEntity.ok(releaseLetterResource);
  }

  @GetMapping(BY_LATEST)
  @Operation(summary = "Find active release letter",
      description = "Get currently active release letter.")
  public ResponseEntity<PagedModel<ReleaseLetterModel>> findLatestReleaseLetter(@ParameterObject Pageable pageable) {
    Page<ReleaseLetter> results = releaseLetterService.findLatestReleaseLetter(pageable);
    if (results.isEmpty()) {
      return generateEmptyPagedModel();
    }
    var releaseLettersPageResources = pagedResourcesAssembler.toModel(results, releaseLetterModelAssembler);
    return ResponseEntity.ok(releaseLettersPageResources);
  }

  @Authorized
  @PostMapping()
  @Operation(hidden = true)
  public ResponseEntity<ReleaseLetterModel> createReleaseLetter(
      @RequestBody @Valid ReleaseLetterModelRequest releaseLetterModelRequest) {
    var newReleaseLetter = releaseLetterService.createReleaseLetter(releaseLetterModelRequest);
    var location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path(BY_ID)
        .buildAndExpand(newReleaseLetter.getId())
        .toUri();
    return ResponseEntity.created(location).build();
  }

  @Authorized
  @PutMapping(BY_SPRINT)
  @Operation(hidden = true)
  public ResponseEntity<ReleaseLetterModel> updateReleaseLetter(
      @PathVariable(SPRINT) @Parameter(description = "The sprint name", example = "S43",
          in = ParameterIn.PATH) String sprint,
      @RequestBody @Valid ReleaseLetterModelRequest releaseLetterModelRequest
  ) {
    var updatedReleaseLetter = releaseLetterService.updateReleaseLetter(sprint, releaseLetterModelRequest);
    var releaseLetterResource = releaseLetterModelAssembler.toModel(updatedReleaseLetter);
    releaseLetterResource.add(linkTo(methodOn(this.getClass()).findReleaseLetterBySprint(updatedReleaseLetter.getSprint())).withSelfRel());
    return ResponseEntity.ok(releaseLetterResource);
  }

  @Authorized
  @DeleteMapping(BY_SPRINT)
  @Operation(hidden = true)
  public void deleteReleaseLetter(
      @PathVariable(SPRINT) @Parameter(description = "The sprint name", example = "S43",
          in = ParameterIn.PATH) String sprint) {
    releaseLetterService.deleteReleaseLetterBySprint(sprint);
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<PagedModel<ReleaseLetterModel>> generateEmptyPagedModel() {
    var emptyPagedModel = (PagedModel<ReleaseLetterModel>) pagedResourcesAssembler.toEmptyModel(Page.empty(),
        ReleaseLetterModel.class);
    return ResponseEntity.ok(emptyPagedModel);
  }
}
