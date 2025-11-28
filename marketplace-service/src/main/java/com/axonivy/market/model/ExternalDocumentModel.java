package com.axonivy.market.model;

import com.axonivy.market.controller.ExternalDocumentController;
import com.axonivy.market.entity.ExternalDocumentMeta;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ExternalDocumentModel extends RepresentationModel<ExternalDocumentModel> {

  @EqualsAndHashCode.Include
  @Schema(description = "Product id", example = "portal")
  private String productId;

  @Schema(description = "Name of artifact", example = "Portal Guide")
  private String artifactName;

  @EqualsAndHashCode.Include
  @Schema(description = "Version of artifact", example = "10.0.0")
  private String version;

  @Schema(description = "Relative link of document page",
      example = "/market-cache/portal/portal-guide/10.0.12/doc/index.html")
  private String relativeLink;

  public static ExternalDocumentModel from(ExternalDocumentMeta externalDocument) {
    var model = ExternalDocumentModel.builder()
        .productId(externalDocument.getProductId())
        .version(externalDocument.getVersion())
        .relativeLink(externalDocument.getRelativeLink())
        .artifactName(externalDocument.getArtifactName())
        .build();
         model.add(linkTo(methodOn(ExternalDocumentController.class).findExternalDocument(externalDocument.getProductId(), externalDocument.getVersion())).withSelfRel());
    return model;
  }
}
