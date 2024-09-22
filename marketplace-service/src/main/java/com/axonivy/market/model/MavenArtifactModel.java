package com.axonivy.market.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class MavenArtifactModel implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  @Schema(description = "Display name and type of artifact", example = "Adobe Acrobat Sign Connector (.iar)")
  private String name;
  @Schema(description = "Artifact download url", example = "https://maven.axonivy" +
      ".com/com/axonivy/connector/adobe/acrobat/sign/adobe-acrobat-sign-connector/10.0" +
      ".25/adobe-acrobat-sign-connector-10.0.25.iar")
  private String downloadUrl;
}