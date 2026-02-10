package com.axonivy.market.controller;

import com.axonivy.market.assembler.ReleaseLetterModelAssembler;
import com.axonivy.market.constants.RequestParamConstants;

import static com.axonivy.market.constants.RequestParamConstants.*;

import com.axonivy.market.entity.ReleaseLetter;
import com.axonivy.market.model.FeedbackModel;
import com.axonivy.market.model.ReleaseLetterModel;
import com.axonivy.market.model.ReleaseLetterModelRequest;
import com.axonivy.market.service.ReleaseLetterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.axonivy.market.constants.RequestMappingConstants.*;

@AllArgsConstructor
@RestController
@RequestMapping(RELEASE_LETTER)
@Tag(name = "Release Letter Controller", description = "API collection to get and search for release letters")
public class ReleaseLetterController {
    private final ReleaseLetterService releaseLetterService;
    private final ReleaseLetterModelAssembler releaseLetterModelAssembler;
    private final PagedResourcesAssembler<ReleaseLetter> pagedResourcesAssembler;

    @GetMapping
    public ResponseEntity<PagedModel<ReleaseLetterModel>> findAllReleaseLetters(@ParameterObject Pageable pageable) {
        Page<ReleaseLetter> releaseLetters = releaseLetterService.findAllReleaseLetters(pageable);
        if (releaseLetters.isEmpty()) {
            return generateEmptyPagedModel();
        }
        var pageResources = pagedResourcesAssembler.toModel(releaseLetters, releaseLetterModelAssembler);
        return ResponseEntity.ok(pageResources);
    }

    @GetMapping(BY_ID)
    public ResponseEntity<ReleaseLetterModel> findReleaseLetterById(
            @PathVariable(ID) @Parameter(description = "The release letter id", example = "66e7efc8a24f36158df06fc7",
                    in = ParameterIn.PATH) String id) {
        ReleaseLetter releaseLetter = releaseLetterService.findReleaseLetterById(id);
        var releaseLetterResource = releaseLetterModelAssembler.toModel(releaseLetter);

        return ResponseEntity.ok(releaseLetterResource);
    }

    @GetMapping(BY_SPRINT)
    public ResponseEntity<ReleaseLetterModel> findReleaseLetterBySprint(
            @PathVariable(SPRINT) @Parameter(description = "The sprint version", example = "S43",
                    in = ParameterIn.PATH) String sprint) {
        ReleaseLetter releaseLetter = releaseLetterService.findReleaseLetterBySprint(sprint);
        var releaseLetterResource = releaseLetterModelAssembler.toModelFromReleaseVersion(releaseLetter);

        return ResponseEntity.ok(releaseLetterResource);
    }

    @PostMapping()
    @Operation(hidden = true)
    public ResponseEntity<ReleaseLetterModel> createReleaseLetter(
            @RequestBody @Valid ReleaseLetterModelRequest releaseLetterModelRequest,
            HttpServletRequest request) {
        String token = request.getHeader(RequestParamConstants.X_AUTHORIZATION);
        var newReleaseLetter = releaseLetterService.createReleaseLetter(releaseLetterModelRequest);
        var releaseLetterResource = releaseLetterModelAssembler.toModelFromReleaseVersion(newReleaseLetter);
//    var location = ServletUriComponentsBuilder.fromCurrentRequest()
//        .path(BY_ID)
//        .buildAndExpand(newReleaseLetter.getId())
//        .toUri();
//    System.out.println("Location: " + location);

        return ResponseEntity.ok(releaseLetterResource);
    }

//    @Authorized
    @PutMapping(BY_SPRINT)
    @Operation(hidden = true)
    public ResponseEntity<ReleaseLetterModel> updateReleaseLetter(
            @PathVariable(SPRINT) @Parameter(description = "The release version", example = "S43",
                    in = ParameterIn.PATH) String releaseVersion,
            @RequestBody @Valid ReleaseLetterModelRequest releaseLetterModelRequest
    ) {
//    String token = request.getHeader(RequestParamConstants.X_AUTHORIZATION);
        var updatedReleaseLetter = releaseLetterService.updateReleaseLetter(releaseVersion, releaseLetterModelRequest);
        var releaseLetterResource = releaseLetterModelAssembler.toModelFromReleaseVersion(updatedReleaseLetter);
//    var location = ServletUriComponentsBuilder.fromCurrentRequest()
//        .path(BY_ID)
//        .buildAndExpand(newReleaseLetter.getId())
//        .toUri();
//    System.out.println("Location: " + location);

        return ResponseEntity.ok(releaseLetterResource);
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<PagedModel<ReleaseLetterModel>> generateEmptyPagedModel() {
        var emptyPagedModel = (PagedModel<ReleaseLetterModel>) pagedResourcesAssembler.toEmptyModel(Page.empty(),
                FeedbackModel.class);
        return ResponseEntity.ok(emptyPagedModel);
    }
}
