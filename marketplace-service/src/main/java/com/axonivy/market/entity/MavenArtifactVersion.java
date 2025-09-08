package com.axonivy.market.entity;

import com.axonivy.market.entity.key.MavenArtifactKey;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.axonivy.market.constants.EntityConstants.MAVEN_ARTIFACT_VERSION;

import java.io.Serial;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity
@Table(name = MAVEN_ARTIFACT_VERSION)
public class MavenArtifactVersion extends AbstractAuditableEntity<MavenArtifactKey> {

  @Serial
  private static final long serialVersionUID = 1;

  @EmbeddedId
  private MavenArtifactKey id;

  @Schema(description = "Display name and type of artifact", example = "Adobe Acrobat Sign Connector (.iar)")
  private String name;

  @Schema(description = "Artifact download url",
      example = "https://maven.axonivy.com/com/axonivy/connector/adobe/acrobat/sign/adobe-acrobat-sign-connector/10.0" +
          ".25/adobe-acrobat-sign-connector-10.0.25.iar")
  private String downloadUrl;

  @JsonIgnore
  private boolean isInvalidArtifact;

  private String groupId;
  private String productId;

  @Override
  public MavenArtifactKey getId() {
    return id;
  }

  @Override
  public void setId(MavenArtifactKey id) {
    this.id = id;
  }
}
