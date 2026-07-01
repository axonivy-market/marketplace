package com.axonivy.market.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminGitHubCallbackRequest {
  @Schema(description = "Exchange code")
  private String code;

  @Schema(description = "One-time OAuth state")
  private String state;
}
