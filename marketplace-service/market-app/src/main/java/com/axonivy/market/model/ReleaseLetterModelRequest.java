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
  @Schema(description = "Release letter content", example = "This is the release letter of Marketplace sprint 43")
  private String content;

  @Schema(description = "Marketplace sprint", example = "S42")
  @NotBlank(message = "Marketplace sprint cannot be blank")
  private String sprint;
}
