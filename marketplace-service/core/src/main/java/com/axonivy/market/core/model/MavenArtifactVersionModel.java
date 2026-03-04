package com.axonivy.market.core.model;

import com.axonivy.market.core.entity.MavenArtifactVersion;
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
  private List<MavenArtifactVersion> artifactsByVersion;
}

