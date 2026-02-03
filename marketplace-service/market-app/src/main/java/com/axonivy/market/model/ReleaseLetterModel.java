package com.axonivy.market.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

@Getter
@Setter
@NoArgsConstructor
@Relation(itemRelation = "releaseLetter")
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ReleaseLetterModel extends RepresentationModel<ReleaseLetterModel> {

  @EqualsAndHashCode.Include
  @Schema(description = "Release version", example = "S42")
  private String releaseVersion;

  @Schema(description = "Release letter content", example = "This is the release letter of Marketplace version 1.20.0")
  private String content;
}
