package com.axonivy.market.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DesignerInstallation {
  @Schema(description = "Ivy designer version", example = "11.4.0")
  private String designerVersion;
  private int numberOfDownloads;
}
