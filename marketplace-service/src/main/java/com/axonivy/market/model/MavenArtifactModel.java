package com.axonivy.market.model;

import com.axonivy.market.entity.ExternalDocumentMeta;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Relation(collectionRelation = "mavenArtifacts", itemRelation = "mavenArtifact")
public class MavenArtifactModel {

  @Id
  @Schema(description = "Display name and type of artifact", example = "Adobe Acrobat Sign Connector (.iar)")
  private String name;
  @Schema(description = "The download url as absolute path",
      example = "https://maven.axonivy.com/com/axonivy/portal/portal/10.0.0/portal-10.0.0.iar")
  private String downloadUrl;
  @Schema(description = "The download url as relative path",
      example = "/com/axonivy/portal/portal/10.0.0/portal-10.0.0.iar")
  private String relativeUrl;
  @JsonIgnore
  private boolean isInvalidArtifact;
}