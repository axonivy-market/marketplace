package com.axonivy.market.entity;

import com.axonivy.market.model.MavenArtifactKey;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

import static com.axonivy.market.constants.EntityConstants.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity
@Table(name = MAVEN_ARTIFACT_MODEL)
public class MavenArtifactModel implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  @EmbeddedId
  private MavenArtifactKey id;  // Use composite key

  @Schema(description = "Display name and type of artifact", example = "Adobe Acrobat Sign Connector (.iar)")
  private String name;

  @Schema(description = "Artifact download url",
      example = "https://maven.axonivy.com/com/axonivy/connector/adobe/acrobat/sign/adobe-acrobat-sign-connector/10.0" +
              ".25/adobe-acrobat-sign-connector-10.0.25.iar")
  private String downloadUrl;

  @JsonIgnore
  private boolean isInvalidArtifact;

  private String productId;
}
