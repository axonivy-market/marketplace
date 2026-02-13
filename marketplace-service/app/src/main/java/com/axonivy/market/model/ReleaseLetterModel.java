package com.axonivy.market.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Relation(itemRelation = "releaseLetter")
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ReleaseLetterModel extends RepresentationModel<ReleaseLetterModel> {

  @EqualsAndHashCode.Include
  @Schema(description = "Sprint version", example = "S42")
  private String sprint;

  @Schema(description = "Release letter content", example = "This is the release letter of Marketplace sprint 43")
  private String content;

  @Schema(description = "Decide whether the created release letter should be the latest", example = "false")
  private boolean isLatest;

  @Schema(description = "The time the release letter was created", example = "2026-02-10 09:47:32.243")
  private Date createdAt;
}
