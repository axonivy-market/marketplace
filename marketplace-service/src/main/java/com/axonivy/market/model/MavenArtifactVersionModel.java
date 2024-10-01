package com.axonivy.market.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MavenArtifactVersionModel {
  @Schema(description = "Target version", example = "10.0.19")
  private String version;
  private List<MavenArtifactModel> artifactsByVersion;
}