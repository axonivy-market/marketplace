package com.axonivy.market.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Transient;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class MavenArtifactModel implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  @Schema(description = "Display name and type of artifact", example = "Adobe Acrobat Sign Connector (.iar)")
  private String name;
  @Schema(description = "Artifact download url", example = "https://maven.axonivy.com/com/axonivy/connector/adobe/acrobat/sign/adobe-acrobat-sign-connector/10.0.25/adobe-acrobat-sign-connector-10.0.25.iar")
  private String downloadUrl;
  @Transient
  private Boolean isProductArtifact;

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || getClass() != object.getClass()) {
      return false;
    }
    MavenArtifactModel reference = (MavenArtifactModel) object;
    return Objects.equals(name, reference.getName()) && Objects.equals(downloadUrl, reference.getDownloadUrl());
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, downloadUrl);
  }
}