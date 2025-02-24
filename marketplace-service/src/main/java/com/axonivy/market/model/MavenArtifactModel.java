package com.axonivy.market.model;

import com.axonivy.market.entity.MavenArtifactVersion;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Entity
@Table(name = "maven_artifact_model")
public class MavenArtifactModel implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  private String id;

  @Schema(description = "Display name and type of artifact", example = "Adobe Acrobat Sign Connector (.iar)")
  private String name;

  @Schema(description = "Artifact download url",
      example = "https://maven.axonivy.com/com/axonivy/connector/adobe/acrobat/sign/adobe-acrobat-sign-connector/10.0" +
              ".25/adobe-acrobat-sign-connector-10.0.25.iar")
  private String downloadUrl;

  @JsonIgnore
  private boolean isInvalidArtifact;

  private String artifactId;
  private String productVersion;

  @ManyToOne
  @JoinColumn(name = "product_version_id")
  @JsonBackReference("productVersionReference")
  private MavenArtifactVersion productVersionReference;

  @ManyToOne
  @JoinColumn(name = "additional_product_version_id")
  @JsonBackReference("additionalVersionReference")
  private MavenArtifactVersion additionalVersionReference;

  @PrePersist
  private void ensureId() {
    if (this.id == null) {
      this.id = UUID.randomUUID().toString();
    }
  }
}
