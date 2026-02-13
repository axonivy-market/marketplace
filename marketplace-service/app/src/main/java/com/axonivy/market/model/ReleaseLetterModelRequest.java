package com.axonivy.market.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReleaseLetterModelRequest {

  @Schema(description = "Release letter content", example = "This is the release letter of Marketplace sprint 43")
  private String content;

  @Schema(description = "Sprint sprint", example = "S42")
  private String sprint;

  @Schema(description = "Decide whether the created release letter should be the latest", example = "false")
  private boolean isLatest;
}
