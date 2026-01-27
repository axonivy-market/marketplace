package com.axonivy.market.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReleaseLetterModelRequest {
  @Schema(description = "Release letter content", example = "This is the release letter of Marketplace version 1.20.0.")
  private String content;

  @Schema(description = "Marketplace release version", example = "1.20.0")
  @NotBlank(message = "Marketplace release version cannot be blank")
  private String releaseVersion;
}
